package multiElevator;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Scheduler extends Thread {
    private List<Elevator> elevatorList;
    private BlockingQueue<Request> requestQueue;
    private ButtonList floorButtonListUp;
    private ButtonList floorButtonListDown;
    private List<Request> pendingRequests = new LinkedList<>();
    private boolean inputEnd = false;

    public Scheduler(List<Elevator> elevatorList, BlockingQueue<Request> requestQueue, ButtonList floorButtonListUp,
                     ButtonList floorButtonListDown) {
        this.elevatorList = elevatorList;
        this.requestQueue = requestQueue;
        this.floorButtonListUp = floorButtonListUp;
        this.floorButtonListDown = floorButtonListDown;
    }

    public void run() {
        try {
            while (true) {
                if (pendingRequests.isEmpty() && inputEnd) {
                    throw new EndOfRequestsException();
                }
                Request newRequest = null;
                newRequest = getRequestToHandle();
                if (newRequest == null) {
                    yield();
                    continue;
                }
                if (newRequest.isEnd()) {
                    inputEnd = true;
                    continue;
                }
                newRequest.setVisited();
                if (newRequest.getType() == Request.Type.FR) {
                    if (!isSameFloorRequest(newRequest) || newRequest.isVisited()) {
                        int floor = newRequest.getFloor();
                        if (newRequest.getDirection() == Direction.UP) {
                            floorButtonListUp.lightUp(floor - 1);
                        }
                        if (newRequest.getDirection() == Direction.DOWN) {
                            floorButtonListDown.lightUp(floor - 1);
                        }
                        boolean assigned = assignFloorRequest(newRequest);
                        if (!assigned) {
                            System.out.println("Not assigned Request: " + newRequest);
                            pendingRequests.add(newRequest);
                        }
                    } else {
                        Output.printSame(newRequest.getOriginString(), newRequest.getTimeArrive());
                    }
                } else {
                    int elevatorId = newRequest.getElevatorId();
                    Elevator assignee = elevatorList.get(elevatorId - 1);
                    synchronized (assignee) {
                        if (!assignee.isSameElevatorRequest(newRequest)) {
                            System.out.println("Assigned " + newRequest + " to " + assignee + " @" + Global.getRelativeTime());
                            assignee.pickUp(newRequest);
                            assignee.lightUpForRequest(newRequest);
                        } else {
                            Output.printSame(newRequest.getOriginString(), newRequest.getTimeArrive());
                        }
                    }
                }
            }
        } catch (EndOfRequestsException ee) {
            for (Elevator elevator : elevatorList) {
                elevator.notifyNoMoreRequests();
            }
        }
    }

    private boolean assignFloorRequest(Request newRequest) {
//        // For test
//        synchronized (elevatorList.get(0)) {
//            elevatorList.get(0).pickUp(newRequest);
//        }
//        return false;
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

    public boolean canAssign(Request newRequest) {
        for (Elevator elevator : elevatorList) {
            synchronized (elevator) {
                if (canPickUp(elevator, newRequest) || elevator.isIdle()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Request getRequestToHandle() {
        Request newReq = requestQueue.peek();
        Request pendingReq = null;
        for (Request req : pendingRequests) {
            if (canAssign(req)) {
                pendingRequests.remove(req);
                return req;
            }
        }
        if (newReq != null) {
            try {
                requestQueue.take();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }
        return newReq;
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
            if (elevator.isUnloadingMainRequest() || !elevator.isIdle()) {
                return false;
            }
            if (request.getType() == Request.Type.FR) {
                boolean isSameDirection = elevator.getCurrentDirection() == request.getDirection();
                boolean isBetweenCurrentPositionAndTargetPosition;
                if (elevator.getCurrentDirection() == Direction.UP) {
                    isBetweenCurrentPositionAndTargetPosition =
                            Utils.doubleLess(elevator.getCurrentPosition(), request.getFloor()) &&
                                    Utils.doubleLessEqual(request.getFloor(), elevator.getCurrentTarget());
                } else if (elevator.getCurrentDirection() == Direction.DOWN) {
                    isBetweenCurrentPositionAndTargetPosition =
                            Utils.doubleGreater(elevator.getCurrentPosition(), request.getFloor()) &&
                                    Utils.doubleGreaterEqual(request.getFloor(), elevator.getCurrentTarget());
                } else {
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

    private boolean isSameFloorRequest(Request request) {
        assert request.getType() == Request.Type.FR;
        int floor = request.getFloor();
        if (request.getDirection() == Direction.UP && floorButtonListUp.getLightStatus(floor - 1)) {
            return true;
        }
        if (request.getDirection() == Direction.DOWN && floorButtonListDown.getLightStatus(floor - 1)) {
            return true;
        }
        return false;
    }
}
