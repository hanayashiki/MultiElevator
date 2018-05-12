package test;

import multiElevator.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SchedulerTest {
    public static void main(String args[]) throws IOException {
        ButtonList floorButtonList = new ButtonList(Config.FLOOR_COUNT);
        List<Elevator> elevatorList = new ArrayList<>();
        for (int i = 0; i < Config.ELEVATOR_COUNT; i++) {
            elevatorList.add(new Elevator(i + 1, floorButtonList));
        }
        PipedInputStream pipedInputStream = TestInputter.getStream("src/test/tests/test_not_enable.txt");
        BlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(1024, true);
        RequestScanner requestScanner = new RequestScanner(pipedInputStream, requestQueue);

        Scheduler scheduler = new Scheduler(elevatorList, requestQueue, floorButtonList);
        requestScanner.start();
        scheduler.start();
        for (Elevator elevator : elevatorList) {
            elevator.start();
        }
    }
}
