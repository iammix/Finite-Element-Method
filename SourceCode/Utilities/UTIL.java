package Utilities;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.PrintWriter;

// Miscellaneous static methods
public class UTIL {

    // Print date and time.
    // PR - PrintWriter for listing file
    public static void printDate(PrintWriter PR) {

        Calendar c = new GregorianCalendar();

        PR.printf("Date: %d-%02d-%02d  Time: %02d:%02d:%02d\n",
            c.get(Calendar.YEAR),  c.get(Calendar.MONTH)+1,
            c.get(Calendar.DATE),  c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),c.get(Calendar.SECOND));
    }

    // Print error message and exit.
    // message - error message that is printed.
    public static void errorMsg(String message) {
            System.out.println("=== ERROR: " + message);
            System.exit(1);
    }

    // Transform text direction into integer.
    // s - direction x/y/z/n.
    // returns  integer direction 1/2/3/0, error: -1.
    public static int direction(String s) {
        if      (s.equals("x")) return 1;
        else if (s.equals("y")) return 2;
        else if (s.equals("z")) return 3;
        else if (s.equals("n")) return 0;
        else return -1;
    }

}
