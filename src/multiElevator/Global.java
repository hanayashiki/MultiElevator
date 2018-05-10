package multiElevator;

import java.text.NumberFormat;

public class Global {
    public static long timeZeroPoint = -1;
    public static long processTimeZeroPoint = System.currentTimeMillis();
    public static long processNanoTimeZeroPoint = System.nanoTime();
    private static NumberFormat nf = NumberFormat.getInstance();
    public static String milliTimeToSecond(long milliTime, int d) {
        nf.setMinimumFractionDigits(d);
        nf.setMaximumFractionDigits(d);
        return nf.format(milliTime - timeZeroPoint);
    }
    public static long getRelativeTime() {
        return System.currentTimeMillis() - processTimeZeroPoint;
    }
}
