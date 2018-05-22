package multiElevator;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class RequestScanner extends Thread {
    /*
    *   @OVERVIEW: 扫描读入类，实现与仿真异步的读取
    * */
    private Scanner scanner;
    private BufferedReader bufferedInputStream;
    private BlockingQueue<Request> requestQueue;

    public RequestScanner(InputStream inputStream, BlockingQueue<Request> requestQueue) {
        this.scanner = new Scanner(inputStream);
        this.bufferedInputStream = new BufferedReader(new InputStreamReader(inputStream));

        this.requestQueue = requestQueue;
        Parser.parse("(FR, 1, UP)", 0);
        // Let OS load parser, so next time parser will not cost too much time.
    }

    public void run() {
        /*  @REQUIRES: 1 <= inputLine.split(";").size <= 10
        *   @MODIFIES:
        *       this.scanner.hasNextLine() ==> this.scanner.nextLine()
        *       Global.timeZeroPoint == -1 ==> Global.timeZeroPoint = inputTime
        *   @EFFECTS:
        *       1. \exists inputString in splitted; !isValid(inputString); Output.printInvalid(ie.message, inputTime);
        *       2. \exists "END" in splitted ==> requestQueue.put(Request.endSignal());
        *       3. \all inputString in splitted; isValid(inputString); requestQueue.put(newRequest(inputString));
        *   @THREAD_REQUIRES:
        *       1. \locked(requestQueue)
        *   @THREAD_EFFECTS:
        *       1. \locked(requestQueue)
        * */
        try {
            while (this.scanner.hasNextLine()) {
                String inputLine;
                inputLine = this.scanner.nextLine();

                long inputTime = System.currentTimeMillis();
                if (Global.timeZeroPoint == -1) {
                    Global.timeZeroPoint = inputTime;
                }
                String [] splitted = inputLine.split(";", -1);
                if (splitted.length > Config.MAX_REQUEST_NUM_ONE_LINE) {
                    Output.println("# Too many input(s) in one line, only read the first " + Config.MAX_REQUEST_NUM_ONE_LINE + " requests.");
                    String [] firstTen = new String[10];
                    for (int i = 0; i < Config.MAX_REQUEST_NUM_ONE_LINE; i++) {
                        firstTen[i] = splitted[i];
                    }
                    splitted = firstTen;
                }

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
