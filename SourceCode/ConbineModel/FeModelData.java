package ConbineModel;

import Elements.*;
import Utilities.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;

// Finite element model data
public class FeModelData {

    static FeScanner RD;
    static PrintWriter PR;

    // Problem dimension =2/3
    public int nDim = 3;
    // Number of degrees of freedom per node =2/3
    public int nDf = 3;
    // Number of nodes
    public int nNod;
    // Number of elements
    public int nEl;
    // Number of degrees of freedom in the FE model
    public int nEq;
    // Elements
    public Element elems[];
    // Materials
    public HashMap materials = new HashMap();
    // Coordinates of nodes
    private double xyz[];
    // Constrained degrees of freedom
    public  LinkedList defDs = new LinkedList();
    public boolean thermalLoading;
    static String varName;

    public static enum StrStates {
        plstrain, plstress, axisym, threed
    }
    public static StrStates stressState = StrStates.threed;

    public static enum PhysLaws {
        elastic, elplastic
    }
    public PhysLaws physLaw = PhysLaws.elastic;

    // Input data names
    enum vars {
        nel, nnod, ndim, stressstate, physlaw, solver,
        elcon, nodcoord, material,
        constrdispl, boxconstrdispl, thermalloading,
        includefile, user, end
    }

    // Allocation of nodal coordinate array
    public void newCoordArray() {
        xyz = new double[nNod*nDim];
    }

    // Set coordinates of node
    public void setNodeCoords(int node, double[] xyzn) {
        for (int i=0; i<nDim; i++) xyz[node*nDim+i] = xyzn[i];
    }

    // Set ith coordinates of node
    public void setNodeCoord(int node, int i, double v) {
        xyz[node*nDim+i] = v;
    }

    // Get coordinates of node
    public double[] getNodeCoords(int node) {
        double nodeCoord[] = new double[nDim];
        for (int i=0; i<nDim; i++)
            nodeCoord[i] = xyz[node*nDim+i];
        return nodeCoord;
    }

    // Get ith coordinate of node
    public double getNodeCoord(int node, int i) {
        return xyz[node*nDim+i];
    }

}
