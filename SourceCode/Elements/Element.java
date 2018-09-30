package Elements;

import ConbineModel.*;
import Material.*;
import FiniteElementAnalysis.FE;
import Utilities.UTIL;

// Finite element
public abstract class Element {

    // Finite element model
    public static FeModel fem;
    // Finite element load
    public static FeLoad load;
    // Material of current element
    static Material mat;
    // Element stiffness matrix
    public static double kmat[][] = new double[60][60];
    // Element vector
    public static double evec[] = new double[60];
    // Element nodal coordinates
    static double xy[][] = new double[20][3];
    // Element nodal temperatures
    static double dtn[] = new double[20];
    // Strain vector
    static double dstrain[] = new double[6];

    // Element name
    public String name;
    // Element material name
    public String matName;
    // Element connectivities
    public int ind[];
    // Stress-strain storage
    public StressContainer[] str;

    // Implemented element types
    static enum elements {
        quad8 {Element create() {return new ElementQuad2D();}},
        hex20 {Element create() {return new ElementQuad3D();}};

        abstract Element create();
    }

    // Construct new element
    // name - element name
    public static Element newElement(String name) {
        elements el = null;
        try {
            el = elements.valueOf(name);
        } catch (Exception e) {
            UTIL.errorMsg("Incorrect element type: " + name);
        }
        return el.create();
    }

    // Constructor for an element.
    // name - element name;
    // nind - number of nodes;
    // nstress - number of stress points
    public Element(String name, int nind, int nstress) {
        this.name = name;
        ind = new int[nind];
        if (FE.main != FE.JMGEN) {
            str = new StressContainer[nstress];
            for (int ip=0; ip<nstress; ip++)
                str[ip] = new StressContainer(fem.nDim);
        }
    }

    // Compute element stiffness matrix kmat[][]
    public void stiffnessMatrix() { }

    // Compute element thermal vector (evec[])
    public void thermalVector() { }

    // Element nodal equivalent of distributed face load
    // (evec[])
    public int equivFaceLoad(ElemFaceLoad surLd) {
        return -1;
    }

    // Nodal vector equivalent to stresses (evec[])
    public void equivStressVector() { }

    // Get local node numbers for element faces
    // returns elementFaces[nFaces][nNodesOnFace]
    public int[][] getElemFaces() {
        return new int[][] {{0},{0}};
    }

    // Get strains at integration point (stress)
    // intPoint - integration point number (stress);
    // returns  strain vector [2*ndim]
    public double[] getStrainsAtIntPoint(int intPoint) {
        return new double[] {0,0};
    }

    // Get temperature at integration point (stress)
    // intPoint - integration point number (stress);
    // returns  temperature
    public double getTemperatureAtIntPoint(int intPoint) {
        return 0.0;
    }

    // Extrapolate quantity from integration points to nodes
    // fip [nInt][2*nDim] - values at integration points;
    // fn [nind][2*nDim] - values at nodes (out)
    public void extrapolateToNodes(double[][] fip,
                                   double[][] fn) {
    }

    // Set element connectivities
    // indel - connectivity numbers
    // nind - number of element nodes
    public void setElemConnectivities(int[] indel, int nind) {
        System.arraycopy(indel, 0, ind, 0, nind);
    }

    // Set element connectivities
    // indel - connectivity numbers
    public void setElemConnectivities(int[] indel) {
        System.arraycopy(indel, 0, ind, 0, indel.length);
    }

    // Set element material name
    // mat - material name
    public void setElemMaterial(String mat) {
        matName = mat;
    }

    // Set element nodal coordinates xy[nind][nDim]
    public void setElemXy() {
        for (int i = 0; i < ind.length; i++) {
            int indw = ind[i] - 1;
            if (indw >= 0) {
                xy[i] = fem.getNodeCoords(indw);
            }
        }
    }

    // Set nodal coordinates xy[nind][nDim] and
    //     temperatures dtn[nind]
    public void setElemXyT() {
        for (int i = 0; i < ind.length; i++) {
            int indw = ind[i] - 1;
            if (indw >= 0) {
                if (fem.thermalLoading)
                    dtn[i] = FeLoad.dtemp[indw];
                xy[i] = fem.getNodeCoords(indw);
            }
        }
    }

    // Assemble element vector.
    // elVector - element vector;
    // glVector - global vector (in/out)
    public void assembleElemVector(double[] elVector,
                                   double[] glVector) {
        for (int i = 0; i < ind.length; i++) {
            int indw = ind[i] - 1;
            if (indw >= 0) {
                int adr = indw*fem.nDim;
                for (int j = 0; j < fem.nDim; j++)
                    glVector[adr+j] += elVector[i*fem.nDim +j];
            }
        }
    }

    // Disassemble element vector (result in evec[]).
    // glVector - global vector
    public void disAssembleElemVector(double[] glVector) {
        for (int i = 0; i < ind.length; i++) {
            int indw = ind[i] - 1;
            if (indw >= 0) {
                int adr = indw*fem.nDim;
                for (int j = 0; j < fem.nDim; j++)
                    evec[i*fem.nDim +j] = glVector[adr+j];
            }
        }
    }

    // Returns element connectivities
    public int[] getElemConnectivities() {
        int indE[] = new int[ind.length];
        System.arraycopy(ind, 0, indE, 0, ind.length);
        return indE;
    }

    //  Accumulate stresses and equivalent plastic strain
    public void accumulateStress() {
        for (int ip=0; ip<str.length; ip++)
            for (int i = 0; i < 2*fem.nDim; i++)
                str[ip].sStress[i] += str[ip].dStress[i];
            for (int ip=0; ip<str.length; ip++)
                str[ip].sEpi += str[ip].dEpi;
    }

}
