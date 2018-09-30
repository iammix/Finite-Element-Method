package Generator;

import FiniteElementAnalysis.*;
import ConbineModel.*;
import Elements.*;
import Utilities.*;

// Paste two meshes.
// Input: modelNameA - name of first mesh to be pasted;
//  modelNameB - name of second mesh to be pasted;
//  modelNameC - name of resulting mesh;
//  [eps] - coordinate tolerance for joining nodes.
public class connect {

    private FeModel mA, mB;
    private double eps = 0.0001;
    private int nConnected;
    private int newNodesB[];

    public connect() {

        String modelNameA = Jmgen.RD.next();
        String modelNameB = Jmgen.RD.next();
        String modelNameC = Jmgen.RD.next();

        Jmgen.PR.printf("Connect: %s + %s -> %s\n",
                modelNameA, modelNameB, modelNameC);

        readData();

        if (Jmgen.blocks.containsKey(modelNameA))
            mA = (FeModel) Jmgen.blocks.get(modelNameA);
        else UTIL.errorMsg(
                "No such mesh block: " + modelNameA);
        if (Jmgen.blocks.containsKey(modelNameB))
            mB = (FeModel) Jmgen.blocks.get(modelNameB);
        else UTIL.errorMsg(
                "No such mesh block: " + modelNameB);
        if (mA.nDim != mB.nDim) UTIL.errorMsg(
                "Models with different nDim");

        findCoincidentNodes();
        FeModel mC = pasteModels();

        Jmgen.blocks.put(modelNameC, mC);
        Jmgen.PR.printf(" %d node pairs connected\n",
                nConnected);
        Jmgen.PR.printf("Mesh " + modelNameC +
                ": nEl = %d  nNod = %d\n", mC.nEl, mC.nNod);
    }

    private void readData() {
        while (Jmgen.RD.hasNext()) {
            String name = Jmgen.RD.next().toLowerCase();
            if (name.equals("#")) {
                Jmgen.RD.nextLine();
                continue;
            }
            if (name.equals("eps"))
                eps = Jmgen.RD.readDouble();
            else if (name.equals("end")) break;
            else UTIL.errorMsg("Unexpected data: " + name);
        }
        Jmgen.PR.printf(
            " Coordinate error tolerance eps = %10.3e\n", eps);
    }

    // Find coincident nodes in models mA and mB,
    // generate new node numbers for model mB
    private void findCoincidentNodes() {

        newNodesB = new int[mB.nNod];
        int ndim = mA.nDim;
        for (int i = 0; i < mB.nNod; i++) newNodesB[i] = -1;

        // Register coincident nodes of mesh B
        //     in array newNodesB
        for (int ia = 0; ia < mA.nNod; ia++) {
            double xyA[] = mA.getNodeCoords(ia);
            B:
            for (int ib = 0; ib < mB.nNod; ib++) {
                for (int j = 0; j < ndim; j++) {
                    if (Math.abs(xyA[j]-mB.getNodeCoord(ib,j))
                            > eps) continue B;
                }
                newNodesB[ib] = ia;
            }
        }
        nConnected = 0;
        int n = mA.nNod;

        // New node numbers for nodes of model mB
        for (int i = 0; i < mB.nNod; i++) {
            if (newNodesB[i] == -1) newNodesB[i] = n++;
            else nConnected++;
        }
    }

    // Paste two meshes.
    // Nodes and elements of the first mesh
    // are first in the resulting mesh.
    // returns   resulting mesh after pasting.
    private FeModel pasteModels() {

        FeModel mC = new FeModel(Jmgen.RD, Jmgen.PR);
        mC.nDim = mA.nDim;

        // nodal coordinates of model mC
        mC.nNod = mA.nNod + mB.nNod - nConnected;
        mC.newCoordArray();
        // Copy nodes of model mA
        for (int i = 0; i < mA.nNod; i++)
            mC.setNodeCoords(i, mA.getNodeCoords(i));
        // Add nodes of model mB
        for (int i = 0; i < mB.nNod; i++)
            mC.setNodeCoords(
                    newNodesB[i], mB.getNodeCoords(i));

        // Element connectivities of model mC
        mC.nEl = mA.nEl + mB.nEl;
        mC.elems = new Element[mC.nEl];
        // Copy elements of model mA
        for (int el = 0; el < mA.nEl; el++) {
            mC.elems[el] =
                    Element.newElement(mA.elems[el].name);
            mC.elems[el].setElemConnectivities(
                    mA.elems[el].ind);
            mC.elems[el].matName = mA.elems[el].matName;
        }
        // Add elements of mB with renumbered connectivities
        for (int el = 0; el < mB.nEl; el++) {
            mC.elems[mA.nEl + el] =
                    Element.newElement(mB.elems[el].name);
            int indel[] = new int[mB.elems[el].ind.length];
            for (int i = 0; i < mB.elems[el].ind.length; i++)
                indel[i] = newNodesB[mB.elems[el].ind[i]-1]+1;
            mC.elems[mA.nEl+el].setElemConnectivities(indel);
            mC.elems[mA.nEl+el].matName = mB.elems[el].matName;
        }
        return mC;
    }

}
