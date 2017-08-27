package material;

import elem.Element;

// Material constitutive relations
public class Material {

    // StressContainer state (plstrain/plstress/axisym/threed)
    String stressState;
    // Elasticity modulus
    double e;
    // Poisson's ratio
    double nu;
    // Thermal expansion
    double alpha;
    // Yield stress
    double sY;
    // Hardening coefficient
    double km;
    // Hardening power
    double mm;

    public static Material newMaterial (String matPhysLaw,
                                   String stressState) {
        if (matPhysLaw.equals("elastic"))
              return new ElasticMaterial(stressState);
        else  return new ElasticPlasticMaterial(stressState);
    }

    // Given strain increment at integration point ip
    // element elm, compute stress dsig increment
    public void strainToStress(Element elm, int ip) {  }

    // Set elastic properties
    public void setElasticProp(double e, double nu,
                               double alpha){
        this.e = e;
        this.nu = nu;
        this.alpha = alpha;
    }

    // Set plastic properties
    public void setPlasticProp(double sY, double km,
                               double mm) {
        this.sY = sY;
        this.km = km;
        this.mm = mm;
    }

    // Returns Lame constant lambda
    public double getLambda() {
        return (stressState.equals("plstress")) ?
            e*nu/((1+nu)*(1-nu)) : e*nu/((1+nu)*(1-2*nu));
    }

    // Returns shear modulus
    public double getMu() { return 0.5*e/(1 + nu); }

    // Returns Poisson's ratio
    public double getNu() { return nu;  }

    // Returns thermal expansion coefficient
    public double getAlpha() { return alpha; }

    // Compute elasticity matrix emat
    public void elasticityMatrix(double[][] emat) {  }

}
