package threadTest;

/*
* Thread-1:63116590711720(before pending)
* Thread-1:63116590719743(restored running)
* 结论：10个进程下，thread相邻时间片大于 1ms
* */

import java.util.LinkedList;
import java.util.List;

public class TimeTest extends Thread {
    private int runTimeLimit = 100;
    private List<Long> runTimeList = new LinkedList<>();
    private List<Long> lengthList = new LinkedList<>();
    public void run() {
        while (true) {
            while (runTimeList.size() > 0 && System.nanoTime() - runTimeList.get(runTimeList.size() - 1) < 1000 * 1000) {
                yield();
            }
            if (runTimeLimit-- >= 0) {
                runTimeList.add(System.nanoTime());
            } else {
                for (int i = 1; i < runTimeList.size(); i++) {
                    lengthList.add(runTimeList.get(i) - runTimeList.get(i-1));
                }
                System.out.println(this.getName() + ": " + lengthList);
                return;
            }
//            if (this.getName().equals("Thread-0") || this.getName().equals("Thread-1")) {
//                System.out.println(this.getName() + ":" + System.nanoTime());
//            }
        }
    }
    public static void main(String args[]) {
        for (int i = 0; i < 5; i++) {
            new TimeTest().start();
        }
    }
}
