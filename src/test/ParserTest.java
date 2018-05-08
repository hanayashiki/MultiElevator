package test;

import multiElevator.InputException;
import multiElevator.Parser;
import multiElevator.Request;

public class ParserTest {
    private static String[] testcases =
            new String[]{
                    "abcdefg:invalid",
                    "(Fr, 10, UP):invalid",
                    "(FR, 10, DOWwN):invalid",
                    "(FR, 10, DOWN:invalid",
                    "(FR, 10, DOWN)):invalid",
                    "(ER, 3, 20):invalid",
                    "(ER, #1, 25):invalid",
                    "(ER, #0, 20):invalid",
                    "(FR, 1, DOWN):invalid",
                    "(FR, 20, UP):invalid",
                    "(FR, 1, UP):good",
                    "(FR, +1,    UP):good",
                    "   (F  R, + 1,    UP):good",
                    "   (E  R, # 1,    20):good",
                    "   (E  R, # 3,    1):good"
            };

    public static void main(String args[]) {
        boolean passed = true;
        for (String testcase : testcases) {
            String [] splitted = testcase.split(":");
            boolean exception = false;
            try {
                Request req = Parser.parse(splitted[0], 0);
                System.out.println(testcase + ": " + req);
            } catch (InputException ie) {
                exception = true;
                if (splitted[1].equals("good")) {
                    passed = false;
                    System.out.println("Bad case1: " + testcase);
                }
            }
            if (splitted[1].equals("invalid") && exception == false) {
                System.out.println("Bad case2: " + testcase);
            }
        }
        if (passed == true) {
            System.out.println("All tests passed.");
        }
    }
}
