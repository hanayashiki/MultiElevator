package multiElevator;

import java.text.NumberFormat;

public class Global {
    public static long timeZeroPoint = -1;
    private static NumberFormat nf = NumberFormat.getInstance();
    public static String milliTimeToSecond(long milliTime, int d) {
        nf.setMinimumFractionDigits(d);
        nf.setMaximumFractionDigits(d);
        return nf.format(milliTime - timeZeroPoint);
    }
}
