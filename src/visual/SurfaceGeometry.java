package visual;

import model.*;

import java.util.*;

// Geometry: surface faces, edges and nodes
class SurfaceGeometry {

    FeModel fem;

    // Numbers of surface element faces, edges and nodes
    int nFaces, nEdges, nsNodes;
    // Surface element faces and edges, surface nodes
    LinkedList listFaces;
    LinkedList listEdges;
    int sNodes[];

    double fun[], fmin, fmax, deltaf;
    private static double xyzmin[] = new double[3];
    private static double xyzmax[] = new double[3];
    static double sizeMax;

    SurfaceGeometry() {

        fem = VisData.fem;
        listFaces = new LinkedList();
        listEdges = new LinkedList();
        sNodes = new int[fem.nNod];

        // Create element faces located at the surface
        createFaces();
        nFaces = listFaces.size();

        // Create element edges located at the surface
        createEdges();
        nEdges = listEdges.size();

        // Create nodes located at the surface
        createNodes();

        if (VisData.drawContours) {
            fun = new double[fem.nNod];
            ResultAtNodes ran = new ResultAtNodes(this, fem);
            ran.setParmAtNodes(VisData.parm, VisData.displ);
        }

        modifyNodeCoordinates();
    }

    // Create linked list listFaces containing element faces
    // located on the model surface. 2D case: element = face
    void createFaces() {

        if (fem.nDim == 3) {  // 3D mesh
            for (int iel = 0; iel < fem.nEl; iel++) {
                int elemFaces[][]
                        = fem.elems[iel].getElemFaces();
                for (int[] elemFace : elemFaces) {
                    int nNodes = elemFace.length;
                    int[] faceNodes = new int[nNodes];
                    for (int i = 0; i < nNodes; i++) {
                        faceNodes[i]
                            = fem.elems[iel].ind[elemFace[i]];
                    }
                    // Zero area degenerated 8-node face
                    if (nNodes == 8 &&
                            (faceNodes[3] == faceNodes[7] ||
                             faceNodes[1] == faceNodes[5]))
                        continue;
                    ListIterator f = listFaces.listIterator(0);
                    boolean faceFound = false;
                    while (f.hasNext()) {
                        int[] faceNodesA = (int[]) f.next();
                        if (equalFaces(faceNodes,faceNodesA)) {
                            f.remove();
                            faceFound = true;
                            break;
                        }
                    }
                    if (!faceFound) f.add(faceNodes);
                }
            }
        }
        else {  // 2D - faces = elements
            ListIterator f = listFaces.listIterator(0);
            for (int iel = 0; iel < fem.nEl; iel++) {
                f.add(fem.elems[iel].ind);
            }
        }
    }

    // Compare two element faces.
    // Surface has 8 or 4 nodes, corners are compared.
    // f1 - first face connectivities.
    // f2 - second face connectivities.
    // returns  true if faces are same.
    boolean equalFaces(int[] f1, int[] f2) {

        // Quadratic elements or linear elements
        int step = (f1.length > 4) ? 2 : 1;

        for (int j = 0; j < f1.length; j += step) {
            int n1 = f1[j];
            boolean nodeFound = false;
            for (int i = 0; i < f2.length; i += step) {
                if (f2[i] == n1) {
                    nodeFound = true;
                    break;
                }
            }
            if (!nodeFound) return false;
        }
        return true;
    }

    // Create linked list listEdges containing element edges
    // located on the model surface
    void createEdges() {

        for (int iFace = 0; iFace < nFaces; iFace++) {

            int faceNodes[] = (int[]) listFaces.get(iFace);
            int nFaceNodes = faceNodes.length;
            int step = (nFaceNodes > 4) ? 2 : 1;

            for (int inod=0; inod < nFaceNodes; inod += step) {
                int[] edgeNodes = new int[step + 1];
                for (int i = inod, k = 0; i <= inod+step;
                     i++, k++)
                    edgeNodes[k] = faceNodes[i%nFaceNodes];

                ListIterator ea = listEdges.listIterator(0);
                boolean edgeFound = false;
                while (ea.hasNext()) {
                    int[] edgeNodesA = (int[]) ea.next();
                    if (equalEdges(edgeNodes, edgeNodesA)) {
                        edgeFound = true;
                        break;
                    }
                }
                if (!edgeFound) ea.add(edgeNodes);
            }
        }
    }

