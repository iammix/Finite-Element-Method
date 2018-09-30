package Generator;

import FiniteElementAnalysis.*;
import ConbineModel.*;
import Elements.*;
import Utilities.*;

// Copy model.
// Input: modelNameA - name of the model to be copied;
//  modelNameB - name of the resulting model.
public class copy {

    private FeModel mA;

    public copy() {

        String modelNameA = Jmgen.RD.next();
        String modelNameB = Jmgen.RD.next();
        Jmgen.PR.printf("Copy: %s -> %s\n",
                modelNameA, modelNameB);
        if (modelNameA.equals(modelNameB)) return;
        if (Jmgen.blocks.containsKey(modelNameA))
            mA = (FeModel) Jmgen.blocks.get(modelNameA);
        else
            UTIL.errorMsg("No such mesh block: " + modelNameA);
        FeModel mB = copyMesh();
        Jmgen.blocks.put(modelNameB, mB);
        Jmgen.PR.printf("Mesh " + modelNameB +
                ": nEl = %d  nNod = %d\n", mB.nEl, mB.nNod);
    }

    private FeModel copyMesh() {

        FeModel mB = new FeModel(Jmgen.RD, Jmgen.PR);
        mB.nDim = mA.nDim;

        mB.nNod = mA.nNod;
        mB.newCoordArray();
        for (int i = 0; i < mB.nNod; i++) {
            for (int j = 0; j < mB.nDim; j++)
                mB.setNodeCoords(i, mA.getNodeCoords(i));
        }

        mB.nEl = mA.nEl;
        mB.elems = new Element[mB.nEl];
        for (int el = 0; el < mB.nEl; el++) {
            mB.elems[el] = Element.newElement(
                    mA.elems[el].name);
            mB.elems[el].setElemConnectivities(
                    mA.elems[el].ind);
            mB.elems[el].matName = mA.elems[el].matName;
        }

        return mB;
    }

}
