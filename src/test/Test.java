package test;

import event.EventController;


public class Test {
    public static void main(String[] args) {
        EventController controller = new EventController();

        ListenerA listener = new ListenerA();
        controller.registerListener(listener);

        controller.callEvent(new EventA("A"));

        controller.unregisterListener(listener);
        controller.callEvent(new EventC("C"));
    }
}
