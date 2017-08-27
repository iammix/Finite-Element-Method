package visual;

import elem.*;

import javax.media.j3d.*;
import java.util.ListIterator;

// Element subfaces, subedges and nodes.
class SurfaceSubGeometry extends SurfaceGeometry {

    int nVertices;
    private FaceSubdivision fs;
    // Edge subdivisions for faces
    private int edgeDiv[][];
    // Coordinates, normals and texture coords for
    // triangle array of the whole model surface
    private float xyzSurface[], norSurface[], texSurface[];

    // Arrays for one element face
    private double xyzFace[][] = new double[8][3];
    private double funFace[] =  new double[8];
    private double an[] = new double[8];
    private double deriv[][] = new double[8][2];
    // Arrays for subdivided surface
    private double[] xyzFacePoints;
    private double[] norFacePoints;
    private double[] texFacePoints;

    // Constructor for subdivision of faces, edges and nodes.
    SurfaceSubGeometry() {

        // Call to constructor of SurfaceGeometry
        super();

        // Constructor edge/face subdivider
        fs = new FaceSubdivision();

        edgeDiv = new int[nFaces][4];

        int np = (VisData.nDivMax +1)*(VisData.nDivMax +1);
        xyzFacePoints = new double[3*np];
        norFacePoints = new double[3*np];
        texFacePoints = new double[np];

        // Determine edge subdivisions for element faces
        int nTrigs = setEdgeDivisions();

        xyzSurface = new float[9*nTrigs];
        norSurface = new float[9*nTrigs];
        texSurface = new float[6*nTrigs];

        // Perform subdivision (triangulation) for faces
        setModelTriangles();
    }

    // Determine numbers of edge subdivisions for all
    // element faces edgeDiv[nFaces][4].
    // returns  upper estimate of number of triangles
    // for the surface of the finite element model.
    int setEdgeDivisions() {

        int nTriangles = 0;
        ListIterator f = listFaces.listIterator(0);

        for (int face=0; face<nFaces; face++) {
            setFaceCoordFun((int[])f.next());
            for (int i = 0; i < 4; i++) {
                int nd = fs.numberOfEdgeDivisions(xyzFace,
                        funFace, deltaf, VisData.drawContours,
                        2*i,2*i+1,(2*i+2)%8);
                edgeDiv[face][i] = nd;
                nTriangles += (int) (0.6*nd*nd + 2);
            }
        }
        return nTriangles;
    }

    // Set coordinates and function values for face nodes
    void setFaceCoordFun(int[] faceNodes) {

        for (int i = 0; i < faceNodes.length; i++) {
            int ind = faceNodes[i] - 1;
            for (int j = 0; j < 3; j++) {
                if (fem.nDim ==2 && j==2) xyzFace[i][j] = 0;
                else xyzFace[i][j] = fem.getNodeCoord(ind,j);
            }
            if (VisData.drawContours) funFace[i] = fun[ind];
        }
    }

    // Perform triangulation for all faces.
    void setModelTriangles() {

        int[] faceNodes;
        nVertices = 0;
        ListIterator f = listFaces.listIterator(0);

        for (int face = 0; face < nFaces; face++) {

            faceNodes = (int[])f.next();
            setFaceCoordFun(faceNodes);

            // Subdivide element face into triangles
            // using local coordinates
            fs.subdivideFace(edgeDiv[face]);

            setFaceVertices(faceNodes);

            // Add triangle coordinates, normals and texture
            // coordinates to surface arrays
            for (int t=0; t<fs.nTrigs; t++) {
                for (int k =0; k <3; k++) {
                    int ind = fs.trigs[t][k];
                    for (int i=0; i<3; i++) {
                        xyzSurface[3*nVertices+i] =
                                (float) xyzFacePoints[3*ind+i];
                        norSurface[3*nVertices+i] =
                                (float) norFacePoints[3*ind+i];
                    }
                    texSurface[2*nVertices] =
                            (float) texFacePoints[ind];
                    nVertices++;
                }
            }
        }
    }

