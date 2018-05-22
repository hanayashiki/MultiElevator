package multiElevator;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ElevatorRequestList {
    // @OVERVIEW: 一个用来包装线程安全的电梯请求队列的容器
    private List<Request> requestList = Collections.synchronizedList(new LinkedList<>());
    public void add(Request request) {
        // @REQUIRES:
        // 1. \all int i, int j; i < j; old(requestList.get(i)) < old(requestList.get(j))
        // 2. requestList.size() == 0 ||
        //                    request.getTimeArrive() >= requestList.get(requestList.size() - 1).getTimeArrive();
        // @MODIFIRES: None
        // @EFFECTS:
        // 1. \all int i, int j; i < j; old(requestList.get(i)) < old(requestList.get(j))
        // 2. requestList.size() == requestList.size() + 1
        // 3. requestList.get(requestList.size() - 1) == request
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
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
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS: \result == List<Request> retList && \all Request req in requestList; req.getFloor == floor;
        //          req in retList;
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
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
        // @REQUIRES: None
        // @MODIFIES: None
        // @EFFECTS:
        // 1. \all Request req in old(requestList); req.getFloor == floor; req in retList;
        // 2. \all Request req in old(requestList); req.getFloor == floor; req not in requestList;
        // @THREAD_REQUIRES: None
        // @THREAD_EFFECTS: locked(this)
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
