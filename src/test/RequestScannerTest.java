package test;

import multiElevator.Request;
import multiElevator.RequestScanner;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RequestScannerTest {
    public static void main(String args[]) throws IOException {
        BlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(1024, true);
        PipedInputStream pipedInputStream = TestInputter.getStream("src/test/tests/test1.txt");
        RequestScanner requestScanner = new RequestScanner(pipedInputStream, requestQueue);
        requestScanner.start();
    }
}
