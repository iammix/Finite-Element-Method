package FiniteElementAnalysis;

// Symbolic constants
public class FE {

    // Main method of application: JFEM/JMGEN/JVIS
    public static int main;
    public static final int JFEM = 0, JMGEN = 1, JVIS = 2;

    public static final int maxNodesPerElem = 20;

    // Big value for displacements boundary conditions
    public static double bigValue = 1.0e64;
    // Solution tuning
    public static boolean tunedSolver = true;

    // Error tolerance for PCG solution of FE equation system
    public static final double epsPCG = 1.e-10;
    // Constants for PCG solution method
    public static int maxRow2D = 21,
                      maxRow3D = 117,
                      maxIterPcg = 10000;

    // Integration scheme for elastic-plastic problems
    public static boolean epIntegrationTANGENT = false;
}
