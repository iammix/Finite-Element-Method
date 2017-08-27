package solver;

import elem.*;
import model.*;
import fea.FE;

// Solution of the global equation system
public abstract class Solver {

    static FeModel fem;
    // number of equations
    static int neq;
    // length of global stiffness matrix
    public static int lengthOfGSM;
    // elem connectivities - degrees of freedom
    int[] indf;
    // number of degrees of freedom for element
    int nindf;
    // Indicator of new global matrix
    boolean newMatrix;

    public static enum Solvers {
        ldu {Solver create()
                {return new SolverLDU();} },
        pcg {Solver create()
                {return new SolverPCG();} };
        abstract Solver create();
    }

    public static Solvers solver = Solvers.ldu;

    public static Solver newSolver(FeModel fem) {
        Solver.fem = fem;
        neq = fem.nEq;
        return solver.create();
    }

    // Assemble global stiffnes matrix
    public void assembleGSM() {
        Element elm;
        indf = new int[FE.maxNodesPerElem*fem.nDf];

        for (int iel=0; iel<fem.nEl; iel++) {
            for (int i=0; i<fem.elems[iel].ind.length; i++) {
                for (int k=0; k<fem.nDf; k++)
                    indf[fem.nDf*i+k] =
                        (fem.elems[iel].ind[i]-1)*fem.nDf+k+1;
            }
            nindf = fem.elems[iel].ind.length*fem.nDf;
            elm = fem.elems[iel];
            elm.setElemXy();
            elm.stiffnessMatrix();
            assembleESM();
        }
        // Indicate that new global matrix appeared
        newMatrix = true;
    }

    // Add element stiffness matrix to GSM
    void assembleESM() {}

    // Solve global equation system
    // x - right-hand side/solution (in/out)
    public int solve(double x[]) {
        return 0;
    }

}