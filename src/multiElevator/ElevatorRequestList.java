package multiElevator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ElevatorRequestList {
    private List<Request> requestList = Collections.synchronizedList(new LinkedList<>());
    public void add(Request request) {
        synchronized (requestList) {
            assert requestList.size() == 0 ||
                    request.getTimeArrive() >= requestList.get(requestList.size() - 1).getTimeArrive();
            requestList.add(request);
        }
    }
    public List<Request> getList() {
        return requestList;
    }
    public List<Request> getRequestsByFloor(int floor) {
        LinkedList<Request> retList = new LinkedList<>();
        synchronized (requestList) {
            for (Request req : requestList) {
                if (req.getFloor() == floor) {
                    retList.add(req);
                }
            }
        }
        return retList;
    }
    public List<Request> removeRequestsByFloor(int floor) {
        LinkedList<Request> retList = new LinkedList<>();
        synchronized (requestList) {
            for (Request req : requestList) {
                if (req.getFloor() == floor) {
                    requestList.remove(req);
                    retList.add(req);
                }
            }
        }
        return retList;
    }
    public boolean isEmpty() {
        return requestList.isEmpty();
    }
}
