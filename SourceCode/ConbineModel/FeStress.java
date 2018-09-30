package ConbineModel;

import Elements.*;
import Material.*;
import Utilities.*;

import java.io.*;
import java.util.ListIterator;

// Stress increment due to displacement increment
public class FeStress {

    public static double relResidNorm;
    private FeModel fem;

    // Constructor for stress increment.
    // fem - finite element model
    public FeStress(FeModel fem) {
        this.fem = fem;
    }

    // Compute stress increment for the finite element model
    public void computeIncrement() {

        // Accumulate solution vector in displacement increment
        for (int i=0; i<fem.nEq; i++)
            FeLoad.dDispl[i] += FeLoad.RHS[i];

        // Compute stresses at reduced integration points
        for (int iel = 0; iel < fem.nEl; iel++) {
            Element elm = fem.elems[iel];
            elm.setElemXyT();
            elm.disAssembleElemVector(FeLoad.dDispl);

            for (int ip = 0; ip < elm.str.length; ip++) {
                Material mat =
                    (Material) fem.materials.get(elm.matName);
                mat.strainToStress(elm, ip);
            }
        }
    }

    // Check equilibrium and assemble residual vector.
    // iter - number of iterations performed
    public boolean equilibrium(int iter) {

        if (fem.physLaw == FeModel.PhysLaws.elastic ||
                iter == FeLoad.maxIterNumber) return true;
        // Assemble residual vector to right-hand side
        for (int i=0; i<fem.nEq; i++)
            FeLoad.RHS[i] = FeLoad.spLoad[i]+FeLoad.dpLoad[i];
        Element elm;
        for (int iel = 0; iel < fem.nEl; iel++) {
            elm = fem.elems[iel];
            elm.setElemXy();
            elm.equivStressVector();
            elm.assembleElemVector(Element.evec,FeLoad.RHS);
        }
        // Displacement boundary conditions
        ListIterator it = fem.defDs.listIterator(0);
        while (it.hasNext()) {
            Dof d = (Dof) it.next();
            FeLoad.RHS[d.dofNum-1] = 0;
        }
        // Relative residual norm
        double dpLoadNorm = vectorNorm(FeLoad.dpLoad);
        if (dpLoadNorm < 1e-30)
            dpLoadNorm = vectorNorm(FeLoad.dhLoad);
        relResidNorm = vectorNorm(FeLoad.RHS)/dpLoadNorm;
        return relResidNorm < FeLoad.residTolerance;
    }

    // Returns norm of a vector v
    double vectorNorm(double[] v) {

        double norm = 0;
        for (double aV : v) norm += aV * aV;
        return Math.sqrt(norm);
    }

    // Accumulate loads, temperature and stresses
    public void accumulate() {

        for (int i=0; i<fem.nEq; i++) {
            FeLoad.spLoad[i] += FeLoad.dpLoad[i];
            FeLoad.sDispl[i] += FeLoad.dDispl[i];
        }
        for (int iel = 0; iel < fem.nEl; iel++)
             fem.elems[iel].accumulateStress();
    }

    // Write results to a file.
    public void writeResults() {

        String fileResult = fea.Jfem.fileOut + "."
                          + FeLoad.loadStepName;
        PrintWriter PR =
                new FePrintWriter().getPrinter(fileResult);

        PR.printf("Displacements\n\n");
        if (fem.nDim == 2)
            PR.printf(" Node             ux             uy");
        else
            PR.printf(" Node             ux             uy"
                                        + "             uz");
        for (int i = 0; i < fem.nNod; i++) {
            PR.printf("\n%5d", i + 1);
            for (int j = 0; j < fem.nDim; j++)
              PR.printf("%15.6e", FeLoad.sDispl[fem.nDim*i+j]);
        }

        PR.printf("\n\nStresses\n");
        for (int iel = 0; iel < fem.nEl; iel++) {
            if (fem.nDim == 2)
                PR.printf("\nEl %4d     sxx            syy"
                    +"            sxy            szz"
                    +"            epi", iel+1);
            else
                PR.printf("\nEl %4d     sxx            syy"
                    +"            szz            sxy"
                    +"            syz            szx"
                    +"            epi", iel+1);
            for (StressContainer aStr : fem.elems[iel].str) {
                PR.printf("\n");
                for (int i = 0; i < 2 * fem.nDim; i++)
                    PR.printf("%15.8f", aStr.sStress[i]);
                PR.printf("%15.8f", aStr.sEpi);
            }
        }
        PR.close();
    }

    // Read results from a file.
    // displ - displacements for the finite element model (out)
    public void readResults(String resultFile,double[] displ) {

        if (resultFile==null) return;

        FeScanner RD = new FeScanner(resultFile);
        // Read displacements
        RD.moveAfterLineWithWord("node");
        for (int i = 0; i < fem.nNod; i++) {
            RD.readInt();
            for (int j = 0; j < fem.nDim; j++)
                displ[fem.nDim*i + j] = RD.readDouble();
        }
        // Read stresses
        for (int iel = 0; iel < fem.nEl; iel++) {
            RD.moveAfterLineWithWord("el");
            for (StressContainer aStr : fem.elems[iel].str) {
                for (int i = 0; i < 2 * fem.nDim; i++)
                    aStr.sStress[i] = RD.readDouble();
                aStr.sEpi = RD.readDouble();
            }
        }
        RD.close();
    }

}
