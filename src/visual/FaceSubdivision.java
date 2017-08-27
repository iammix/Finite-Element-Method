package visual;

// Subdivision of an edge and a face
public class FaceSubdivision {

    // Number of points used for subdivision
    public int nFacePoints;
    // Local coordinates of points
    public double[] xi, et;
    // Number of triangles and their indexes
    public int nTrigs, trigs[][];

    private double[] ze;

    public FaceSubdivision() {

        int npMax = (VisData.nDivMax +1)*(VisData.nDivMax +1);
        xi = new double[npMax];
        et = new double[npMax];
        ze = new double[npMax];

        trigs = new int[2*VisData.nDivMax*VisData.nDivMax][3];
    }

    // Compute number of edge subdivisions.
    // xyz - array [][3] of face nodal coordinates.
    // fun - function values at nodes.
    // deltaf - function range for the whole model.
    // funDiv - if true perform results-based subdivision.
    // i1, i2, i3 - indexes of three nodes on the edge.
    // returns   number of edge subdivisions.
    int numberOfEdgeDivisions(double[][] xyz, double[] fun,
                              double deltaf, boolean funDiv,
                              int i1, int i2, int i3) {

        int nDiv = VisData.nDivMin;

        // Curvature-based subdivision
        double a1 = distance(xyz, i1, i3);
        double a2 = distance(xyz, i1, i2);
        double a3 = distance(xyz, i2, i3);

        double p = 0.5*(a1 + a2 + a3);
        double s = Math.sqrt(p*(p-a1)*(p-a2)*(p-a3));
        // Curvature parameter
        double ro = 2*s/a1*Math.abs(1/a2 + 1/a3);
        nDiv = Math.max (nDiv, (int)(1.5 + VisData.Csub*ro));

        if (!funDiv) return Math.min(nDiv, VisData.nDivMax);

        // Results-based subdivision
        int n = (int) (1.5 + VisData.Fsub*Math.max(
                Math.abs(fun[i1]-fun[i2]),
                Math.abs(fun[i2]-fun[i3]))/deltaf);

        return Math.min(VisData.nDivMax, Math.max(nDiv, n));
    }

    // Distance between two points.
    // xyz - array [][3] containing point locations.
    // p1, p2 - point indexes.
    // returns   distance
    double distance(double[][] xyz, int p1, int p2) {
        return Math.sqrt(
            (xyz[p1][0]-xyz[p2][0])*(xyz[p1][0]-xyz[p2][0])
          + (xyz[p1][1]-xyz[p2][1])*(xyz[p1][1]-xyz[p2][1])
          + (xyz[p1][2]-xyz[p2][2])*(xyz[p1][2]-xyz[p2][2]));
    }

    // Subdivide 2 x 2 square into triangles.
    // ndiv - number of subdivisions on edges.
    public void subdivideFace(int[] ndiv) {

        // Small number to generate slightly imparallel lines
        final double EPS = 1.e-6;
        double xiQ, etQ;
        nFacePoints = 0;

        // Squared min distance for node placement
        int ndivMax = 1;
        for (int side = 0; side < 4; side++)
            ndivMax = Math.max(ndivMax, ndiv[side]);
        double dMin = 2.0/ndivMax;
        double minNodeDist2 = Math.min(1.0,0.8*dMin*dMin);
        // Add point at the face center
        addPoint(0, EPS, EPS, 0.0);

        // Generate points for triangular sectors
        for (int sector = 0; sector < 4; sector++) {
            int n = ndiv[sector];
            double d = 2.0/n;
            // Points inside quarter of the element
            for (int row = 0; row <= n/2; row++) {
                for (int i = row; i <= n-row; i++) {
                    xiQ = -1.0 + d*i;
                    etQ = -1.0 + d*row;
                    if (row > 0) {
                        xiQ += EPS;
                        etQ += EPS*xiQ;
                    }
                    addPoint(sector, xiQ, etQ, minNodeDist2);
                }
            }
            if (n<3) addPoint(sector, EPS, -0.5, minNodeDist2);
        }

        // Delaunay triangulation
        nTrigs = triangulateDelaunay();
    }

    // Add point to seeded points for Delaunay triangulation.
    // sector - sector number 0..3.
    // xiQ - xi-sector-coordinate of the point.
    // etQ - eta-sector-coordinate of the point.
    // minDistance2 - squared min distance between points.
    private void addPoint(int sector, double xiQ, double etQ,
                          double minDistance2) {

        double sin[] = {0, 1,  0, -1};
        double cos[] = {1, 0, -1,  0};

        double x = xiQ*cos[sector] - etQ*sin[sector];
        double y = xiQ*sin[sector] + etQ*cos[sector];

        int i;
        for (i = 0; i < nFacePoints; i++) {
            double dx = x - xi[i];
            double dy = y - et[i];
            if (dx*dx + dy*dy < minDistance2) break;
        }

        if (i == nFacePoints) {
            xi[nFacePoints] = x;
            et[nFacePoints] = y;
            nFacePoints++;
        }
    }

    // Create triangular mesh using Delaunay approach.
    // returns  number of triangles
    private int triangulateDelaunay() {

        double EPS = 1.e-6;
        int n = nFacePoints;
        int nt = 0;

        for (int i = 0; i < n; i++)
            ze[i] = xi[i]*xi[i] + et[i]*et[i];

        for (int i = 0; i < n; i++) {
            double pix = xi[i];
            double pie = et[i];
            double piz = ze[i];

            for (int j = i + 1; j < n; j++) {
                double pjx = xi[j];
                double pje = et[j];
                double pjz = ze[j];

                for (int k = i + 1; k < n; k++) {
                    double pkx = xi[k];
                    double pke = et[k];
                    double pkz = ze[k];
                    double zn = (pjx - pix)*(pke - pie)
                              - (pkx - pix)*(pje - pie);

                    if (j == k || zn > 0) continue;

                    double xn = (pje - pie)*(pkz - piz)
                              - (pke - pie)*(pjz - piz);
                    double en = (pkx - pix)*(pjz - piz)
                              - (pjx - pix)*(pkz - piz);

                    int m;
                    for (m = 0; m < n; m++) {
                        double pmx = xi[m];
                        double pmy = et[m];
                        double pmz = ze[m];
                        if (m != i && m != j && m != k
                                && (pmx-pix)*xn + (pmy-pie)*en
                                 + (pmz-piz)*zn > 0)
                            break;
                    }

                    if (m == n) {
                        double area = pix*(pke-pje)
                            + pkx*(pje-pie) + pjx*(pie-pke);
                        if (Math.abs(area) > EPS) {
                            // Add triangle polygon
                            // (unticlockwise node order)
                            trigs[nt][0] = i;
                            trigs[nt][1] = k;
                            trigs[nt][2] = j;
                            nt++;
                        }
                    }
                }
            }
        }
        return nt;
    }

}
