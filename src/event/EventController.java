package event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * EventController is a class that manages events and listeners.
 */
public final class EventController {

    private static class Invoker{
        private final EventListener listener;
        private final Method method;

        public Invoker(EventListener listener, Method method) {
            this.listener = listener;
            this.method = method;
        }

        public <T extends Event> void invoke(T event) throws InvocationTargetException, IllegalAccessException {
            method.invoke(listener, event);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listener, method);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Invoker invoker = (Invoker) o;
            return Objects.equals(listener, invoker.listener) && Objects.equals(method, invoker.method);
        }
    }

    private final Map<Class<? extends Event>, Set<Invoker>> data;

    public EventController() {
        data = new HashMap<>();
    }

    /**
     * Call an event.
     * @param event Event to be invoked.
     * @param <T> Type of the Event.
     */
    public <T extends Event> void callEvent(T event){
        Set<Invoker> invoked = new HashSet<>();

        for(var e: data.entrySet()){
            if(e.getKey().isAssignableFrom(event.getClass())){
                for(Invoker inv: e.getValue()){
                    if (invoked.contains(inv))
                        continue;
                    try {
                        inv.invoke(event);
                    } catch (InvocationTargetException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                    invoked.add(inv);
                }
            }
        }
    }

    /**
     * Register an EventListener.
     * @param listener EventListener to be registered.
     */
    public void registerListener(EventListener listener) {
        for(Method m: listener.getClass().getMethods()){
            if(!m.isAnnotationPresent(EventMethod.class))
                continue;
            if(m.getParameterTypes().length != 1)
                continue;
            Class<?> c = m.getParameterTypes()[0];
            if(Event.class.isAssignableFrom(c)){
                Class<? extends Event> ec = (Class<? extends Event>) c;
                Invoker invoker = new Invoker(listener, m);
                data.computeIfAbsent(ec, k -> new HashSet<>()).add(invoker);
            }
        }
    }

    /**
     * Unregister an EventListener.
     * @param listener EventListener to be unregistered.
     */
    public void unregisterListener(EventListener listener){
        var i = data.entrySet().iterator();
        while(i.hasNext()){
            var e = i.next();
            e.getValue().removeIf(inv -> inv.listener.equals(listener));
            if(e.getValue().isEmpty())
                i.remove();
        }
    }

}
