package FiniteElementAnalysis;

import visual.*;
import util.*;

import java.applet.Applet;
import com.sun.j3d.utils.applet.MainFrame;

// Main class of the visualizer
public class Jvis extends Applet {

    public static FeScanner RD = null;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println(
                    "Usage: java FiniteElementAnalysis.Jvis FileIn \n");
            return;
        }
        FE.main = FE.JVIS;

        RD = new FeScanner(args[0]);
        System.out.println("FiniteElementAnalysis.Jvis: Visualization." +
                " Data file: " + args[0]);

        new MainFrame(new Jvis(), 800, 600);
    }

    public Jvis() {

        VisData.readData(RD);

        new J3dScene(this);

    }

}
