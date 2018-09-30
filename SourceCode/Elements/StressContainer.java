package Elements;

// Stresses and equivalent strains at integration point
public class StressContainer {

    // Accumulated stress
    public double sStress[];
    // StressContainer increment
    public double dStress[];
    // Accumulated equivalent plastic strain
    public double sEpi;
    // Equivalent plastic strain increment
    public double dEpi;

    StressContainer(int nDim) {
        sStress = new double[2*nDim];
        dStress = new double[2*nDim];
    }

}
