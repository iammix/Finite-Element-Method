package util;

import model.Dof;
import java.util.Scanner;
import java.util.ListIterator;
import java.io.File;

// JFEM data scanner. Delimiters: blank, =.
public class FeScanner {

    private Scanner es;

    // Constructs FE data scanner.
    // fileIn - name of the file containing data.
    public FeScanner(String fileIn) {

        try {
            es = new Scanner(new File(fileIn));
        } catch (Exception e) {
            UTIL.errorMsg("Input file not found: " + fileIn);
        }
        es.useDelimiter("\\s*=\\s*|\\s+");

    }

    // Returns  true if another token is available.
    public boolean hasNext() { return es.hasNext(); }

     // Returns  true if double is next in input.
    public boolean hasNextDouble() {return es.hasNextDouble();}

    // Gives the next token from this scanner.
    public String next() { return es.next(); }

    // Gives the next double from this scanner.
    public double nextDouble() { return es.nextDouble(); }

    // Reads the next integer.
    // Generates an error if next token is not integer.
    public int readInt() {
        if (!es.hasNextInt()) UTIL.errorMsg(
                "Expected integer. Instead: "+es.next());
        return es.nextInt();
    }

    // Reads the next double.
    // Generates an error if next token is not double.
    public double readDouble() {
        if (!es.hasNextDouble()) UTIL.errorMsg(
                "Expected double. Instead: "+es.next());
        return es.nextDouble();
    }

    // Advances the scanner past the current line.
    public void nextLine() { es.nextLine(); }

    // Moves to line which follows a line with the word.
    public void moveAfterLineWithWord(String word) {

        while (es.hasNext()) {
            String varname = es.next().toLowerCase();
            if (varname.equals("#")) { es.nextLine();
                                              continue; }
            if (varname.equals(word)) {
                es.nextLine();
                return;
            }
        }
        UTIL.errorMsg("moveAfterLineWithWord cannot find: "
                + word);
    }

    // Method reads < nNumbers numbers > and places resulting
    // degrees of freedom in a List data structure.
    // Here numbers is a sequence of the type n1 n2 -n3 ...
    // where n2 -n3 means from n2 to n3 inclusive.
    // it - list iterator.
    // dir - direction (1,2,3).
    // nDim - problem dimension (2/3).
    // sValue - specified value.
    // returns - modified list iterator it.
    public ListIterator readNumberList(ListIterator it,
                 int dir, int ndim, double sValue) {
        // number of items in the list
        int ndata = readInt();
        int i1, i2;
        i1 = i2 = readInt();
        for (int i=1; i<ndata; i++) {
            i2 = readInt();
            if (i2 > 0 && i1 >= 0) {
                if (i1 > 0) {
                    it.add(new Dof(ndim*(i1-1)+dir, sValue));
                }
                i1 = i2;
            }
            else if (i2 < 0) {
                for (int j=i1; j<=(-i2); j++) {
                    it.add(new Dof(ndim*(j-1)+dir, sValue));
                }
                i1 = 0;
                i2 = 0;
            }
        }
        if (i2 > 0) {
            it.add(new Dof(ndim*(i2-1)+dir, sValue));
        }
        return it;
    }

    // Closes this scanner.
    public void close() { es.close(); }

}
