package Generator;

import ConbineModel.*;
import FiniteElementAnalysis.Jmgen;
import Utilities.FeScanner;

// Read mesh data from text file.
// Input: modelName - name of the finite element model;
// fileName - name of the file.
public class readmesh {

    public readmesh() {

        String modelName = Jmgen.RD.next();
        String fileName = Jmgen.RD.next();
        Jmgen.PR.printf("ReadMesh:  %s    %s\n",
                modelName, fileName);
        FeScanner RD = new FeScanner(fileName);
        FeModel m = new FeModel(RD, Jmgen.PR);
        m.readData();
        Jmgen.blocks.put(modelName, m);
        Jmgen.PR.printf("Mesh " + modelName +
                ": nEl = %d  nNod = %d\n", m.nEl, m.nNod);
    }

}
