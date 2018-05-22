package multiElevator;

import java.util.ArrayList;
import java.util.List;

enum Status {
    /*  @OVERVIEW: 电梯状态枚举类型，UNLOADING 表示 [t_开门时刻, t_关门时刻]，MOVING 表示其它状态
    * */
    MOVING, UNLOADING
}

public class Elevator extends Thread {
    /*  @OVERVIEW: 电梯仿真，线程运行时对电梯运行状态和请求状况进行实时更新
        @INHERIT: Thread | run
     */
    private int idx;
    private boolean noMoreRequest = false;

    private double currentPosition = Config.MIN_FLOOR;
    private Direction currentDirection = Direction.STILL;
    private Status status = Status.MOVING;
    private long unloadingTime = -1;
    private long lastSimulatingTime = System.nanoTime();
    private long minWaitingTime = Long.MAX_VALUE;

    private int currentTarget = -1;

    private double totalTravelDistance = 0;

    private ElevatorRequestList elevatorRequestList = new ElevatorRequestList();
    private List<Request> currentFinishedRequestList = new ArrayList<>(1024);

    private ButtonList floorButtonListUp;
    private ButtonList floorButtonListDown;
    private ButtonList elevatorButtonList = new ButtonList(Config.FLOOR_COUNT);

    public int lastFloorReached = Config.MIN_FLOOR;

    private boolean startFast = false; // After unloading, prevent waiting ELEVATOR_MILLISECOND_PER_UPDATE

    public Elevator(int idx, ButtonList floorButtonListUp, ButtonList floorButtonListDown) {
        /*  @REQUIRES: None
        *   @MODIFIES: None
        *   @EFFECTS: this.idx == idx; this.floorButtonListUp == floorButtonListUp;
        *   this.floorButtonListDown == floorButtonListDown;
        * */
        this.idx = idx;
        this.floorButtonListUp = floorButtonListUp;
        this.floorButtonListDown = floorButtonListDown;
    }

    public String toString() {
        /*  @REQUIRES: None
            @MODIFIES: None
            @EFFECTS: \result = "Elevator#" + this.idx;
        * */
        return "Elevator#" + this.getIdx();
    }

    synchronized private void move(double distance) {
        /*  @REQUIRES: None
            @MODIFIES: totalTravelDistance = \old(totalTravelDistance) + |distance|
            @EFFECTS: currentPosition == \old(currentPosition) + distance
            @THREAD_REQUIRES：None
            @THREAD_EFFECTS: locked(this)
        * */
        System.out.println(currentDirection);
        assert !Utils.doubleGreater(currentPosition + distance, Config.MAX_FLOOR);
        assert !Utils.doubleLess(currentPosition + distance, Config.MIN_FLOOR);
        currentPosition += distance;
        totalTravelDistance += Math.abs(distance);
    }

    public void run() {
        /*  @REQUIRES:
            1. \all Request req; req in elevatorRequestList; req is valid
            2. currentTarget == the main request should be carrying now
            3. minWaitingTime is correctly the minimum interval of simulation
            4. isValid(noMoreRequest)
            @MODIFIES:
            1. lastSimulatingTime == System.nanoTime() when simulate() is run.
            @EFFECTED:
            1. \all Request req; req in elevatorRequestList; req is so far cannot be finished,
                updated each time simulate() is run.
            2. currentPosition == latest simulated position updated each time simulate() is run.
            3. currentDirection == latest simulated direction updated each time simulate() is run.
            4. status == UNLOADING if the elevator has finished a request and opening and closing the door,
                updated each time simulate() is run.
            5. status == MOVING if the elevator is moving for a request or idle ,
                updated each time simulate() is run.
            6. totalTravelDistance == \sum |distance|, each time move is called,
                updated each time simulate() is run.
            7. currentTarget == next main request according to guidebook when \old(currentTarget) is finished.
            8. noMoreRequest ===> this.exit().EFFECTS
            @THREAD_REQUIRES:
            1. \locked(floorButtonListUp) && \locked(floorButtonListDown) && \locked(elevatorButtonList)
                && \locked(elevatorRequestList) && \locked(this)
            @THREAD_EFFECTS:
            1. \locked(floorButtonListUp) && \locked(floorButtonListDown) && \locked(elevatorButtonList)
                && \locked(elevatorRequestList) && \locked(this)
        * */
        long timeStart;
        long elapsedTime;
        long simulateCost = 1000000;
        try {
            while (true) {
                while (elevatorRequestList.isEmpty() && status == Status.MOVING) {
                    if (this.noMoreRequest) {
                        throw new EndOfRequestsException();
                    }
                    lastSimulatingTime = System.nanoTime();
                    yield();
                }
                long simuStart = System.nanoTime();
                simulate(Config.ELEVATOR_MILLISECOND_PER_UPDATE * 1000000);
                lastSimulatingTime = System.nanoTime();
                simulateCost = lastSimulatingTime - simuStart; // learning simulateCost on current machine
                timeStart = System.nanoTime();
                long interval = Math.min(Config.ELEVATOR_MILLISECOND_PER_UPDATE, minWaitingTime);
                while ((elapsedTime = (System.nanoTime() - timeStart + simulateCost)) / 1e6 < interval
                        && !startFast) {
                }
                startFast = false;
            }
        } catch (EndOfRequestsException ee) {
        }
    }

