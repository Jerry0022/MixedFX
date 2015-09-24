package de.mixedfx.eventbus;

import org.bushe.swing.event.EventTopicSubscriber;

public interface EventBusServiceInterface extends EventTopicSubscriber<Object>
{

	/**
	 * This method is not called automatically.
	 * 
	 * <pre>
	 * 	For Example:
	 *  &#64;Override
	 * 	public void initilizeEventBusAndSubscriptions()
	 * 	{
	 * 		eventBus = new EventBusService(this.getClass(), this.clientID);
	 * 		eventBus.subscribe(NetworkEventBusTopic.topic_NETWORK_Stream_MessageReceived, this);
	 * 		eventBus.subscribe(NetworkEventBusTopic.topic_NETWORK_Stream_ConnectionLost, this);
	 * 	}
	 * </pre>
	 */
	public void initilizeEventBusAndSubscriptions();

	/**
	 * Make this method synchronized to force synchronous events on listener side
	 *
	 * @see org.bushe.swing.event.EventTopicSubscriber#onEvent(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onEvent(String topic, Object event);
}
