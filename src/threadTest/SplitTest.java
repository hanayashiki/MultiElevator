package threadTest;

public class SplitTest {
    public static void main(String args[]) {
        String [] xxx = ";;;;".split(";", -1);
        System.out.println(xxx.length);
        for (String s : xxx) {
            System.out.println(s);
        }
    }
}
