package multiElevator;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class MultiScheduler extends Thread {
    /*  OVERVIEW: 调度类，从与 RequestScanner 共享的 requestQueue 读取 request，将 request 分配给 elevatorList
    *       中的 elevator
    * */
    private List<Elevator> elevatorList;
    private BlockingQueue<Request> requestQueue;
    private ButtonList floorButtonListUp;
    private ButtonList floorButtonListDown;
    private List<Request> pendingRequests = new LinkedList<>();
    private boolean inputEnd = false;

    public MultiScheduler(List<Elevator> elevatorList, BlockingQueue<Request> requestQueue, ButtonList floorButtonListUp,
                          ButtonList floorButtonListDown) {
        super();
        this.elevatorList = elevatorList;
        this.requestQueue = requestQueue;
        this.floorButtonListUp = floorButtonListUp;
        this.floorButtonListDown = floorButtonListDown;
    }

    public void run() {
        /*  @REQUIRES: \all Request req in pendingRequests; req.isVisited() &&
        *    (systemTime == req.getTimeArrival ==> cannotAssign(req)) && isSourceFromOutput(requestQueue)
        *   @MODIFIES: \all Request req; isSameFloorRequest(newRequest); Output.printSame(newRequest)
        *   @EFFECTS:
        *   1. \all Request req in requestQueue cannotAssign(req) && hasNotReassigned(req) ==> req in pendingRequests
        *   2. \all Request req in requestQueue canPickUp(req) ==>
        *        \min(getTotalTravelDistance){elevator.canPickUp(req) | elevator in elevatorList}
        *   3. \all Request req in requestQueue !(canPickUp(req)) && canRespond(req) ==>
        *        \min(getTotalTravelDistance){elevator.canRespond(req) | elevator in elevatorList}
        *   4. \exist req in requestQueue && (t == req.getTimeArrival()) ==> \all elevator; elevator.notifyNoMoreRequests();
        *   @THREAD_REQUIRES:
        *       locked({elevator in elevatorList}) && locked(floorButtonListUp) && locked(floorButtonListDown) &&
        *       locked(requestQueue)
        *   @THREAD_EFFECTS:
        *       locked({elevator in elevatorList}) && locked(floorButtonListUp) && locked(floorButtonListDown) &&
        *       locked(requestQueue)
        * */
        try {
            while (true) {
                if (pendingRequests.isEmpty() && inputEnd) {
                    throw new EndOfRequestsException();
                }
                Request newRequest = getRequestToHandle();
                if (newRequest == null) {
                    continue;
                }
                if (newRequest.isEnd()) {
                    inputEnd = true;
                    continue;
                }
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
                            newRequest.setVisited();
                            pendingRequests.add(newRequest);
                        }
                    } else {
                        Output.printSame(newRequest);
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
                            Output.printSame(newRequest);
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
            Elevator assignee = getElevatorOfMinTraveledDistance(canLift);
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
                // System.out.println(elevator + ": idle = " + elevator.isIdle());
                if (elevator.isIdle()) {
                    canLift.add(elevator);
                }
            }
        }
        if (!canLift.isEmpty()) {
            Elevator assignee = getElevatorOfMinTraveledDistance(canLift);
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
        for (Request req : pendingRequests) {
            if (canAssign(req)) {
                pendingRequests.remove(req);
                return req;
            }
        }
        Request newReq = requestQueue.peek();
        if (newReq != null) {
            requestQueue.remove();
        }
        return newReq;
    }

    private Elevator getElevatorOfMinTraveledDistance(List<Elevator> elevatorList) {
        List<Elevator> tempList = new ArrayList<>(elevatorList);
        tempList.sort(new Comparator<Elevator>() {
            @Override
            public int compare(Elevator o1, Elevator o2) {
                return o1.getTotalTravelDistance() - o2.getTotalTravelDistance();
            }
        });
        int topIndex = -1;
        Elevator first = tempList.get(0);
        for (Elevator elevator : tempList) {
            synchronized (elevator) {
                if (elevator.getTotalTravelDistance() == first.getTotalTravelDistance()) {
                    topIndex++;
                }
            }
        }
        int index = new Random().nextInt(topIndex + 1);
        return tempList.get(index);
    }

    private boolean canPickUp(Elevator elevator, Request request) {
        synchronized (elevator) {
            if (elevator.isUnloadingMainRequest() || elevator.isIdle()) {
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
        // System.out.println("reqfloor: " +  request.getFloor() + ", " + floorButtonListUp);
        if (request.getDirection() == Direction.UP && floorButtonListUp.getLightStatus(floor - 1)) {
            // System.out.println(true);
            return true;
        }
        if (request.getDirection() == Direction.DOWN && floorButtonListDown.getLightStatus(floor - 1)) {
            // System.out.println(false);
            return true;
        }
        return false;
    }
}
