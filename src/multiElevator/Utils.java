package multiElevator;

public class Utils {
    public static double directionToSign(Direction direction) {
        switch (direction) {
            case UP:
                return 1;
            case DOWN:
                return -1;
            case STILL:
                return 0;
        }
        assert false;
        return -1;
    }
    public static Direction signToDirection(double sign) {
        sign = Math.signum(sign);
        if (doubleEqual(sign, 1)) {
            return Direction.UP;
        }
        if (doubleEqual(sign, 0)) {
            return Direction.STILL;
        }
        if (doubleEqual(sign, -1)) {
            return Direction.DOWN;
        }
        return Direction.DOWN;
    }

    public static boolean doubleEqual(double a, double b) {
        return Math.abs(a-b) < Config.epsilon;
    }
    public static boolean doubleGreater(double a, double b) {
        return a-b >= Config.epsilon;
    }
    public static boolean doubleLess(double a, double b) {
        return a-b <= -Config.epsilon;
    }
    public static boolean doubleGreaterEqual(double a, double b) {
        return doubleEqual(a, b) || doubleGreater(a, b);
    }
    public static boolean doubleLessEqual(double a, double b) {
        return doubleEqual(a, b) || doubleLess(a, b);
    }
    public static boolean doubleNearInt(double a) {
        double floor = Math.floor(a);
        double ceil = Math.ceil(a);
        return doubleEqual(floor, a) || doubleEqual(ceil, a);
    }
}
