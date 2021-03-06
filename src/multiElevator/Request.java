package multiElevator;

public class Request {
    /*
    *   @OVERVIEW: 不可变类，用于记录 Request 的信息
    *
    * */
    enum Type {
        FR, ER
    }

    private Type type;

    private Direction direction;
    private int floor;
    private int elevatorId;
    private long timeArrive;
    private boolean end;
    private boolean visited = false;

    String originString;

    Request() {
        this.type = Type.ER;
    }

    Request(Type type, int floor, Direction direction, long timeArrive, String originString) {
        assert type == Type.FR;
        this.type = type;
        this.floor = floor;
        this.direction = direction;
        this.timeArrive = timeArrive;      // 到达时的 System.currentTimeMillis()
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

    public static Request endSignal() {
        Request endSignal = new Request();
        endSignal.end = true;
        return endSignal;
    }

    public void setVisited() {
        visited = true;
    }

    public boolean isVisited() {
        return visited;
    }

    public Type getType() {
        return type;
    }

    public long getTimeArrive() {
        return timeArrive;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isEnd() {
        return end;
    }

    public String getOriginString() {
        return originString;
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
        return this.getOriginString().trim();
    }

}

