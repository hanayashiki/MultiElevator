package multiElevator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.Random;

public class Scheduler extends Thread {
    List<Elevator> elevatorList;
    private BlockingQueue<Request> requestQueue;

    public Scheduler(List<Elevator> elevatorList, BlockingQueue<Request> requestQueue) {
        this.elevatorList = elevatorList;
        this.requestQueue = requestQueue;
    }

    public void run() {
        try {
            while (true) {
                Request newRequest = null;
                try {
                    newRequest = requestQueue.take();
                    if (newRequest.isEnd()) {
                        for (Elevator elevator : elevatorList) {
                            synchronized (elevator) {
                                elevator.notifyNoMoreRequests();
                            }
                        }
                        throw new EndOfRequestsException();
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                if (newRequest.getType() == Request.Type.FR) {
                    boolean assigned = asssignFloorRequest(newRequest);
                    // TODO: pending a Request ? or not
                    if (!assigned) {
                        System.out.println("Not assigned Request: " + newRequest);
                    }
                } else {
                    int elevatorId = newRequest.getElevatorId();
                    Elevator assignee = elevatorList.get(elevatorId - 1);
                    synchronized (assignee) {
                        System.out.println("Assigned " + newRequest + " to " + assignee + " @" + Global.getRelativeTime());
                        assignee.pickUp(newRequest);
                    }
                }
            }
        } catch (EndOfRequestsException ee) {

        }
    }

    private boolean asssignFloorRequest(Request newRequest) {
        List<Elevator> canLift = new ArrayList<>(elevatorList.size());
        // 1. check if any elevator can pick up
        for (Elevator elevator : elevatorList) {
            synchronized (elevator) {
                if (canPickUp(elevator, newRequest)) {
                    canLift.add(elevator);
                }
            }
        }
        if (!canLift.isEmpty()) {
            Elevator assignee = getElevaterOfMinTraveledDistance(canLift);
            synchronized (assignee) {
                System.out.println("Assigned " + newRequest + " to " + assignee + " @" + Global.getRelativeTime());
                assignee.pickUp(newRequest);
            }
            return true;
        }
        // 2. check if any elevator is free and able to respond
        canLift.clear();
        for (Elevator elevator : elevatorList) {
            synchronized (elevator) {
                System.out.println(elevator + ": idle = " + elevator.isIdle());
                if (elevator.isIdle()) {
                    canLift.add(elevator);
                }
            }
        }
        if (!canLift.isEmpty()) {
            Elevator assignee = getElevaterOfMinTraveledDistance(canLift);
            synchronized (assignee) {
                System.out.println("Assigned " + newRequest + " to " + assignee + " @" + Global.getRelativeTime());
                assignee.pickUp(newRequest);
            }
            return true;
        } else {
            return false;
        }
    }

    private Elevator getElevaterOfMinTraveledDistance(List<Elevator> elevatorList) {
        boolean allEqual = true;
        Elevator elevator = elevatorList.get(0);
        for (int i = 1; i < elevatorList.size(); i++) {
            if (elevatorList.get(i).getTotalTravelDistance() < elevator.getTotalTravelDistance()) {
                allEqual = false;
                elevator = elevatorList.get(i);
            }
        }
        if (allEqual) {
            // return elevatorList.get(new Random().nextInt(elevatorList.size()));
        }
        return elevator;
    }

    private boolean canPickUp(Elevator elevator, Request request) {
        synchronized (elevator) {
            if (request.getType() == Request.Type.FR) {
                boolean isSameDirection = elevator.getCurrentDirection() == request.getDirection();
                boolean isBetweenCurrentPositionAndTargetPosition;
                if (elevator.getCurrentDirection() == Direction.UP) {
                    isBetweenCurrentPositionAndTargetPosition =
                            Utils.doubleLess(elevator.getCurrentPosition(),  request.getFloor()) &&
                            Utils.doubleLessEqual(request.getFloor(), elevator.getCurrentTarget());
                }
                else if (elevator.getCurrentDirection() == Direction.DOWN) {
                    isBetweenCurrentPositionAndTargetPosition =
                            Utils.doubleGreater(elevator.getCurrentPosition(), request.getFloor()) &&
                            Utils.doubleGreaterEqual(request.getFloor(), elevator.getCurrentTarget());
                }
                else {
                    return false;
                }
                if (!isSameDirection) {
                    return false;
                }
                if (!isBetweenCurrentPositionAndTargetPosition) {
                    return false;
                }
            }
            if (request.getType() == Request.Type.ER) {
                if (elevator.getCurrentDirection() == Direction.UP) {
                    return Utils.doubleLess(elevator.getCurrentPosition(), request.getFloor());
                } else if (elevator.getCurrentDirection() == Direction.DOWN) {
                    return Utils.doubleGreater(elevator.getCurrentPosition(), request.getFloor());
                }
            }
            return true;
        }
    }
}
