package gener;

import model.*;
import fea.*;
import util.*;
import elem.*;

// Generate mesh of quadratic elements inside a rectangle.
// Input: nx, ny - number of elements along x and y;
//  xs, ys - locations of element boundaries on x and y;
//  [mat] - material name.
public class rectangle {

    private FeModel m;
    enum vars {
        nx, ny, xs, ys, mat, end
    }

    private vars name;

    private int nx, ny;
    String mat="1";
    private double xs[], ys[];

    public rectangle() {
        String modelName = Jmgen.RD.next();
        Jmgen.PR.printf("Rectangle: %s\n", modelName);
        readData();
        printData();
        m = new FeModel(Jmgen.RD, Jmgen.PR);
        generateMesh();
        Jmgen.blocks.put(modelName,m);
        Jmgen.PR.printf("Mesh " + modelName +
                ": nEl = %d  nNod = %d\n", m.nEl, m.nNod);
    }

    private void readData() {
        String varName, varname;

        while (Jmgen.RD.hasNext()) {

            varName = Jmgen.RD.next();
            varname = varName.toLowerCase();
            if (varName.equals("#")) {
                Jmgen.RD.nextLine(); continue;
            }
            try {
                name = vars.valueOf(varname);
            } catch (Exception e) {
                UTIL.errorMsg("Variable name is not found: "
                        + varName);
            }
            switch (name) {
            case nx:    nx = Jmgen.RD.readInt();
                break;
            case ny:    ny = Jmgen.RD.readInt();
                break;
            case xs:
                xs = new double[nx+1];
                for (int i = 0; i <= nx; i++)
                    xs[i] = Jmgen.RD.readDouble();
                break;
            case ys:
                ys = new double[ny+1];
                for (int i = 0; i <= ny; i++)
                    ys[i] = Jmgen.RD.readDouble();
                break;
            case mat:    mat = Jmgen.RD.next();
                break;
            case end:
                return;
            }
        }
    }

    private void printData() {
        Jmgen.PR.printf(" nx =%5d\n", nx);
        Jmgen.PR.printf(" ny =%5d\n", ny);
        Jmgen.PR.printf(" xs:  ");
        for (int i = 0; i <= nx; i++)
            Jmgen.PR.printf("%7.3f", xs[i]);
        Jmgen.PR.printf("\n ys:  ");
        for (int i = 0; i <= ny; i++)
            Jmgen.PR.printf("%7.3f", ys[i]);
        Jmgen.PR.printf("\n");
    }

    private void generateMesh() {
        int ind[] = new int[8];
        m.nDim = 2;

        // Connectivity array
        m.nEl = nx*ny;
        m.elems = new Element[m.nEl];

        int el = 0;
        for (int iy=0; iy<ny; iy++) {
            for (int ix=0; ix<nx; ix++) {
                m.elems[el] = Element.newElement("quad8");
                int in0 = iy*(3*nx+2) + 2*ix;
                ind[0] = in0 + 1;
                ind[1] = in0 + 2;
                ind[2] = in0 + 3;
                int in1 = iy*(3*nx+2) + 2*nx + 1 + ix + 1;
                ind[3] = in1 + 1;
                ind[7] = in1;
                int in2 = (iy+1)*(3*nx+2) + 2*ix;
                ind[4] = in2 + 3;
                ind[5] = in2 + 2;
                ind[6] = in2 + 1;
                m.elems[el].setElemConnectivities(ind);
                m.elems[el].setElemMaterial(mat);
                el++;
            }
        }

        // Node coordinate array
        m.nNod = (3*nx+2)*ny + 2*nx + 1;
        m.newCoordArray();
        int n = 0;
        for (int iy=0; iy<2*ny+1; iy++) {
            int py = (iy+1)/2;
            for (int ix=0; ix<2*nx+1; ix++) {
                int px = (ix+1)/2;
                if (ix%2==0 && iy%2==0) {
                    m.setNodeCoord(n, 0, xs[px]);
                    m.setNodeCoord(n, 1, ys[py]);
                    n++;
                }
                else if (ix%2==1 && iy%2==0) {
                    m.setNodeCoord(n,0,0.5*(xs[px-1]+xs[px]));
                    m.setNodeCoord(n, 1, ys[py]);
                    n++;
                }
                else if (ix%2==0 && iy%2==1) {
                    m.setNodeCoord(n, 0, xs[px]);
                    m.setNodeCoord(n,1,0.5*(ys[py-1]+ys[py]));
                    n++;
                }
            }
        }
    }

}