    synchronized private void simulate(long elapsedTime) {
        /*  @REQUIRES:
            1. \all Request req; req in elevatorRequestList; req is valid
            2. currentTarget == the main request should be carrying now
            @MODIFIES: None
            @EFFECTED:
            1. \all Request req; req in elevatorRequestList; req is so far cannot be finished,
                updated each time simulate() is run.
            2. currentPosition == latest simulated position updated each time simulate() is run.
            3. currentDirection == latest simulated direction updated each time simulate() is run.
            4. status == UNLOADING if the elevator has finished a request and opening and closing the door,
                updated each time simulate() is run.
            5. status == MOVING if the elevator is moving for a request or idle ,
                updated each time simulate() is run.
            6. totalTravelDistance == \sum |distance|, each time move is called,
                updated each time simulate() is run.
            7. currentTarget == next main request according to guidebook when \old(currentTarget) is finished.
            @THREAD_REQUIRES:
            1. \locked(floorButtonListUp) && \locked(floorButtonListDown) && \locked(elevatorButtonList)
                && \locked(elevatorRequestList) && \locked(this)
            @THREAD_EFFECTS:
            1. \locked(floorButtonListUp) && \locked(floorButtonListDown) && \locked(elevatorButtonList)
                && \locked(elevatorRequestList) && \locked(this)
        * */
        switch (status) {
            case MOVING:
                boolean arrived = false;
                arrived = checkFloorArrival(); // deletes requests in the queue, does output
                if (arrived) {
                    setOnArrival();
                    System.out.println(this + " door opens. @" + Global.getRelativeTime());
                }
                else {
                    move(Utils.directionToSign(this.currentDirection) * elapsedTime / 1e9 / Config.TIME_PER_FLOOR);
                    System.out.println(this + ": " + this.currentPosition + " @" + Global.getRelativeTime());
                }
                break;
            case UNLOADING:
                if (System.nanoTime() - unloadingTime >= (long) Config.TIME_PER_OPEN * 1000 * 1000000 + 1) {
                    System.out.println(this + " door closes. @" + Global.getRelativeTime());
                    for (Request request : currentFinishedRequestList) {
                        if (request.getType() == Request.Type.FR) {
                            if (request.getDirection() == Direction.UP) {
                                lightDownForFinished(request, this.floorButtonListUp);
                            }
                            if (request.getDirection() == Direction.DOWN) {
                                lightDownForFinished(request, this.floorButtonListDown);
                            }
                        } else {
                            lightDownForFinished(request, this.elevatorButtonList);
                        }
                    }
                    currentFinishedRequestList.clear();
                    if (Utils.doubleEqual(this.getCurrentPosition(), this.currentTarget)
                            && !this.elevatorRequestList.isEmpty()) {
                        Request lastUnfinishedRequest = this.elevatorRequestList.getHead();
                        this.setMain(lastUnfinishedRequest);
                    }
                    cancelOnFinished();
                }
                break;
        }
    }

    synchronized boolean isUnloadingMainRequest() {
        // @REQUIRES: Utils.doubleNearInt(this.currentPosition)
        // @MODIFIES: None
        // @EFFECTS: \result ==
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return this.status == Status.UNLOADING && ((int) Math.round(this.currentPosition) == this.currentTarget);
    }

    synchronized private void setOnArrival() {
        // @REQUIRES: right after this reaches a floor for a request
        // @MODIFIES: None
        // @EFFECTS: status == Status.UNLOADING &&
        // unloadingTime == the time when the floor is reached && minWaitingTime == 0;
        status = Status.UNLOADING;
        unloadingTime = System.nanoTime();
        minWaitingTime = 0;
    }

    synchronized private void cancelOnFinished() {
        // @REQUIRES: right after this finished opening and closing a door
        // @MODIFIES: None
        // @EFFECTS: status == Status.MOVING;
        status = Status.MOVING;
        minWaitingTime = Long.MAX_VALUE;
        startFast = true;
    }

