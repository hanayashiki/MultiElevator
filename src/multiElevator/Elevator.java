package multiElevator;

import java.util.ArrayList;
import java.util.List;

enum Status {
    MOVING, UNLOADING
}

public class Elevator extends Thread {
    private int idx;
    private boolean noMoreRequest = false;

    private double currentPosition = Config.MIN_FLOOR;
    private Direction currentDirection = Direction.STILL;
    private Status status = Status.MOVING;
    private long unloadingTime = -1;

    private int currentTarget = -1;

    private double totalTravelDistance = 0;

    private ElevatorRequestList elevatorRequestList = new ElevatorRequestList();
    private List<Request> currentFinishedRequestList = new ArrayList<>(1024);

    private ButtonList floorButtonList;
    private ButtonList elevatorButtonList = new ButtonList(Config.FLOOR_COUNT);

    public Elevator(int idx, ButtonList floorButtonList) {
        this.idx = idx;
        this.floorButtonList = floorButtonList;
    }

    public String toString() {
        return "Elevator#" + this.getIdx();
    }

    synchronized private void move(double distance) {
        assert currentDirection == Direction.UP;
        assert !Utils.doubleGreater(currentPosition + distance, Config.MAX_FLOOR);
        assert !Utils.doubleLess(currentPosition + distance, Config.MIN_FLOOR);
        currentPosition += distance;
        totalTravelDistance += Math.abs(distance);
    }

    public void run() {
        long timeStart = System.nanoTime();
        long elapsedTime;
        long simulateCost = 3000;
        try {
            while (true) {
                while (elevatorRequestList.isEmpty() && status == Status.MOVING) {
                    if (this.noMoreRequest) {
                        throw new EndOfRequestsException();
                    }
                    yield();
                }
                timeStart = System.nanoTime();
                // System.out.println(this + " starts working: @" + Global.getRelativeTime());
                while ((elapsedTime = (System.nanoTime() - timeStart)) / 1e6 <= Config.ELEVATOR_MILLISECOND_PER_UPDATE) {
                }
                // System.out.println(1.0 * elapsedTime / 1e6);
                long nano = System.nanoTime();
                simulate(elapsedTime + simulateCost);
                simulateCost = ((System.nanoTime() - nano) + 4 * simulateCost) / 5; // learning simulateCost on current machine
            }
        } catch (EndOfRequestsException ee) {
        }
    }

    synchronized private void simulate(long elapsedTime) {
        // First handle requests that can be done in this moment
        switch (status) {
            case MOVING:
                boolean arrived = false;
                arrived = checkFloorArrival(elapsedTime);
                if (arrived) {
                    setOnArrival();
                    System.out.println(this + " door opens. @" + Global.getRelativeTime());
                }
                move(Utils.directionToSign(this.currentDirection) * elapsedTime / 1e9 / Config.TIME_PER_FLOOR);
                arrived |= checkFloorArrival(elapsedTime);
                if (arrived) {
                    setOnArrival();
                    System.out.println(this + " door opens. @" + Global.getRelativeTime());
                }
                break;
            case UNLOADING:
                if (System.currentTimeMillis() - unloadingTime >= (long)Config.TIME_PER_OPEN * 1000) {
                    status = Status.MOVING;
                    System.out.println(this + " door closes. @" + Global.getRelativeTime());
                    for (Request request : currentFinishedRequestList) {
                        if (request.getType() == Request.Type.FR) {
                            floorButtonList.lightDown(request.getFloor() - 1);
                        }
                    }
                    currentFinishedRequestList.clear();
                }
                break;
        }
    }

    synchronized private void setOnArrival() {
        status = Status.UNLOADING;
        unloadingTime = System.currentTimeMillis();
    }

    synchronized private boolean checkFloorArrival(long elapsedTime) {
        boolean ret = false;
        if (isFloorArrived()) {
            // indicates a floor
            int floor = (int) Math.round(this.currentPosition);
            List<Request> requestsFinished = elevatorRequestList.removeRequestsByFloor(floor);
            currentFinishedRequestList.addAll(requestsFinished);
            if (!requestsFinished.isEmpty()) {
                for (Request request : requestsFinished) {
                    // TODO: output formally
                    System.out.println("Finished " + request + " by " + this + " @" + Global.getRelativeTime());
                    ret = true;
                }
            }
            // TODO: if have finished main request
        }
        return ret;
    }

    synchronized private boolean isFloorArrived() {
        return Utils.doubleNearInt(this.currentPosition);
    }

    synchronized public void pickUp(Request request) {
        if (elevatorRequestList.isEmpty()) {
            setMain(request);
        }
        elevatorRequestList.add(request);
    }

    private void setMain(Request request) {
        this.currentDirection = Utils.signToDirection(request.getFloor() - currentPosition);
        this.currentTarget = request.getFloor();
    }

    synchronized public void notifyNoMoreRequests() {
        this.noMoreRequest = true;
    }

    synchronized public boolean isSameElevatorRequest(Request request) {
        assert request.getType() == Request.Type.ER;
        return this.elevatorButtonList.getLightStatus(request.getFloor() - 1);
    }

    synchronized public void lightUpForRequest(Request request) {
        assert request.getType() == Request.Type.ER;
        this.elevatorButtonList.lightUp(request.getFloor() - 1);
    }

    synchronized public int getIdx() {
        return idx;
    }

    synchronized public double getCurrentPosition() {
        return currentPosition;
    }

    synchronized public Direction getCurrentDirection() {
        return currentDirection;
    }

    synchronized public int getCurrentTarget() {
        return currentTarget;
    }

    synchronized public ElevatorRequestList getElevatorRequestList() {
        return elevatorRequestList;
    }

    synchronized public double getTotalTravelDistance() {
        return totalTravelDistance;
    }

    synchronized public boolean isIdle() {
        return this.elevatorRequestList.isEmpty();
    }

}
