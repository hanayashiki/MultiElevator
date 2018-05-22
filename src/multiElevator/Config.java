package multiElevator;

public class Config {
    /*  @OVERVIEW: 用来记录一些非关键作用的常量
        @INHERIT: Object
        @INVARIANCE: 所有变量不能改变
    */
    public static final int MIN_FLOOR = 1;
    public static final int MAX_FLOOR = 20;
    public static final int FLOOR_COUNT = MAX_FLOOR - MIN_FLOOR + 1;
    public static final double TIME_PER_FLOOR = 3.0;
    public static final double TIME_PER_OPEN = 6.0;
    public static final int MIN_ELEVATOR_ID = 1;
    public static final int MAX_ELEVATOR_ID = 3;

    public static final long ELEVATOR_COUNT = 3;
    public static final long ELEVATOR_MILLISECOND_PER_UPDATE = 1000;

    // public static final double epsilon = (1.0 * ELEVATOR_MILLISECOND_PER_UPDATE + 1) / 1000 * (1 / TIME_PER_FLOOR);
    public static final double epsilon = 1e-3;

    public static final int MAX_REQUEST_NUM_ONE_LINE = 10;
}
