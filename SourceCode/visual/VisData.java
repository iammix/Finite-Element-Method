package visual;

import ConbineModel.*;
import Utilities.*;
import Elements.Element;

import javax.vecmath.*;

public class VisData {

    static FeModel fem;
    // Global vectot of nodal displacements
    static double displ[];

    // Input data names
    enum vars {
        meshfile, resultfile, parm, showedges, shownodes,
        ndivmin, ndivmax, fmin, fmax, ncontours, deformscale,
        end
    }

    // Parameters that can be visualized: displacements,
    // stresses, principal stresses and equivalent stress
    enum parms {
        ux, uy, uz, sx, sy, sz, sxy, syz, szx,
        s1, s2, s3, si, s13, none
    }

    static String meshFile = null, resultFile = null;
    static parms parm = parms.none;
    static boolean showEdges = true, showNodes = false,
        showDeformShape = false, drawContours = false;
    static double deformScale = 0.0;
    static int nDivMin = 2, nDivMax = 16;
    static double fMin = 0, fMax = 0;
    static int nContours = 256;

    static float offset = 500.0f;
    static float offsetFactor = 1.0f;

    static Color3f bgColor     = new Color3f(1.0f, 1.0f, 1.0f);
    static Color3f modelColor  = new Color3f(0.5f, 0.5f, 0.9f);
    static Color3f surTexColor = new Color3f(0.8f, 0.8f, 0.8f);
    static Color3f edgeColor   = new Color3f(0.2f, 0.2f, 0.2f);
    static Color3f nodeColor   = new Color3f(0.2f, 0.2f, 0.2f);

    // Size of the color gradation strip
    static int textureSize = 256;
    // Coefficient for curvature: n = 1 + C*ro
    static double Csub = 15;
    // Coefficient for contours: n = 1 + F*abs(df)/deltaf
    static double Fsub = 20;

    public static void readData(FeScanner RD) {

        readDataFile(RD);

        FeScanner fes = new FeScanner(meshFile);
        fem = new FeModel(fes, null);
        Element.fem = fem;
        fem.readData();

        if (resultFile != null) {
            displ = new double[fem.nNod*fem.nDf];
            FeStress stress = new FeStress(fem);
            stress.readResults(resultFile, displ);
            if (deformScale > 0) showDeformShape = true;
            drawContours = VisData.parm != VisData.parms.none;

        }
    }

    static void readDataFile(FeScanner RD) {

        vars name = null;

        while (RD.hasNext()) {

            String varName = RD.next();
            String varNameLower = varName.toLowerCase();
            if (varName.equals("#")) {
                RD.nextLine();    continue;
            }
            try {
                name = vars.valueOf(varNameLower);
            } catch (Exception e) {
                UTIL.errorMsg(
                    "Variable name is not found: " + varName);
            }

            switch (name) {

            case meshfile:
                meshFile = RD.next();
                break;
            case resultfile:
                resultFile = RD.next();
                break;
            case parm:
                try {
                  varName = RD.next();
                  parm = parms.valueOf(varName.toLowerCase());
                } catch (Exception e) { UTIL.errorMsg(
                  "No such result parameter: " + varName); }
                break;
            case showedges:
                showEdges = RD.next().equalsIgnoreCase("y");
                break;
            case shownodes:
                showNodes = RD.next().equalsIgnoreCase("y");
                break;
            case ndivmin:
                nDivMin = RD.readInt();
                break;
            case ndivmax:
                nDivMax = RD.readInt();
                break;
            case fmin:
                fMin = RD.readDouble();
                break;
            case fmax:
                fMax = RD.readDouble();
                break;
            case ncontours:
                nContours = RD.readInt();
                break;
            case deformscale:
                deformScale = RD.readDouble();
                break;
            case end:
                return;
            }
        }
    }

}
