package visual;

import elem.*;
import model.FeModel;

// Result values at nodes of the finite element model.
public class ResultAtNodes {

    private SurfaceGeometry sg;
    private FeModel fem;
    private int[] multNod;
    private double[][] stressNod;

    private double sm, psi, si, f;
    final double THIRD = 1.0/3.0, SQ3 = Math.sqrt(3.0);

    // Constructor for results at nodes.
    // sg - geometry of model surface.
    ResultAtNodes(SurfaceGeometry sg, FeModel fem) {

        this.sg = sg;
        this.fem = fem;
        multNod = new int[fem.nNod];
        stressNod = new double[fem.nNod][2*fem.nDim];

        feStressAtNodes();
    }

    // FE stresses at nodes: global array stressNod.
    private void feStressAtNodes() {

        double[][] elStressInt = new double[8][6];
        double[][] elStressNod = new double[20][6];

        for (int i = 0; i < fem.nNod; i++) {
            multNod[i] = 0;
            for (int j = 0; j < 2*fem.nDim; j++)
                stressNod[i][j] = 0;
        }

        for (int iel = 0; iel < fem.nEl; iel++) {
            Element el = fem.elems[iel];
            for (int ip = 0; ip < el.str.length; ip++)
                for (int j = 0; j < 2*fem.nDim; j++)
                    elStressInt[ip][j] = el.str[ip].sStress[j];

            el.extrapolateToNodes(elStressInt, elStressNod);

            // Assemble stresses
            for (int i=0; i<fem.elems[iel].ind.length; i++) {
                int jind = fem.elems[iel].ind[i] - 1;
                if (jind >= 0) {
                    for (int k = 0; k < 2*fem.nDim; k++)
                       stressNod[jind][k] += elStressNod[i][k];
                    multNod[jind] += 1;
                }
            }
        }
        // Divide by node multiplicity factor
        for (int i = 0; i < fem.nNod; i++) {
            for (int j = 0; j < 2*fem.nDim; j++)
                stressNod[i][j] /= multNod[i];
        }
    }

    // Set array sg.fun[] containing requested value at nodes.
    // parm - requested result value.
    // displ - displacement vector.
    void setParmAtNodes(VisData.parms parm, double[] displ) {

        sg.fmin = 1.e77;
        sg.fmax = -1.e77;

        for (int node = 0; node < fem.nNod; node++) {
            if (sg.sNodes[node] >= 0) {
                if (parm == VisData.parms.s1 ||
                    parm == VisData.parms.s2 ||
                    parm == VisData.parms.s3 ||
                    parm == VisData.parms.si ||
                    parm == VisData.parms.s13)
                            setEquivalentStress(node);
                switch (parm) {
                case ux:
                    f = displ[node*fem.nDim];          break;
                case uy:
                    f = displ[node*fem.nDim + 1];      break;
                case uz:
                    if (fem.nDim == 3)
                        f = displ[node*fem.nDim + 2];
                    else f = 0;
                                                       break;
                case sx:
                    f = stressNod[node][0];            break;
                case sy:
                    f = stressNod[node][1];            break;
                case sz:
                    f = stressNod[node][2];            break;
                case sxy:
                    f = stressNod[node][3];            break;
                case syz:
                    f = stressNod[node][4];            break;
                case szx:
                    f = stressNod[node][5];            break;
                case s1:
                    f = sm + 2*THIRD*si*Math.cos(psi); break;
                case s2:
                    f = sm - 2*THIRD*si*
                        Math.cos(THIRD*Math.PI+psi);   break;
                case s3:
                    f = sm - 2*THIRD*si*
                        Math.cos(THIRD*Math.PI-psi);   break;
                case si:
                    f = si;                            break;
                case s13:
                    f = THIRD*si*(Math.cos(psi) +
                        Math.cos(THIRD*Math.PI-psi));
                }
                sg.fun[node] = f;
                sg.fmin = Math.min(sg.fmin, f);
                sg.fmax = Math.max(sg.fmax, f);
            }
        }
        if (!(VisData.fMin == 0.0 && VisData.fMax == 0.0)) {
            sg.fmax = VisData.fMax;
            sg.fmin = VisData.fMin;
        }
        if (sg.fmax - sg.fmin < 1.e-6) sg.fmax += 1.e-6;
        sg.deltaf = sg.fmax - sg.fmin;
    }

    // Compute stress invariants and equivalent stress.
    private void setEquivalentStress(int node) {
        // Stresses
        double sx  = stressNod[node][0];
        double sy  = stressNod[node][1];
        double sz  = stressNod[node][2];
        double sxy = stressNod[node][3];
        double syz, szx;
        if (fem.nDim == 3) {
            syz = stressNod[node][4];
            szx = stressNod[node][5];
        }
        else { syz = 0;  szx = 0; }
        // Mean stress
        sm = THIRD*(sx + sy + sz);
        // Deiatoric stresses
        double dx = sx - sm;
        double dy = sy - sm;
        double dz = sz - sm;
        // Second and third deviatoric invariants
        double J2 =  0.5*(dx*dx + dy*dy + dz*dz)
                  + sxy*sxy + syz*syz + szx*szx;
        double J3 = dx*dy*dz + 2*sxy*syz*szx
                  - dx*syz*syz - dy*szx*szx - dz*sxy*sxy;
        // Angle
        psi = THIRD*Math.acos(1.5*SQ3*J3/Math.sqrt(J2*J2*J2));
        // Equivalent stress
        si = Math.sqrt(3*J2);
    }

}