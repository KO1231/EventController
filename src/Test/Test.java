package Test;

import event.EventController;


public class Test {
    public static void main(String[] args) {
        EventController controller = new EventController();

        controller.registerListener(new ListenerA());

        controller.callEvent(new EventA("A"));
        controller.callEvent(new EventC("C"));
    }
}
