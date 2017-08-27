package model;

import fea.*;
import elem.*;
import util.*;

import java.util.ListIterator;
import java.util.LinkedList;

// Load increment for the finite element model
public class FeLoad extends FeLoadData  {

    // Finite element model
    private static FeModel fem;
    ListIterator itnf, itsf;

    // Construct finite element load.
    // fem - finite element model
    public FeLoad(FeModel fem) {
        FeLoad.fem = fem;
        RD = FeModel.RD;

        spLoad = new double[fem.nEq];
        dpLoad = new double[fem.nEq];
        dhLoad = new double[fem.nEq];
        sDispl = new double[fem.nEq];
        dDispl = new double[fem.nEq];
        RHS    = new double[fem.nEq];

        if (fem.thermalLoading) {
            dtemp = new double[fem.nNod];
        }
    }

    // Read data describing load increment.
    // returns  true if load data has been read
    public boolean readData( ) {

        return readDataFile(RD, true);
    }

    // Read data fragment for load increment.
    // newLoad = true - beginning of new load,
    //         = false - continuation of load.
    // returns  true if load data has been read
    private boolean readDataFile(FeScanner es,
                                 boolean newLoad) {
        if (newLoad) {
            scaleLoad = 0;
            nodForces = new LinkedList();
            itnf = nodForces.listIterator(0);
            surForces = new LinkedList();
            itsf = surForces.listIterator(0);
            if (fem.thermalLoading) {
                for (int i = 0; i < dtemp.length; i++)
                    dtemp[i] = 0.0;
            }
            for (int i=0; i<dDispl.length; i++) dDispl[i] = 0;
        }

        if (!es.hasNext()) return false;  // No load data

        vars name = null;
        String s;

        while (es.hasNext()) {
            String varName = es.next();
            String varNameLower = varName.toLowerCase();
            if (varName.equals("#")) {
                es.nextLine(); continue; }
            try {
                name = vars.valueOf(varNameLower);
            } catch (Exception e) {
                UTIL.errorMsg(
                    "Variable name is not found: " + varName);
            }

            switch (name) {

            case loadstep:
                loadStepName = es.next();
                break;

            case scaleload:
                scaleLoad = es.readDouble();
                break;

            case residtolerance:
                residTolerance = es.readDouble();
                break;

            case maxiternumber:
                maxIterNumber = es.readInt();
                break;

            case nodforce:
                readNodalForces(es);
                break;

            case surforce:
                readSurForces(es);
                break;

            case boxsurforce:
                createBoxSurForces(es);
                break;

            case nodtemp:
                dtemp = new double[fem.nNod];
                for (int i = 0; i < fem.nNod; i++)
                    dtemp[i] = es.readDouble();
                break;

            case includefile:
                s = es.next().toLowerCase();
                FeScanner R = new FeScanner(s);
                readDataFile(R, false);
                break;

            case end:
                return true;
            }
        }
        return true;
    }


    // Read data for specified nodal forces
    private void readNodalForces(FeScanner es) {

        String s = es.next().toLowerCase();
        int idf = UTIL.direction(s);
        if (idf == -1) UTIL.errorMsg("nodForce" +
            " direction should be x/y/z. Specified:"+s);

        if (!es.hasNextDouble()) UTIL.errorMsg(
            "nodForce value is not a double: " + es.next());
        double vd = es.nextDouble();

        itnf = es.readNumberList(itnf, idf, fem.nDim, vd);
    }

    // Read data for surface forces (element face loading):
    // direction, iel, nFaceNodes, faceNodes, forcesAtNodes.
    private void readSurForces(FeScanner es) {

        String s = es.next().toLowerCase();
        int dir = UTIL.direction(s);
        if (dir == -1) UTIL.errorMsg("surForce" +
           " direction should be x/y/z/n. Specified:"+s);
        int iel = es.readInt();
        int nFaceNodes = es.readInt();
        for (int i=0; i<nFaceNodes; i++)
            iw[i] = es.readInt();
        for (int i=0; i<nFaceNodes; i++)
            dw[i] = es.readDouble();
        itsf.add(new ElemFaceLoad(iel-1,nFaceNodes,dir,iw,dw));
    }

    // Create data for distributed surface load
    // specified inside a box
    private void createBoxSurForces(FeScanner es) {
        int[][] faces;
        String s = es.next().toLowerCase();
        int dir = UTIL.direction(s);
        if (dir == -1) UTIL.errorMsg("boxSurForce" +
            " direction should be x/y/z/n. Specified:" + s);

        if (!es.hasNextDouble()) UTIL.errorMsg(
            "boxSurForce value is not a double: " + es.next());
        double force = es.nextDouble();

        for (int i = 0; i < 2; i++)
            for (int j = 0; j < fem.nDim; j++)
                box[i][j] = es.readDouble();

        for (int iel=0; iel<fem.nEl; iel++) {
            Element el = fem.elems[iel];
            faces = el.getElemFaces();
            FACE:
            for (int[] face : faces) {
                int nNodes = face.length;
                for (int inod = 0; inod < nNodes; inod++)
                    iw[inod] = 0;
                for (int inod = 0; inod < nNodes; inod++) {
                    int iGl = el.ind[face[inod]];
                    if (iGl > 0) {
                        for (int j = 0; j < fem.nDim; j++) {
                            double x =
                                    fem.getNodeCoord(iGl-1,j);
                            if (x < box[0][j] || x > box[1][j])
                                continue FACE;
                        }
                        iw[inod] = iGl;
                    }
                }
                itsf.add(
                    new ElemFaceLoad(iel,nNodes,dir,iw,force));
            }
        }
    }

    // Assemble right-hand side of the global equation system
    public void assembleRHS() {

        if (scaleLoad != 0.0) {
            for (int i = 0; i < fem.nEq; i++) {
                dpLoad[i] *= scaleLoad;
                dhLoad[i] *= scaleLoad;
                RHS  [i] = dpLoad[i] + dhLoad[i];
            }
            return;
        }
        for (int i = 0; i < fem.nEq; i++) {
            dpLoad[i] = 0.0;
            dhLoad[i] = 0.0;
        }

        // Nodal forces specified directly
        itnf = nodForces.listIterator(0);
        Dof d;
        while (itnf.hasNext()) {
            d = (Dof) itnf.next();
            dpLoad[d.dofNum-1] = d.value;
        }

        // Surface load at element faces
        itsf = surForces.listIterator(0);
        ElemFaceLoad efl;
        Element elm;
        while (itsf.hasNext()) {
            efl =(ElemFaceLoad) itsf.next();
            elm = fem.elems[efl.iel];
            elm.setElemXy();
            if (elm.equivFaceLoad(efl)==-1)
                UTIL.errorMsg("surForce" +
                    " does not match any face of element: "
                    + efl.iel);
            elm.assembleElemVector(Element.evec,dpLoad);
        }

        // Temperature field
        if (fem.thermalLoading) {
            for (int iel = 0; iel < fem.nEl; iel++) {
                elm = fem.elems[iel];
                elm.setElemXyT();
                elm.thermalVector();
                elm.assembleElemVector(Element.evec,dhLoad);
            }
        }

        // Right-hand side = actual load + fictitious load
        for (int i = 0; i < fem.nEq; i++)
            RHS[i] = dpLoad[i] + dhLoad[i];

        // Displacement boundary conditions for right-hand side
        ListIterator itdbc = fem.defDs.listIterator(0);
        while (itnf.hasNext()) {
            d = (Dof) itdbc.next();
            RHS[d.dofNum-1] = FE.bigValue * d.value;
        }
    }

}
