package multiElevator;

public class Output {
    public static void println(String string) {
        System.out.println(string);
    }
    public static void printInvalid(String string, long inputTime) {
        String currentTimeDecimal = Global.milliTimeToSecond(System.currentTimeMillis(), 1);
        String inputTimeDecimal = Global.milliTimeToSecond(inputTime, 1);
        println(currentTimeDecimal + ": " + "[" + string + ", " + inputTimeDecimal + "]");
    }
}