    synchronized private boolean checkFloorArrival() {
        // @REQUIRES: Utils.doubleNearInt(this.currentPosition)
        //              && \all Request req; req in elevatorRequestList; isValid(req)
        // @MODIFIES:
        // 1. \all request; request in old(elevatorRequestList) && Utils.doubleEqual(request.getFloor == currentPosition);
        //          Output.outputStream.newLine == information of finishing request
        // 2. totalTravelDistance == \old(totalTravelDistance) + Math.abs(lastFloorReached - floor)
        // 3. totalTravelDistance == floor
        // @EFFECTS:
        // 1. \all request; request in old(elevatorRequestList) && Utils.doubleEqual(request.getFloor == currentPosition);
        //         !(request in elevatorRequestList)
        // 2. \exists request; request in old(elevatorRequestList) &&
        //         Utils.doubleEqual(request.getFloor == currentPosition) <===> \result == true
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        boolean ret = false;
        if (isFloorArrived()) {
            // indicates a floor
            int floor = (int) Math.round(this.currentPosition);
            List<Request> requestsFinished = elevatorRequestList.removeRequestsByFloor(floor);
            currentFinishedRequestList.addAll(requestsFinished);
            for (Request request : requestsFinished) {
                Output.printArrival(request, this.getIdx(), request.getFloor(),
                        this.getTotalTravelDistance(), this.currentDirection, Global.getST());
                System.out.println("Finished " + request + " by " + this + " @" + Global.getRelativeTime());
                ret = true;
            }
            this.totalTravelDistance += Math.abs(lastFloorReached - floor);
            totalTravelDistance = floor;
        }
        return ret;
    }

    synchronized private boolean isFloorArrived() {
        // @REQUIRES: status == Status.MOVING
        // @MODIFIES: None
        // @EFFECTS: \result == Utils.doubleNearInt(this.currentPosition)
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return Utils.doubleNearInt(this.currentPosition);
    }

    synchronized public void pickUp(Request request) {
        // @REQUIRES: isValid(request)
        // @MODIFIES: None
        // @EFFECTS:
        // 1. elevatorRequestList.isEmpty() ==> setMain(request).EFFECTS
        // 2. elevatorRequestList.requestList.size() = old(elevatorRequestList.requestList.size()) + 1
        // 3. elevatorRequestList.requestList[elevatorRequestList.requestList.size() - 1] = request
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        if (elevatorRequestList.isEmpty()) {
            setMain(request);
        }
        elevatorRequestList.add(request);
    }

    private void setMain(Request request) {
        // @REQUIRES: isValid(request)
        // @MODIFIES: None
        // @EFFECTS:
        // 1. this.currentDirection == Utils.signToDirection(request.getFloor() - currentPosition)
        // 2. this.currentTarget == request.getFloor()
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        this.currentDirection = Utils.signToDirection(request.getFloor() - currentPosition);
        this.currentTarget = request.getFloor();
    }

    synchronized public void notifyNoMoreRequests() {
        // @REQUIRES: isValid(request)
        // @MODIFIES: None
        // @EFFECTS:
        // 1. this.noMoreRequest == true
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        this.noMoreRequest = true;
    }

    synchronized public boolean isSameElevatorRequest(Request request) {
        // @REQUIRES: request.getType() == Request.Type.ER
        // @MODIFIES: None
        // @EFFECTS: \result == this.elevatorButtonList.getLightStatus(request.getFloor() - 1);
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        assert request.getType() == Request.Type.ER;
        return this.elevatorButtonList.getLightStatus(request.getFloor() - 1);
    }

    synchronized public void lightUpForRequest(Request request) {
        // @REQUIRES: request.getType() == Request.Type.ER
        // @MODIFIES: None
        // @EFFECTS: this.elevatorButtonList.lightUp(request.getFloor() - 1)
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        assert request.getType() == Request.Type.ER;
        this.elevatorButtonList.lightUp(request.getFloor() - 1);
    }

    synchronized public void lightDownForFinished(Request request, ButtonList buttonList) {
        // @REQUIRES: request.getType() == Request.Type.ER
        // @MODIFIES: None
        // @EFFECTS: this.elevatorButtonList.lightDown(request.getFloor() - 1)
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        buttonList.lightDown(request.getFloor() - 1);
    }


    synchronized public int getIdx() {
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS: \result == this.idx
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return idx;
    }

    synchronized public double getCurrentPosition() {
        // @REQUIRES: lastSimulatingTime == old(System.nanoTime())
        // @MODIFIES: None
        // @EFFECTS: \result == currentPosition +
        //                Utils.directionToSign(this.currentDirection) * (timeNotConsiderd)
        //                / 1e9 / Config.TIME_PER_FLOOR;
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        long timeNotConsiderd = System.nanoTime() - lastSimulatingTime;
        // System.out.println(currentPosition);
        return currentPosition +
                Utils.directionToSign(this.currentDirection) * (timeNotConsiderd) / 1e9 / Config.TIME_PER_FLOOR;
    }

    synchronized public Direction getCurrentDirection() {
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS: \result == this.currentDirection
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return currentDirection;
    }

    synchronized public int getCurrentTarget() {
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS: \result == this.currentTarget
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return currentTarget;
    }

    synchronized public ElevatorRequestList getElevatorRequestList() {
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS: \result == this.elevatorRequestList
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return elevatorRequestList;
    }

    synchronized public int getTotalTravelDistance() {
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS: \result == (int) Math.floor(totalTravelDistance);
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return (int) Math.floor(totalTravelDistance);
    }

    synchronized public boolean isIdle() {
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS: \result == this.elevatorRequestList.isEmpty();
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
        return this.elevatorRequestList.isEmpty();
    }

}
