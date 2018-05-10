package multiElevator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static String floorRequestRegex = "\\(FR,(\\+?[0-9]+),(UP|DOWN)\\)";
    private static String elevatorRequestRegex = "\\(ER,#(\\+?[0-9]+),(\\+?[0-9]+)\\)";
    private static Pattern floorRequestPattern = Pattern.compile(floorRequestRegex);
    private static Pattern elevatorRequestPattern = Pattern.compile(elevatorRequestRegex);

    public static Request parse(String requestString, long arrivalTime) {
        String trimmedInput = requestString.replace(" ", "");
        Matcher floorRequestMatcher = floorRequestPattern.matcher(trimmedInput);
        Matcher elevatorRequestMatcher = elevatorRequestPattern.matcher(trimmedInput);

        if (trimmedInput.equals("END")) {
            return null;
        }

        if (!floorRequestMatcher.matches() && !elevatorRequestMatcher.matches()) {
            throw new InputException(requestString);
        }

        Request newRequest = null;

        try {

            if (floorRequestMatcher.matches()) {
                int callingFloor = parseInt(floorRequestMatcher.group(1));
                Direction direction = Direction.UP;
                String directionString = floorRequestMatcher.group(2);
                if (directionString.equals("UP")) {
                    direction = Direction.UP;
                }
                if (directionString.equals("DOWN")) {
                    direction = Direction.DOWN;
                }

                newRequest = new Request(Request.Type.FR, callingFloor, direction, arrivalTime, requestString);

            }

            if (elevatorRequestMatcher.matches()) {
                int elevatorId = parseInt(elevatorRequestMatcher.group(1));
                int targetFloor = parseInt(elevatorRequestMatcher.group(2));

                newRequest = new Request(Request.Type.ER, elevatorId, targetFloor, arrivalTime, requestString);
            }

        } catch (NumberFormatException ne) {
            throw new InputException(requestString);
        }

        if (checkSemantics(newRequest)) {
            return newRequest;
        } else {
            throw new InputException(requestString);
        }
    }

    private static int parseInt(String str) {
        return Integer.parseInt(str);
    }

    private static boolean checkSemantics(Request newRequest) {
        if (newRequest.getType() == Request.Type.FR) {
            if (newRequest.getCallingFloor() < Config.MIN_FLOOR || newRequest.getCallingFloor() > Config.MAX_FLOOR) {
                return false;
            }
            if (newRequest.getCallingFloor() == Config.MIN_FLOOR && newRequest.getDirection() == Direction.DOWN) {
                return false;
            }
            if (newRequest.getCallingFloor() == Config.MAX_FLOOR && newRequest.getDirection() == Direction.UP) {
                return false;
            }
        }
        if (newRequest.getType() == Request.Type.ER) {
            if (newRequest.getElevatorId() < Config.MIN_ELEVATOR_ID || newRequest.getElevatorId() > Config.MAX_ELEVATOR_ID) {
                return false;
            }
            if (newRequest.getTargetFloor() < Config.MIN_FLOOR || newRequest.getTargetFloor() > Config.MAX_FLOOR) {
                return false;
            }
        }
        return true;
    }

}
