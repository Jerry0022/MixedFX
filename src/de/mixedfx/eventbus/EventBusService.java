package de.mixedfx.eventbus;

import java.util.Hashtable;

import org.bushe.swing.event.ThreadSafeEventService;

/**
 * <pre>
 * Individual EventBus
 * - thread safe
 * - synchronous and asynchronous
 * - requires fixed structure if you use constructor(Class, int)
 *
 * For a generic EventBus use EventBus by bushe
 * </pre>
 *
 * @author Jerry
 */
public class EventBusService extends ThreadSafeEventService
{
	private static final String									delimiter		= "|";
	private static volatile Hashtable<String, EventBusService>	eventBusList	= new Hashtable<String, EventBusService>();

	public static EventBusService getEventBus(final Class<?> eventBusHolderClass, final int ID)
	{
		return EventBusService.getEventBus(EventBusService.createEventBusName(eventBusHolderClass, ID));
	}

	public static EventBusService getEventBus(final String eventBusName)
	{
		return EventBusService.eventBusList.get(eventBusName);
	}

	public static void removeEventBusExtended(final Class<?> eventBusHolderClass, final int ID)
	{
		EventBusService.removeEventBusExtended(EventBusService.createEventBusName(eventBusHolderClass, ID));
	}

	public static void removeEventBusExtended(final String eventBusName)
	{
		EventBusService.eventBusList.remove(eventBusName);
	}

	private static String createEventBusName(final Class<?> classObject, final int ID)
	{
		return "EventBus" + EventBusService.delimiter + classObject.getName() + EventBusService.delimiter + ID;
	}

	/*
	 * To initialize an EventBus use this:
	 * 
	 * EventBusService eventBus = new EventBusService(this.getClass(), ID);
	 * eventBus.subscribe("topic", classWithSubscriptions);
	 * 
	 * (the subscription class has to implement EventTopicSubscriber<Object>)
	 * 
	 * To use this EventBus use (inside the classWithSubscriptions): eventBus.publishAsync(String
	 * topic, ...) or eventBus.publishSync(String topic, ...) etc. or (outside the
	 * classWithSubscriptions): EventBusService.getEventBus(classWithSubscriptions,
	 * ID).publishAsync(String topic, ...) etc.;
	 */

	private final String	eventBusName;

	public EventBusService(final Class<?> eventBusHolderClass, final int ID)
	{
		this(EventBusService.createEventBusName(eventBusHolderClass, ID));
	}

	/**
	 * Overrides existing eventbuses or creates a new one with this name
	 * 
	 * @param eventBusName
	 */
	public EventBusService(final String eventBusName)
	{
		this.eventBusName = eventBusName;
		if (EventBusService.eventBusList.containsKey(eventBusName))
			EventBusService.eventBusList.remove(eventBusName);
		EventBusService.eventBusList.put(this.eventBusName, this);
	}

	public synchronized void publishSync(final String topic, final Object event)
	{
		this.publish(topic, event);
	}

	public void publishAsync(final String topic, final Object event)
	{
		final Thread thread = new Thread(() -> EventBusService.this.publish(topic, event));
		thread.setDaemon(true);
		thread.start();
	}
}
