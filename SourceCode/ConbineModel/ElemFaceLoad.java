package model;

// Element face load
public class ElemFaceLoad {
    // Element number (start with 0)
    public int iel;
    // Direction: 1-x, 2-y, 3-z, 0-normal
    public int direction;
    public int[] faceNodes;
    public double[] forceAtNodes;

    ElemFaceLoad(int iel, int nFaceNodes, int direction,
                 int[] faceNodes, double[] forceAtNodes) {
        this.iel = iel;
        this.direction = direction;
        this.faceNodes = new int[nFaceNodes];
        this.forceAtNodes = new double[nFaceNodes];

        for (int i=0; i<nFaceNodes; i++) {
            this.faceNodes[i] = faceNodes[i];
            this.forceAtNodes[i] = forceAtNodes[i];
        }
    }

    ElemFaceLoad(int iel, int nFaceNodes, int direction,
                 int[] faceNodes, double force) {
        this.iel = iel;
        this.direction = direction;
        this.faceNodes = new int[nFaceNodes];
        this.forceAtNodes = new double[nFaceNodes];

        for (int i=0; i<nFaceNodes; i++) {
            this.faceNodes[i] = faceNodes[i];
            this.forceAtNodes[i] = force;
        }
    }

    // Rearrange surface load (faceNodes[] and ForcesAtNodes[])
    // according to order in element faces.
    // faces - local numbers (from zero) of element faces,
    // ind - element connectivities.
    // returns  loaded face number or -1 if no match
    //  between ind[] and load data.
    public int rearrange(int[][] faces, int[] ind) {

        int perm[] = new int[8];
        double fw[] = new double[8];
        int loadedFace = -1;

        FACE: for (int iface=0; iface<faces.length; iface++) {
            int nNodes = faces[iface].length;
            for (int inod = 0; inod < nNodes; inod++)
                perm[inod] = -1;
            for (int inod = 0; inod < nNodes; inod++) {
                int iGlob = ind[faces[iface][inod]];
                if (iGlob > 0) {
                    boolean EQ = false;
                    int i;
                    for (i = 0; i < nNodes; i++)
                        if (faceNodes[i] == iGlob) {
                            EQ = true;
                            break;
                        }
                    if (!EQ) continue FACE;
                    perm[inod] = i;
                }
            }
            loadedFace = iface;
            for (int inod = 0; inod < nNodes; inod++) {
                faceNodes[inod] = ind[faces[iface][inod]];
                fw[inod] = forceAtNodes[inod];
            }
            for (int inod = 0; inod < nNodes; inod++)
                forceAtNodes[inod] =
                    (perm[inod] == -1) ? 0.0 : fw[perm[inod]];
        }
        return loadedFace;
    }

}
