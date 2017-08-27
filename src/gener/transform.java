package gener;

import model.*;
import util.*;
import fea.*;

// Make transformations for a specified model.
// Input: modelName - name of the finite element model;
// [translate axis value] - translate axis coordinates;
// [scale axis value] - scale axis coordinates;
// [rotate axis value] - rotate around axis by angle (degrees);
// [mirror axis value] - mirror along axis around axis value
public class transform {

    private FeModel m;
    enum vars {
        translate, scale, rotate, mirror, end
    }

    enum elementMirror {
        quad8 {int permutation(int i) {
            int[] p = {1,8,7,6,5,4,3,2};
            return p[i]-1;}
        },
        hex20 {int permutation(int i) {
            int[] p = {1,8,7,6,5,4,3,2, 9,12,11,10,
                       13,20,19,18,17,16,15,14};
            return p[i]-1;}
        };
        abstract int permutation(int i);
    }

    private vars opName;
    private char axis;
    private double value;

    public transform() {

        String modelName = Jmgen.RD.next();
        Jmgen.PR.printf("Transform: %s\n", modelName);

        if (Jmgen.blocks.containsKey(modelName))
            m = (FeModel) Jmgen.blocks.get(modelName);
        else UTIL.errorMsg("No such mesh block: " + modelName);

        while (Jmgen.RD.hasNext()) {
            String name = Jmgen.RD.next().toLowerCase();
            if (name.equals("#")) {
                Jmgen.RD.nextLine(); continue;
            }
            if (name.equals("end")) break;

            try {
                opName = vars.valueOf(name);
            } catch (Exception e) {
                UTIL.errorMsg("Operation name is not found: "
                        + name);
            }

            String Axis = Jmgen.RD.next().toLowerCase();
            axis = Axis.charAt(0);
            if (axis!='x' && axis!='y' && axis!='z')
                UTIL.errorMsg("Incorrect axis: " + axis);
            value = Jmgen.RD.readDouble();
            Jmgen.PR.printf("%10s  %s  %10.4f\n",
                    opName, Axis, value);

            switch (opName) {
                case translate:   doTranslate();
                    break;
                case scale:       doScale();
                    break;
                case rotate:      doRotate();
                    break;
                case mirror:      doMirror();
                    break;
            }
        }
        Jmgen.PR.printf("Mesh " + modelName +
                ": nEl = %d  nNod = %d\n", m.nEl, m.nNod);
    }

    private static int getIntAxis(char axis) {
        int iAxis = 0;
        if (axis=='y') iAxis = 1;
        else if (axis=='z') iAxis = 2;
        return iAxis;
    }

    private void doTranslate() {

        if (m.nDim == 2 && axis == 'z') return;
        int iax = getIntAxis(axis);

        for (int i=0; i<m.nNod; i++)
            m.setNodeCoord(i, iax,
                    m.getNodeCoord(i, iax) + value);
    }

    private void doScale() {

        if (m.nDim == 2 && axis == 'z') return;
        int iax = getIntAxis(axis);

        for (int i=0; i<m.nNod; i++)
            m.setNodeCoord(i, iax,
                    m.getNodeCoord(i,iax)* value);
    }

    private void doRotate() {

        if (m.nDim == 2 && (axis=='x' || axis=='y')) return;

        double sina = Math.sin(Math.toRadians(value));
        double cosa = Math.cos(Math.toRadians(value));
        double a[][] = new double[3][3];
        double x[] = new double[3];

        if (axis=='x') {
            a[0][0]= 1;    a[0][1]= 0;    a[0][2]=0;
            a[1][0]= 0;    a[1][1]= cosa; a[1][2]=-sina;
            a[2][0]= 0;    a[2][1]= sina; a[2][2]= cosa;
        }
        else if (axis=='y') {
            a[0][0]= cosa; a[0][1]= 0;    a[0][2]= sina;
            a[1][0]= 0;    a[1][1]= 1;    a[1][2]= 0;
            a[2][0]=-sina; a[2][1]= 0;    a[2][2]= cosa;
        }
        else {  // around z
            a[0][0]= cosa; a[0][1]=-sina; a[0][2]= 0;
            a[1][0]= sina; a[1][1]= cosa; a[1][2]= 0;
            a[2][0]= 0;    a[2][1]= 0;    a[2][2]= 1;
        }

        for (int inod=0; inod<m.nNod; inod++) {
            for (int j=0; j<m.nDim; j++)
                x[j] = m.getNodeCoord(inod,j);
            for (int i=0; i<m.nDim; i++) {
                double s = 0;
                for (int j=0; j<m.nDim; j++) s += a[i][j]*x[j];
                m.setNodeCoord(inod, i, s);
            }
        }
    }

    private void doMirror() {

        if (m.nDim == 2 && axis == 'z') return;
        int iax = getIntAxis(axis);

        // Mirror nodal coordinates
        for (int i=0; i<m.nNod; i++)
            m.setNodeCoord(
                    i, iax, -m.getNodeCoord(i,iax)+2*value);

        // Change order of element connectivities
        for (int e=0; e<m.nEl; e++) {
            elementMirror em = null;
            try {
                em = elementMirror.valueOf(m.elems[e].name);
            } catch (Exception el) {
                UTIL.errorMsg("Mirror: element not supported "
                        + m.elems[e].name);
            }

            int nind = m.elems[e].ind.length;
            int[] ind = new int[nind];
            for (int i=0; i<nind; i++)
                ind[em.permutation(i)] = m.elems[e].ind[i];
            m.elems[e].setElemConnectivities(ind);
        }
    }

}
