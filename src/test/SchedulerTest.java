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
        ButtonList floorButtonListUp = new ButtonList(Config.FLOOR_COUNT);
        ButtonList floorButtonListDown = new ButtonList(Config.FLOOR_COUNT);
        List<Elevator> elevatorList = new ArrayList<>();
        for (int i = 0; i < Config.ELEVATOR_COUNT; i++) {
            elevatorList.add(new Elevator(i + 1, floorButtonListUp, floorButtonListDown));
        }
        PipedInputStream pipedInputStream = TestInputter.getStream("src/test/tests/test_elevator_select.txt");
        BlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(1024, true);
        RequestScanner requestScanner = new RequestScanner(pipedInputStream, requestQueue);

        Scheduler scheduler = new Scheduler(elevatorList, requestQueue, floorButtonListUp, floorButtonListDown);
        requestScanner.start();
        scheduler.start();
        for (Elevator elevator : elevatorList) {
            elevator.start();
        }
    }
}
