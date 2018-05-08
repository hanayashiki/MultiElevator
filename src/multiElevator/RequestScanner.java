package multiElevator;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class RequestScanner extends Thread {
    private Scanner scanner;
    private BlockingQueue<Request> requestQueue;

    public RequestScanner(InputStream inputStream, BlockingQueue<Request> requestQueue) {
        this.scanner = new Scanner(inputStream);
        this.requestQueue = requestQueue;
    }

    public void run() {
        while (true) {
            while (!scanner.hasNextLine()) {}
            String inputLine = scanner.nextLine();
            long inputTime = System.currentTimeMillis();
            if (Global.timeZeroPoint == -1) {
                Global.timeZeroPoint = inputTime;
            }
            String [] splitted = inputLine.split(";");
            for (String inputString : splitted) {
                try {
                    Request newRequest = Parser.parse(inputString, inputTime);
                    System.out.println("receive new request: " + newRequest + " @" + Global.milliTimeToSecond(inputTime, 1));
                    try {
                        requestQueue.put(newRequest);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                } catch (InputException ie) {
                    Output.printInvalid(ie.message, inputTime);
                }
            }
        }
    }
}
