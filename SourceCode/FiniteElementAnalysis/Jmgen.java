package FiniteElementAnalysis;

import Utilities.*;

import java.io.*;
import java.util.HashMap;

// Main class of the mesh generator
public class Jmgen {

    public static FeScanner RD;
    public static PrintWriter PR;
    public static HashMap blocks;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println(
                "Usage: java FiniteElementAnalysis.Jmgen FileIn [FileOut]\n");
            return;
        }
        FE.main = FE.JMGEN;

        RD = new FeScanner(args[0]);

        String fileOut = (args.length == 1) ?
                args[0]+".lst" : args[1];
        PR = new FePrintWriter().getPrinter(fileOut);

        PR.println("FiniteElementAnalysis.Jmgen: Mesh generator. Data file: "
                + args[0]);
        System.out.println("FiniteElementAnalysis.Jmgen: Mesh generator. "
                + "Data file: " + args[0]);

        new Jmgen();
    }

    Jmgen() {

        UTIL.printDate(PR);

        // Hash table for storing mesh blocks
        blocks = new HashMap();

        while (RD.hasNext()) {

            String name = RD.next().toLowerCase();
            if (name.equals("#")) { RD.nextLine(); continue; }
            PR.println("------------------------------------");

            try {
                Class.forName("gener." + name).newInstance();
            } catch (Exception e) {
                UTIL.errorMsg("Class name not found: "+name);
            }
        }
        PR.close();
    }

}
