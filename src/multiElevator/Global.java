package multiElevator;

import java.text.NumberFormat;

public class Global {
    /* @OVERVIEW: 全局类，用于记录时间和处理时间请求。
    * */
    public static long timeZeroPoint = -1;
    public static long processTimeZeroPoint = System.currentTimeMillis();
    public static long processNanoTimeZeroPoint = System.nanoTime();
    private static NumberFormat nf = NumberFormat.getInstance();
    public static String sysMilliTimeToT(long milliTime, int d) {
        // @REQUIRE: None
        // @MODIFIES: None
        // @EFFECTS: \result == string &&
        //          string == (milliTime - firstRequest.getTimeArrival()).toString() &&
        //          string.fractionDigit == 1;
        nf.setMinimumFractionDigits(d);
        nf.setMaximumFractionDigits(d);
        return nf.format(1.0 * (milliTime - timeZeroPoint) / 1000);
    }
    public static long getRelativeTime() {
        return System.currentTimeMillis() - processTimeZeroPoint;
    }
    public static long getST() {
        return System.currentTimeMillis();
    }
}
