package gener;

import model.*;
import fea.*;
import util.*;
import elem.*;

// Generate 3D mesh of hexahedral 20-node elements by sweeping
//     2D mesh of quadrilateral 8-node elements.
// Input: modelName2 - name of 2D model;
//  modelName3 - name of the resulting 3D model;
//  nlayers - number of element layers in 3D mesh;
//  zlayers - z-distances or angles (deg) for copying 2D mesh;
//  [rotate=Y/N] - rotate mesh around y-axis,
//      otherwize translate along z.
public class sweep {

    // 2D source model
    private FeModel m2;
    // 3D resulting model
    private FeModel m3;

    enum vars {
        nlayers, zlayers, rotate, end
    }

    private vars name;
    private int nlayers;
    boolean rotate = false;
    private double zlayers[];
    // Node types for 2D mesh,
    int[] nodeType2;
    // nodeNum2[i] is a number of i-th 2D node in 3D mesh
    int[] nodeNum2;

    public sweep() {

        String modelName2 = Jmgen.RD.next();
        String modelName3 = Jmgen.RD.next();
        Jmgen.PR.printf(
                "Sweep: %s %s\n", modelName2, modelName3);
        readData();
        printData();

        if (Jmgen.blocks.containsKey(modelName2))
            m2 = (FeModel) Jmgen.blocks.get(modelName2);
        else
            UTIL.errorMsg("No such mesh block: " + modelName2);

        m3 = new FeModel(Jmgen.RD, Jmgen.PR);
        m3.nDim = 3;
        m3.nEl = m2.nEl*nlayers;
        m3.nNod = nodeTypesNumbers2D();
        elementConnectivities3D();
        nodeCoordinates3D();
        Jmgen.blocks.put(modelName3, m3);
        Jmgen.PR.printf("Mesh " + modelName3 +
                ": nEl = %d  nNod = %d\n", m3.nEl, m3.nNod);
    }

    private void readData() {
        String varName, varname;

        while (Jmgen.RD.hasNext()) {

            varName = Jmgen.RD.next();
            varname = varName.toLowerCase();
            if (varName.equals("#")) {
                Jmgen.RD.nextLine();
                continue;
            }
            try {
                name = vars.valueOf(varname);
            } catch (Exception e) {
                UTIL.errorMsg("Variable name is not found: "
                        + varName);
            }
            switch (name) {
            case nlayers:
                nlayers = Jmgen.RD.readInt();
                break;
            case rotate:
                rotate =
                    (Jmgen.RD.next().equalsIgnoreCase("Y"));
                break;
            case zlayers:
                zlayers = new double[2*nlayers + 1];
                for (int i = 0; i < 2*nlayers + 1; i += 2)
                    zlayers[i] = Jmgen.RD.readDouble();
                // Interpolation for 3D midside nodes
                for (int i = 1; i < 2*nlayers; i += 2)
                    zlayers[i] =
                            0.5*(zlayers[i-1] + zlayers[i+1]);
                break;
            case end:
                return;
            }
        }
    }

    private void printData() {
        Jmgen.PR.printf(" nlayers =%5d\n", nlayers);
        Jmgen.PR.printf(
                " rotate  = %s\n", (rotate ? "Y" : "N"));
        Jmgen.PR.printf(" zlay:  ");
        for (int i = 0; i <= 2*nlayers + 1; i += 2)
            Jmgen.PR.printf("%7.3f", zlayers[i]);
        Jmgen.PR.printf("\n");
    }

    // Create arrays nodeType2 and nodeNum2
    // return number of nodes in 3D mesh
    private int nodeTypesNumbers2D() {

        // nodeType2 - node types for 2D mesh,
        //  =0 - midside, =1 - corner/degenerate
        nodeType2 = new int[m2.nNod];
        for (int iel = 0; iel < m2.nEl; iel++) {
            for (int i=0; i<m2.elems[iel].ind.length; i++) {
                int in = m2.elems[iel].ind[i] - 1;
                if (in != -1) {
                    nodeType2[in] = (i + 1)%2;
                }
            }
        }

        // nodeNum2[i] is a number of i-th 2D node in 3D mesh
        nodeNum2 = new int[m2.nNod];
        int node = 1;
        for (int i = 0; i < m2.nNod; i++) {
            nodeNum2[i] = node;
            int dn = (nodeType2[i] == 0) ?
                    nlayers + 1 : 2*nlayers + 1;
            // If node is located on rotation axis Y
            if (rotate && m2.getNodeCoord(i, 0) == 0.0) dn = 1;
            node = node + dn;
        }
        return node - 1;
    }

    private void elementConnectivities3D() {

        m3.elems = new Element[m3.nEl];

        int[] ind2 = new int[8], t3 = new int[8],
                n3 = new int[8], ind3 = new int[20];
        int iel3d = 0;

        for (int iel2d = 0; iel2d < m2.nEl; iel2d++) {
            int nind2 = m2.elems[iel2d].ind.length;
            String mat = m2.elems[iel2d].matName;
            for (int i = 0; i < nind2; i++) {
                int i2 = m2.elems[iel2d].ind[i] - 1;
                t3[i] = nodeType2[i2];
                n3[i] = nodeNum2[i2];
                ind2[i] = i2;
            }

            for (int i3 = 0; i3 < nlayers; i3++) {
                for (int i = 0; i < nind2; i++) {
                    int dn = (t3[i] == 0) ? 1 : 2;
                    int node = n3[i] + i3*dn;
                    int dn2 = dn;
                    int dn1 = 1;
                    // Node at rotation axis
                    if (rotate &&
                            m2.getNodeCoord(ind2[i], 0) == 0) {
                        node = n3[i];
                        dn2 = 0;
                        dn1 = 0;
                    }
                    ind3[i] = node;
                    ind3[i + 12] = node + dn2;
                    if ((i + 1)%2 == 1)
                        ind3[(i+2)/2-1+nind2] = node + dn1;
                }
                m3.elems[iel3d] = Element.newElement("hex20");
                m3.elems[iel3d].setElemConnectivities(ind3);
                m3.elems[iel3d].setElemMaterial(mat);
                iel3d++;
            }
        }
    }

    private void nodeCoordinates3D() {

        m3.newCoordArray();

        for (int i2 = 0; i2 < m2.nNod; i2++) {
            int step = (nodeType2[i2] == 0) ? 2 : 1;
            int n = 2*nlayers + 1;
            // Node at rotation axis
            if (rotate && m2.getNodeCoord(i2, 0) == 0.0) n = 1;
            int nodeNum = nodeNum2[i2] - 1;
            for (int i = 0; i < n; i += step) {
                double z = zlayers[i];
                double r = m2.getNodeCoord(i2, 0);
                double y = m2.getNodeCoord(i2, 1);
                if (rotate) {
                    // Sweeping by rotation around Y
                    double fi = Math.toRadians(zlayers[i]);
                    double[] w =
                        {r*Math.cos(fi), y, r*Math.sin(fi)};
                    m3.setNodeCoords(nodeNum, w);
                }
                else {
                    // Sweeping by translation along Z
                    double[] w = {r, y, z};
                    m3.setNodeCoords(nodeNum, w);
                }
                nodeNum++;
            }
        }
    }

}
