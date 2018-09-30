package FiniteElementAnalysis;

import Elements.*;
import ConbineModel.*;
import Solvers.*;
import Utilities.*;
import java.io.*;

// Main class of the finite element processor
public class Jfem {

    private static FeScanner RD;
    private static PrintWriter PR;
    public static String fileOut;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println(
                   "Usage: java FiniteElementAnalysis.JFEM FileIn [FileOut]\n");
            return;
        }
        FE.main = FE.JFEM;

        RD = new FeScanner(args[0]);

        fileOut = (args.length==1) ? args[0]+".lst" : args[1];
        PR = new FePrintWriter().getPrinter(fileOut);

        PR.println("FiniteElementAnalysis.JFEM: FE code. Data file: " + args[0]);
        System.out.println("FiniteElementAnalysis.JFEM: FE code. Data file: "
                + args[0]);

        new Jfem();
        PR.close();
    }

    public Jfem () {

        UTIL.printDate(PR);

        FeModel fem = new FeModel(RD, PR);
        Element.fem = fem;

        fem.readData();

        PR.printf("\nNumber of elements    nEl = %d\n"+
                  "Number of nodes      nNod = %d\n"+
                  "Number of dimensions nDim = %d\n",
                  fem.nEl, fem.nNod, fem.nDim);

        long t0 = System.currentTimeMillis();

        Solver solver = Solver.newSolver(fem);
        solver.assembleGSM();

        PR.printf("Memory for global matrix: %7.2f MB\n",
                Solver.lengthOfGSM*8.0e-6);

        FeLoad load = new FeLoad(fem);
        Element.load = load;

        FeStress stress = new FeStress(fem);

        // Load step loop
        while (load.readData( )) {
            load.assembleRHS();
            int iter = 0;
            // Equilibrium iterations
            do {
                iter++;
                int its = solver.solve(FeLoad.RHS);
                if (its > 0) PR.printf(
                    "Solver: %d iterations\n", its);
                stress.computeIncrement();
            } while (!stress.equilibrium(iter));

            stress.accumulate();
            stress.writeResults();
            PR.printf("Loadstep %s", FeLoad.loadStepName);
            if (iter>1) PR.printf(" %5d iterations, " +
                "Relative residual norm = %10.5f",
                iter, FeStress.relResidNorm);
            PR.printf("\n");
        }

        PR.printf("\nSolution time = %10.2f s\n",
                (System.currentTimeMillis()-t0)*0.001);
    }

}
