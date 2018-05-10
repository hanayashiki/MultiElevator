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
        try {
            while (scanner.hasNextLine()) {
                String inputLine = scanner.nextLine();
                long inputTime = System.currentTimeMillis();
                if (Global.timeZeroPoint == -1) {
                    Global.timeZeroPoint = inputTime;
                }
                String [] splitted = inputLine.split(";");
                for (String inputString : splitted) {
                    try {
                        Request newRequest = Parser.parse(inputString, inputTime);
                        if (newRequest == null) {
                            throw new EndOfRequestsException();
                        }
                        System.out.println("receive new request: " + newRequest + " @" + (Global.getRelativeTime()));
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
        } catch (EndOfRequestsException ee) {
            System.out.println("Scanner ends");
            try {
                requestQueue.put(Request.endSignal());
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
