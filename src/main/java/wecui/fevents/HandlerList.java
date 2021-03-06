package wecui.fevents;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;

/**
 * @author lahwran
 * @param <TEvent> Event type
 *
 */
@SuppressWarnings("unchecked")
public class HandlerList<TEvent extends Event<TEvent>> {

    /**
     * handler array. this field being an array is the key to this system's speed.
     * 
     * is initialized in bake().
     */
    public Listener<TEvent>[][] handlers;
    /**
     * Int array same length as handlers. each value in this array is the index
     * of an Order slot, corossponding to the equivalent value in handlers.
     * 
     * is initialized in bake().
     */
    public int[] handlerids;
    /**
     * Dynamic handler lists. These are changed using register() and
     * unregister() and are automatically baked to the handlers array any
     * time they have changed.
     */
    private final EnumMap<Order, ArrayList<Listener<TEvent>>> handlerslots;
    /**
     * Whether the current handlerslist has been fully baked. When this is set
     * to false, the Map<Order, List<Listener>> will be baked to Listener[][]
     * next time the event is called.
     * 
     * @see EventManager.callEvent
     */
    private boolean baked = false;
    /**
     * List of all handlerlists which have been created, for use in bakeall()
     */
    private static ArrayList<HandlerList<? extends Event<?>>> alllists = new ArrayList<HandlerList<? extends Event<?>>>();

    /**
     * Bake all handler lists. Best used just after all normal event
     * registration is complete, ie just after all plugins are loaded if
     * you're using fevents in a plugin system.
     */
    public static void bakeall() {
        for (HandlerList<? extends Event<?>> h : alllists) {
            h.bake();
        }
    }

    /**
     * Create a new handler list and initialize using EventManager.Order
     * handlerlist is then added to meta-list for use in bakeall()
     */
    public HandlerList() {
        this.handlerslots = new EnumMap<Order, ArrayList<Listener<TEvent>>>(Order.class);
        for (Order o : Order.values()) {
            this.handlerslots.put(o, new ArrayList<Listener<TEvent>>());
        }
        alllists.add(this);
    }

    /**
     * Register a new listener in this handler list
     * @param listener listener to register
     * @param order order location at which to call provided listener
     */
    public void register(Listener<TEvent> listener, Order order) {
        if (this.handlerslots.get(order).contains(listener)) {
            throw new IllegalStateException("This listener is already registered to order " + order.toString());
        }
        this.baked = false;
        this.handlerslots.get(order).add(listener);
    }

    /**
     * Remove a listener from all order slots
     * @param listener listener to purge
     */
    public void unregister(Listener<TEvent> listener) {
        for (Order o : Order.values()) {
            this.unregister(listener, o);
        }
    }

    /**
     * Remove a listener from a specific order slot
     * @param listener listener to remove
     * @param order order from which to remove listener
     */
    public void unregister(Listener<TEvent> listener, Order order) {
        if (this.handlerslots.get(order).contains(listener)) {
            this.baked = false;
            this.handlerslots.get(order).remove(listener);
        }
    }

    /**
     * Bake HashMap and ArrayLists to 2d array - does nothing if not necessary
     */
    void bake() {
        if (this.baked) {
            return; // don't re-bake when still valid
        }
        ArrayList<Listener<? extends Event<?>>[]> handlerslist = new ArrayList<Listener<? extends Event<?>>[]>();
        ArrayList<Integer> handleridslist = new ArrayList<Integer>();
        for (Entry<Order, ArrayList<Listener<TEvent>>> entry : this.handlerslots.entrySet()) {
            Order orderslot = entry.getKey();

            ArrayList<Listener<TEvent>> list = entry.getValue();

            int ord = orderslot.getIndex();
            handlerslist.add(list.toArray(new Listener[list.size()]));
            handleridslist.add(ord);
        }
        this.handlers = handlerslist.toArray(new Listener[handlerslist.size()][]);
        this.handlerids = new int[handleridslist.size()];
        for (int i = 0; i < handleridslist.size(); i++) {
            this.handlerids[i] = handleridslist.get(i);
        }
        this.baked = true;
    }
}
