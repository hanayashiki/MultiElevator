package multiElevator;

public class Request {
    enum Type {
        FR, ER
    }

    private Type type;

    private Direction direction;
    private int floor;
    private int elevatorId;
    private long timeArrive;
    String originString;

    Request(Type type, int floor, Direction direction, long timeArrive, String originString) {
        assert type == Type.FR;
        this.type = type;
        this.floor = floor;
        this.direction = direction;
        this.timeArrive = timeArrive;
        this.originString = originString;
    }

    Request(Type type, int elevatorId, int floor, long timeArrive, String originString) {
        assert type == Type.ER;
        this.type = type;
        this.elevatorId = elevatorId;
        this.floor = floor;
        this.timeArrive = timeArrive;
        this.originString = originString;
    }

    public Type getType() {
        return type;
    }

    public long getTimeArrive() {
        return timeArrive;
    }

    // type == FR

    public int getCallingFloor() {
        assert type == Type.FR;
        return floor;
    }

    public Direction getDirection() {
        assert type == Type.FR;
        return direction;
    }

    // type == ER

    public int getElevatorId() {
        assert type == Type.ER;
        return elevatorId;
    }

    public int getTargetFloor() {
        assert type == Type.ER;
        return floor;
    }

    @Override
    public String toString() {
        if (type == Type.FR) {
            return "{ type: FR, " + "callingFloor: " + getCallingFloor() + ", " +
                    "direction: " + getDirection() + ", " + "timeArrive: " + getTimeArrive() + " }";
        }
        if (type == Type.ER) {
            return "{ type: ER, " + "elevatorId: " + getElevatorId() + ", " +
                    "targetFloor: " + getTargetFloor() + ", " + "timeArrive: " + getTimeArrive() + " }";
        }
        return "Untyped";
    }
}

