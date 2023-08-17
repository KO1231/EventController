package event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class EventController {

    private static class Invoker{
        Method method;
        EventListener listener;

        public Invoker(Method method, EventListener listener) {
            this.method = method;
            this.listener = listener;
        }

        public <T extends Event> void invoke(T event) throws InvocationTargetException, IllegalAccessException {
            method.invoke(listener, event);
        }

        @Override
        public int hashCode() {
            return method.hashCode() + listener.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Invoker inv){
                return inv.method.equals(method) && inv.listener.equals(listener);
            }
            return false;
        }
    }

    private Map<Class<? extends Event>, Set<Invoker>> data;

    public EventController() {
        data = new HashMap<>();
    }

    public <T extends Event> void callEvent(T event){
        Set<Invoker> invoke_invokers = new HashSet<>();

        for(var e: data.entrySet()){
            if(e.getKey().isAssignableFrom(event.getClass())){
                for(Invoker inv: e.getValue()){
                    if (invoke_invokers.contains(inv))
                        continue;
                    try {
                        inv.invoke(event);
                    } catch (InvocationTargetException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                    invoke_invokers.add(inv);
                }
            }
        }
    }


    public void registerListener(EventListener v) {
        for(Method m: v.getClass().getMethods()){
            if(!m.isAnnotationPresent(EventMethod.class))
                continue;
            if(m.getParameterTypes().length != 1)
                continue;
            Class<?> c = m.getParameterTypes()[0];
            if(Event.class.isAssignableFrom(c)){
                Class<? extends Event> ec = (Class<? extends Event>) c;
                Invoker inv = new Invoker(m, v);
                if(data.containsKey(c)){
                    data.get(c).add(inv);
                }else{
                    data.put(ec, new HashSet<>(List.of(inv)));
                }
            }
        }
    }

}
