package multiElevator;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static void main(String args[]) throws IOException {
        ButtonList floorButtonListUp = new ButtonList(Config.FLOOR_COUNT);
        ButtonList floorButtonListDown = new ButtonList(Config.FLOOR_COUNT);
        List<Elevator> elevatorList = new ArrayList<>();
        for (int i = 0; i < Config.ELEVATOR_COUNT; i++) {
            elevatorList.add(new Elevator(i + 1, floorButtonListUp, floorButtonListDown));
        }

        BlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(1024, true);
        RequestScanner requestScanner = new RequestScanner(System.in, requestQueue);

        MultiScheduler multiScheduler = new MultiScheduler(elevatorList, requestQueue, floorButtonListUp, floorButtonListDown);
        requestScanner.start();
        requestScanner.setPriority(10);
        multiScheduler.start();
        for (Elevator elevator : elevatorList) {
            elevator.start();
        }
        for (Elevator elevator : elevatorList) {
            try {
                elevator.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        System.out.println("Test ended.");

    }
}