    // Compare two element edges.
    // e1 - first edge connectivities.
    // e2 - second edge connectivities.
    // returns  true if edges have same node numbers at ends
    boolean equalEdges(int[] e1, int[] e2) {

        int len = e1.length - 1;
        return (e1[0] == e2[0] && e1[len] == e2[len]) ||
                (e1[0] == e2[len] && e1[len] == e2[0]);
    }

    // Fill out array of surface nodes sNodes (0/1).
    void createNodes() {

        for (int i = 0; i < sNodes.length; i++) sNodes[i] = 0;

        ListIterator e = listEdges.listIterator();

        for (int iEdge = 0; iEdge < nEdges; iEdge++) {
            int edgeNodes[] = (int[]) e.next();
            int nEdgeNodes = edgeNodes.length;
            for (int i = 0; i < nEdgeNodes; i++)
                sNodes[edgeNodes[i] - 1] = 1;
        }
        nsNodes = 0;
        for (int sNode : sNodes)
            if (sNode > 0) nsNodes++;
    }

    // Add scaled displacements to nodal coordinates and
    //  center finite element mesh
    void modifyNodeCoordinates() {

        // Deformed shape: add scaled displacements
        // to nodal coordinates
        if (VisData.showDeformShape) {
            setBoundingBox();
            double displMax = 0;
            for (int i = 0; i < fem.nNod; i++) {
                double d = 0;
                for (int j = 0; j < fem.nDim; j++) {
                    double s =  VisData.displ[i*fem.nDim+j];
                    d += s*s;
                }
                displMax = Math.max(d, displMax);
            }
            displMax = Math.sqrt(displMax);
            // Scale for visualization of deformed shape
            double scaleD =
                    sizeMax*VisData.deformScale/displMax;
            for (int i = 0; i < fem.nNod; i++) {
                for (int j = 0; j < fem.nDim; j++)
                    fem.setNodeCoord(i, j,
                        fem.getNodeCoord(i, j) +
                        scaleD*VisData.displ[i*fem.nDim+j]);
            }
        }

        setBoundingBox();
        // Translate JFEM model to have the bounding
        //  box center at (0,0,0).
        double xyzC[] = new double[3];
        for (int j = 0; j < 3; j++)
            xyzC[j] = 0.5*(xyzmin[j] + xyzmax[j]);
        for (int i = 0; i < fem.nNod; i++)
            for (int j = 0; j < fem.nDim; j++)
                fem.setNodeCoord(i, j,
                    fem.getNodeCoord(i, j) - xyzC[j]);
    }

    // Set min-max values of xyz coordinates of JFEM model
    // xyzmin[] and xyzmax[].
    void setBoundingBox() {

        for (int j = 0; j < fem.nDim; j++) {
            xyzmin[j] = fem.getNodeCoord(0, j);
            xyzmax[j] = fem.getNodeCoord(0, j);
        }
        for (int i = 1; i < fem.nNod; i++) {
            if (sNodes[i] >= 0) {
                for (int j = 0; j < fem.nDim; j++) {
                    double c = fem.getNodeCoord(i, j);
                    xyzmin[j] = Math.min(xyzmin[j], c);
                    xyzmax[j] = Math.max(xyzmax[j], c);
                }
            }
        }
        if (fem.nDim == 2) {
            xyzmin[2] = -0.01;
            xyzmax[2] =  0.01;
        }
        sizeMax = 0;
        for (int i = 0; i < 3; i++) {
            double s = xyzmax[i] - xyzmin[i];
            sizeMax = Math.max(s, sizeMax);
        }
    }

    // Compute scale for the finite element model.
    // returns  scale value.
    double getScale() {

        if (sizeMax > 0) return 0.8/sizeMax;
        else return 1.0;
    }

}