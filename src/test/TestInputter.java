package test;

import multiElevator.Global;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

class TestEntry {
    long milliTime;
    String inputString;
    TestEntry(long milliTime, String inputString) {
        this.milliTime = milliTime;
        this.inputString = inputString;
    }
}

public class TestInputter extends Thread {
    ArrayList<TestEntry> testEntryList;
    OutputStream outputStream;
    TestInputter(String scriptAddr, OutputStream outputStream) throws FileNotFoundException {
        this.outputStream = outputStream;

        Scanner scanner = new Scanner(new FileInputStream(scriptAddr));
        testEntryList = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String newLine = scanner.nextLine();
            String [] splitted = newLine.split(":");
            String milliTimeString = splitted[0];
            String request = splitted[1];
            testEntryList.add(new TestEntry(Long.parseLong(milliTimeString), request));
        }
    }
    public void run() {
        long startingMilliTime = System.currentTimeMillis();
        for (int i = 0; i < testEntryList.size(); i++) {
            while (System.currentTimeMillis() - startingMilliTime < testEntryList.get(i).milliTime) {
                yield();
            }
            try {
                outputStream.write((testEntryList.get(i).inputString + "\n").getBytes("UTF-8"));
                System.out.println("send new request: " +
                        testEntryList.get(i).inputString + " @" + (System.currentTimeMillis() - startingMilliTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
