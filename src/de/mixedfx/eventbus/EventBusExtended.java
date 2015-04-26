package de.mixedfx.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.ProxyTopicSubscriber;

public class EventBusExtended extends EventBus
{
	/**
	 * Offers a synchronous thread safe topic publish() extending the EventBus by bushee
	 * Does only work with Annotations?!
	 * @param topic The topic to reach the subscribers of this topic
	 * @param eventObject The object to transfer to the subscribers
	 */
	public synchronized static void publishSyncSafe(String topic, Object eventObject)
	{	
		ProxyTopicSubscriber subscriber;
		for(Object proxySubscriber : EventBus.getSubscribers(topic))
		{
			System.err.println(proxySubscriber.getClass().getSuperclass().getSuperclass().getSimpleName());
			subscriber = (ProxyTopicSubscriber) proxySubscriber;
			
			try
			{
				subscriber.getSubscriptionMethod().invoke(subscriber.getProxiedSubscriber().getClass().cast(subscriber.getProxiedSubscriber()), topic, eventObject);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e)
			{
				// TODO: Handle Exception
				e.printStackTrace();
				System.out.println(101);
			}
		}
	}
	
	public synchronized static void publishSafe(String topic, Object eventObject)
	{
		ArrayList<Object> proxySubscribers = new ArrayList<Object>(EventBus.getSubscribers(topic));

		for(Object proxySubscriber : proxySubscribers)
		{
			AtomicReference<ProxyTopicSubscriber> proxySubscriberAtomic = new AtomicReference<ProxyTopicSubscriber>((ProxyTopicSubscriber) proxySubscriber);
			(new Thread(new Runnable(){
				@Override
				public void run()
				{
					try
					{
						ProxyTopicSubscriber subscriber = proxySubscriberAtomic.get();
						
						if(subscriber.getSubscriptionMethod() != null)
							subscriber.getSubscriptionMethod().invoke(subscriber.getProxiedSubscriber().getClass().cast(subscriber.getProxiedSubscriber()), topic, eventObject);
						// else should throw a programming failure exception OR asking sth. like ignore?
					} 
					catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | SecurityException e)
					{
						// TODO: Handle Exception
						e.printStackTrace();
						System.out.println(101);
					}
					catch(ClassCastException e)
					{
						e.printStackTrace();
						System.out.println("Guilty is: " + proxySubscriber.getClass().getSimpleName());
						System.out.println("At the topic " + topic + " occured an error!");
					}
				}
				
			})).start();
		}
	}
}
