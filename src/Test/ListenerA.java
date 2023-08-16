package Test;

import event.EventMethod;

public class ListenerA implements event.EventListener{

    @EventMethod
    public void onEvent(EventA event) {
        System.out.println(event.a);
    }
}
