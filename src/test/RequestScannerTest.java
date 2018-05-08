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
        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        BlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(1024, true);

        pipedInputStream.connect(pipedOutputStream);
        TestInputter testInputter = new TestInputter("src/test/tests/test1.txt", pipedOutputStream);
        RequestScanner requestScanner = new RequestScanner(pipedInputStream, requestQueue);

        testInputter.start();
        requestScanner.start();
    }
}
