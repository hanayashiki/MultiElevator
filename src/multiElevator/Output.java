package multiElevator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Output {
    /*
    * @OVERVIEW: 输出类，用于与评测相关的输出，分离评测输出和 debug 输出。
    * */
    private static OutputStream outputStream;

    synchronized public static void println(String string) {
        /* @REQUIRES: canWrite("result.txt")
        *  @MODIFIES: outputStream == null ==> outputStream = new FileOutputStream(new File("result.txt"));
        *  @EFFECTS:
        *  1. outputStream.write((string + "\n").getBytes("UTF-8"))
        *  2. exceptional_behavior(!canWrite("result.txt") ==> raise IOException)
        *  @THREAD_REQUIRES: None
        *  @THREAD_EFFECTS: locked(this)
        * */
        try {
            if (outputStream == null) {
                outputStream = new FileOutputStream(new File("result.txt"));
            }
            outputStream.write((string + "\n").getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void printInvalid(String string, long inputTime) {
        /*  @REQUIRES: None
         *  @MODIFIES: None
         *  @EFFECTS: outputStream.nextLine ==
         *      Global.getST() + ": INVALID " + "[" + string + ", " + inputTimeDecimal + "]"
         *  @THREAD_REQUIRES: None
         *  @THREAD_EFFECTS: locked(this)
         */
        String inputTimeDecimal = Global.sysMilliTimeToT(inputTime, 1);
        println(Global.getST() + ": INVALID " + "[" + string + ", " + inputTimeDecimal + "]");
    }
    public static void printSame(Request req) {
        /*
        *   @REQUIRES: None
        *   @MODIFIES: None
        *   @EFFECTS: outputStream.nextLine ==
        *       "# " + Global.getST() + ": SAME " + "[" + req.getOriginString() + ", " + inputTimeDecimal + "]"
        *   @THREAD_REQUIRES: None
        *   @THREAD_EFFECTS: locked(this)
        * */
        String inputTimeDecimal = Global.sysMilliTimeToT(req.getTimeArrive(), 1);
        println("# " + Global.getST() + ": SAME " + "[" + req.getOriginString() + ", " + inputTimeDecimal + "]");
    }
    // st:[request, T] / (#电梯号, 楼层, UP/DOWN, 累积运动量，t)
    public static void printArrival(Request req, int elevatorId, int targetFloor, int totalDistance,
                                    Direction direction, long finishingTime) {
        /*
        *   @REQUIRES: None
        *   @MODIFIES: None
        *   @EFFECTS: outputStream.nextLine == (Global.getST() + ": [" + req.getOriginString() + ", " +
        *       inputTimeDecimal + "]" + " / " + "(" + "#" + elevatorId + ", " + targetFloor + ", " +
        *       direction + ", " + totalDistance + ", " + arrivalTimeDecimal + ")");
        *   @THREAD_REQUIRES: None
        *   @THREAD_EFFECTS:  locked(this);
        * */
        String inputTimeDecimal = Global.sysMilliTimeToT(req.getTimeArrive(), 1);
        String arrivalTimeDecimal;
        if (direction == Direction.STILL) {
            arrivalTimeDecimal = Global.sysMilliTimeToT(finishingTime +
                    1000 * (int)Config.TIME_PER_OPEN, 1);
        } else {
            arrivalTimeDecimal = Global.sysMilliTimeToT(finishingTime, 1);
        }
        println(Global.getST() + ": [" + req.getOriginString() + ", " + inputTimeDecimal + "]" +
                " / " + "(" + "#" + elevatorId + ", " + targetFloor + ", " + direction + ", " + totalDistance +
                ", " + arrivalTimeDecimal + ")");
    }
}
