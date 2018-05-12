package multiElevator;

import java.util.Vector;

public class ButtonList {
    private Vector<Boolean> buttonStatus;
    public ButtonList(int buttonCount) {
        buttonStatus = new Vector<>(buttonCount);
        synchronized (this) {
            for (int i = 0; i < buttonCount; i++) {
                buttonStatus.add(false);
            }
        }
    }
    synchronized public void lightUp(int index) {
        buttonStatus.set(index, true);
    }
    synchronized public void lightDown(int index) {
        buttonStatus.set(index, false);
    }
    synchronized public boolean getLightStatus(int index) {
        return buttonStatus.get(index);
    }
    public String toString() {
        return buttonStatus.toString();
    }
}
