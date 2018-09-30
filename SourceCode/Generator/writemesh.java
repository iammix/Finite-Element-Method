package Generator;

import ConbineModel.*;
import FiniteElementAnalysis.*;
import Utilities.*;

import java.io.PrintWriter;

// Write mesh to file.
// Input:  modelName - name of the finite element model;
//  fileName - name of the file.
public class writemesh {

    FeModel m;

    public writemesh() {

        String modelName = Jmgen.RD.next();
        String fileName = Jmgen.RD.next();
        Jmgen.PR.printf("WriteMesh: %s    %s\n",
                modelName, fileName);

        PrintWriter WR =
                new FePrintWriter().getPrinter(fileName);

        if (Jmgen.blocks.containsKey(modelName))
            m = (FeModel) Jmgen.blocks.get(modelName);
        else UTIL.errorMsg("No such mesh block: " + modelName);

        WR.printf("# Model name: %s\n", modelName);
        WR.printf("nNod = %5d\n", m.nNod);
        WR.printf("nEl = %5d\n", m.nEl);
        WR.printf("nDim = %5d\n", m.nDim);

        WR.printf("nodCoord\n");
        for (int i = 0; i < m.nNod; i++) {
            for (int j = 0; j < m.nDim; j++)
                WR.printf("%20.9f", m.getNodeCoord(i, j));
            WR.printf("\n");
        }

        WR.printf("\nelCon");
        for (int iel = 0; iel < m.nEl; iel++) {
            WR.printf("\n%s %6s", m.elems[iel].name,
                    m.elems[iel].matName);
            int nind = m.elems[iel].ind.length;
            for (int i = 0; i < nind; i++)
                WR.printf("%6d", m.elems[iel].ind[i]);
        }
        WR.printf("\n\nend\n");
        WR.close();
        Jmgen.PR.printf("Mesh " + modelName +
                ": nEl = %d  nNod = %d\n", m.nEl, m.nNod);
    }

}