    // Compute global coordinates, normals and texture
    // coordinates for face triangle vertices.
    private void setFaceVertices(int[] faceNodes) {

        double e[][] = new double[2][3], en[] = new double[3];

        for (int iv = 0; iv < fs.nFacePoints; iv++) {
            // Shape functions and their derivatives for
            // a face of 3D hexahedral quadratic element
            ShapeQuad3D.shapeDerivFace(fs.xi[iv], fs.et[iv],
                    faceNodes, an, deriv);
            for (int j = 0; j < 3; j++) {
                double s = 0;
                for (int i = 0; i < 8; i++)
                    s += an[i]*xyzFace[i][j];
                xyzFacePoints[3*iv + j] = (float) s;
            }
            // Tangents e to the local coordinates
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    double s = 0;
                    for (int k = 0; k < 8; k++)
                        s += deriv[k][i]*xyzFace[k][j];
                    e[i][j] = s;
                }
            }
            // Normal vector en
            en[0] = (e[0][1]*e[1][2] - e[1][1]*e[0][2]);
            en[1] = (e[0][2]*e[1][0] - e[1][2]*e[0][0]);
            en[2] = (e[0][0]*e[1][1] - e[1][0]*e[0][1]);
            double s = 1.0/Math.sqrt(en[0]*en[0] +
                    en[1]*en[1] + en[2]*en[2]);
            for (int i = 0; i < 3; i++)
                norFacePoints[3*iv + i] = (float) (en[i]*s);

            if (VisData.drawContours) {
                double f = 0;
                for (int i = 0; i < 8; i++)
                    f += an[i]*funFace[i];
                double t = (f - fmin)/(fmax - fmin);
                if (t < 0.003) t = 0.003;
                if (t > 0.997) t = 0.997;
                texFacePoints[iv] = (float) t;
            }
        }
    }

    // Create TriangleArray containing vertex coordinates,
    // normals and possibly texture coordinates for contours.
    // returns  TriangleArray
    TriangleArray getModelTriangles() {

        int vFormat = TriangleArray.COORDINATES |
                     TriangleArray.NORMALS |
                     TriangleArray.BY_REFERENCE;
        if (VisData.drawContours) vFormat = vFormat |
                TriangleArray.TEXTURE_COORDINATE_2;

        TriangleArray triangleArray =
                new TriangleArray(nVertices, vFormat);

        triangleArray.setCoordRefFloat(xyzSurface);
        triangleArray.setNormalRefFloat(norSurface);
        if (VisData.drawContours)
            triangleArray.setTexCoordRefFloat(0, texSurface);
        
        return triangleArray;
    }

    // Create array of lines for drawing element edges.
    // returns  line array for element edges at the surface
    LineArray getModelLines() {

        float x[] = new float[3];
        double xys[][] = new double[3][3];
        double an[] = new double[3];
        int[] divs = new int[nEdges];
        int nDivTotal = 0;

        int ii = 0;
        ListIterator e = listEdges.listIterator(0);

        for (int edge=0; edge<nEdges; edge++) {
            int edgeNodes[] = (int[])e.next();
            for (int k = 0; k < 3; k++) {
                for (int n = 0; n < fem.nDim; n++)
                    xys[k][n] = fem.getNodeCoord(
                            edgeNodes[k]-1,n);
            }
            int n = fs.numberOfEdgeDivisions(xys, null,
                    deltaf, false, 0, 1, 2);
            divs[edge] = n;
            nDivTotal += n;
        }
        LineArray lineArray =
            new LineArray(nDivTotal*2, LineArray.COORDINATES);

        e = listEdges.listIterator(0);

        for (int edge=0; edge<nEdges; edge++) {
            int edgeNodes[] = (int[])e.next();
            for (int k = 0; k < 3; k++)
                for (int n = 0; n < fem.nDim; n++) xys[k][n] =
                            fem.getNodeCoord(edgeNodes[k]-1,n);

            int ndiv = divs[edge];
            double dxi = 2.0/ndiv;
            for (int i=0; i<3; i++) x[i] = (float) xys[0][i];

            for (int k = 1; k <= ndiv; k++) {
                lineArray.setCoordinate(ii++, x);
                double xi = -1 + k*dxi;
                // Quadratic shape functions
                an[0] = -0.5*xi*(1 - xi);
                an[1] =  1 - xi*xi;
                an[2] =  0.5*xi*(1 + xi);

                for (int i = 0; i < 3; i++) {
                    double s = 0;
                    for (int j = 0; j < 3; j++)
                        s += xys[j][i]*an[j];
                    x[i] = (float) s;
                }
                lineArray.setCoordinate(ii++, x);
            }
        }
        return lineArray;
    }

    // Create array of nodal points.
    // returns  point array containing nodes at the surface
    PointArray getModelPoints() {

        float x[] = new float[3];
        PointArray pointArray =
            new PointArray(nsNodes*3, PointArray.COORDINATES);
        int ii = 0;
        for (int node = 0; node < sNodes.length; node++) {
            if (sNodes[node] > 0) {
                for (int i = 0; i < fem.nDim; i++)
                    x[i] = (float) fem.getNodeCoord(node, i);
                pointArray.setCoordinate(ii++, x);
            }
        }
        return pointArray;
    }

}