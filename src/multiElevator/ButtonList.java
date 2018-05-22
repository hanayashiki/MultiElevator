package multiElevator;

import java.util.Vector;

public class ButtonList {
    /*  @OVERVIEW: 包装一个记录电梯状态的数组
        @INHERIT: Object
        @INVARIANCE: None
    */
    private Vector<Boolean> buttonStatus;

    public ButtonList(int buttonCount) {
        /*  @REQUIRES: None
            @MODIFIES: None
            @EFFECTS: \all int i; 0 <= i < buttonStatus.length; buttonStatus[i] = 0
            @THREAD_REQUIRES: None
            @THREAD_EFFECTS: locked()
        */
        buttonStatus = new Vector<>(buttonCount);
        synchronized (this) {
            for (int i = 0; i < buttonCount; i++) {
                buttonStatus.add(false);
            }
        }
    }

    synchronized public void lightUp(int index) {
        /*  @REQUIRES: None
            @MODIFIES: None
            @EFFECTS: buttonStatus[index] == true
            @THREAD_REQUIRES: None
            @THREAD_EFFECTS: locked()
        */
        buttonStatus.set(index, true);
    }

    synchronized public void lightDown(int index) {
        /*  @REQUIRES: None
            @MODIFIES: None
            @EFFECTS: buttonStatus[index] == false
            @THREAD_REQUIRES: None
            @THREAD_EFFECTS: locked()
        */
        buttonStatus.set(index, false);
    }

    synchronized public boolean getLightStatus(int index) {
        /*  @REQUIRES: None
            @MODIFIES: None
            @EFFECTS: \result == buttonStatus[index]
            @THREAD_REQUIRES: None
            @THREAD_EFFECTS: locked()
        */
        return buttonStatus.get(index);
    }

    public String toString() {
        /*  @REQUIRES: None
            @MODIFIES: None
            @EFFECTS: \result == buttonStatus.toString()
            @THREAD_REQUIRES: None
            @THREAD_EFFECTS: locked()
        */
        return buttonStatus.toString();
    }
}
