package multiElevator;

public class Output {
    public static void println(String string) {
        System.out.println(string);
    }
    public static void printInvalid(String string, long inputTime) {
        String inputTimeDecimal = Global.milliTimeToSecond(inputTime, 1);
        println(Global.getST() + ": INVALID " + "[" + string + ", " + inputTimeDecimal + "]");
    }
    public static void printSame(String string, long inputTime) {
        String inputTimeDecimal = Global.milliTimeToSecond(inputTime, 1);
        println(Global.getST() + ": SAME " + "[" + string + ", " + inputTimeDecimal + "]");
    }
}
