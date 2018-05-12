package multiElevator;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ElevatorRequestList {
    private List<Request> requestList = Collections.synchronizedList(new LinkedList<>());
    public void add(Request request) {
        synchronized (this) {
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
        synchronized (this) {
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
        synchronized (this) {
            Iterator<Request> iterator = requestList.iterator();
            while (iterator.hasNext()) {
                Request req = iterator.next();
                if (req.getFloor() == floor) {
                    iterator.remove();
                    retList.add(req);
                }
            }
        }
        return retList;
    }
    public boolean isEmpty() {
        return requestList.isEmpty();
    }
    public Request getHead() {
        synchronized (this) {
            return requestList.get(0);
        }
    }
}
