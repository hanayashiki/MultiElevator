package test;

import multiElevator.Elevator;
import multiElevator.Request;
import multiElevator.RequestScanner;
import multiElevator.Scheduler;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SchedulerTest {
    public static void main(String args[]) throws IOException {
        List<Elevator> elevatorList = Arrays.asList(new Elevator(1), new Elevator(2), new Elevator(3));
        PipedInputStream pipedInputStream = TestInputter.getStream("src/test/tests/test1.txt");
        BlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(1024, true);
        RequestScanner requestScanner = new RequestScanner(pipedInputStream, requestQueue);
        Scheduler scheduler = new Scheduler(elevatorList, requestQueue);
        requestScanner.start();
        scheduler.start();
        for (Elevator elevator : elevatorList) {
            elevator.start();
        }
    }
}
