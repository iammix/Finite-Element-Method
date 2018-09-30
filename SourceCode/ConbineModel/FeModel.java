package ConbineModel;

import Elements.*;
import Materials.*;
import Utilities.*;
import Solvers.*;

import java.io.PrintWriter;
import java.util.ListIterator;

// Description of the finite element model
public class FeModel extends FeModelData {

    static private int elCon[] = new int[20];
    static private double box[][] = new double[2][3];
    ListIterator it;

    // Construct finite element model.
    // RD - data scanner, PR - print writer.
    public FeModel(FeScanner RD, PrintWriter PR) {
        FeModelData.RD = RD;
        FeModelData.PR = PR;
    }

    // Read data for a finite element model
    public void readData() {
        readDataFile(RD);
    }

    private void readDataFile(FeScanner es) {

        vars name = null;
        String s;
        Material mat;
        it = defDs.listIterator(0);

        while (es.hasNext()) {
            varName = es.next();
            String varname = varName.toLowerCase();
            if (varName.equals("#")) {es.nextLine(); continue;}
            try {
                name = vars.valueOf(varname);
            } catch (Exception e) {
                UTIL.errorMsg(
                    "Variable name is not found: "+varName);
            }

            switch (name) {

            case nel:    nEl = es.readInt();
                break;

            case nnod:   nNod = es.readInt();
                break;

            case ndim:   nDim = es.readInt();
                nDf = nDim;
                break;

            case stressstate:
                s = es.next().toLowerCase();
                try {
                    stressState = StrStates.valueOf(s);
                } catch (Exception e) {
                    UTIL.errorMsg(
                        "stressState has forbidden value: "+s);
                }
                if (stressState != StrStates.threed)
                      nDim = nDf = 2;
                else  nDim = nDf = 3;
                break;

            case physlaw:
                s = es.next().toLowerCase();
                try {
                    physLaw = PhysLaws.valueOf(s);
                } catch (Exception e) {
                    UTIL.errorMsg(
                            "physLaw has forbidden value: "+s);
                }
                break;

            case solver:
                s = es.next().toLowerCase();
                try {
                    Solver.solver = Solver.Solvers.valueOf(s);
                } catch (Exception e) {
                    UTIL.errorMsg(
                            "solver has forbidden value: "+s);
                }
                break;

            case elcon:
                readElemData(es);
                break;

            case nodcoord:
                if (nNod == 0 || nDim == 0)
                    UTIL.errorMsg("nNod and nDim should be"
                            +" specified before nodCoord");
                nEq = nNod * nDim;
                // Nodal coordinates
                newCoordArray();
                for (int i = 0; i < nNod; i++)
                    for (int j = 0; j < nDim; j++)
                        setNodeCoord(i, j, es.readDouble());
                break;

            case material:
                String matname = es.next();
                mat = Material.newMaterial(physLaw.toString(),
                                      stressState.toString());
                double e = es.readDouble();
                double nu = es.readDouble();
                double alpha = es.readDouble();
                mat.setElasticProp(e, nu, alpha);
                if (physLaw == PhysLaws.elplastic) {
                    double sY = es.readDouble();
                    double km = es.readDouble();
                    double mm = es.readDouble();
                    mat.setPlasticProp(sY, km, mm);
                }
                materials.put(matname, mat);
                break;

            case constrdispl:
                readConstrDisplacements(es);
                break;

            case boxconstrdispl:
                createBoxConstrDisplacements(es);
                break;

            case thermalloading:
                s = es.next();
                if (s.toLowerCase().equals("y"))
                    thermalLoading = true;
                else if (s.toLowerCase().equals("n"))
                    thermalLoading = false;
                else
                    UTIL.errorMsg("thermalLoading should be"
                            + " y/n. Specified: " + s);
                break;

            case includefile:
                s = es.next().toLowerCase();
                FeScanner R = new FeScanner(s);
                readDataFile(R);
                break;

            case end:  return;
            }
        }
    }

    // Read element type, material and connectivities
    // for all elements
    private void readElemData(FeScanner es) {

        if (nEl == 0) UTIL.errorMsg (
                "nEl should be defined before elCon");
        elems = new Element[nEl];

        for (int iel = 0; iel < nEl; iel++) {
            // Element type
            String s = es.next().toLowerCase();
            elems[iel] = Element.newElement(s);
            // Element material
            String elMat = es.next();
            elems[iel].setElemMaterial(elMat);
            // Element connectivities
            int nind = elems[iel].ind.length;
            for (int i = 0; i < nind; i++) {
                elCon[i] = es.readInt();
            }
            elems[iel].setElemConnectivities(elCon,nind);
        }
    }

    // Read data for specified constrained displacements
    private void readConstrDisplacements(FeScanner es) {
        String s = es.next().toLowerCase();
        int idf = UTIL.direction(s);
        if (idf == -1) UTIL.errorMsg("constrDispl direction"+
                " should be x/y/z. Specified:"+s);
        if (!es.hasNextDouble())
            UTIL.errorMsg("constrDispl value is not a double: "
                    +es.next());
        double vd = es.nextDouble();
        it = es.readNumberList(it, idf, nDim, vd);
    }

    // Create data for constrained displacements
    // specified inside a box
    private void createBoxConstrDisplacements(FeScanner es) {
        String s = es.next().toLowerCase();
        int idf = UTIL.direction(s);
        if (idf == -1)
            UTIL.errorMsg("boxConstrDispl direction should be"
                    +" x/y/z. Specified:"+s);
        if (!es.hasNextDouble())
            UTIL.errorMsg("boxConstrDispl value is not"
                    + " a double: " + es.next());
        double vd = es.nextDouble();
        for (int i = 0; i < 2; i++)
            for (int j=0; j<nDim; j++)
                box[i][j] = es.readDouble();
        node: for (int i = 0; i < nNod; i++) {
            for (int j = 0; j < nDim; j++) {
                double x = getNodeCoord(i,j);
                if (x<box[0][j] || x>box[1][j]) continue node;
            }
            it.add(new Dof(nDim *i + idf, vd));
        }
    }

}
