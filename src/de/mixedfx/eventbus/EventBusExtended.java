package de.mixedfx.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.ProxyTopicSubscriber;

public class EventBusExtended extends EventBus
{
	/**
	 * Offers a synchronous thread safe topic synchronously publish() extending the EventBus by bushee. Does only work with Annotations?!
	 *
	 * @param topic
	 *            The topic to reach the subscribers of this topic
	 * @param eventObject
	 *            The object to transfer to the subscribers
	 */
	public synchronized static void publishSyncSafe(final String topic, final Object eventObject)
	{
		ProxyTopicSubscriber subscriber;
		for (final Object proxySubscriber : EventBus.getSubscribers(topic))
		{
			subscriber = (ProxyTopicSubscriber) proxySubscriber;

			try
			{
				subscriber.getSubscriptionMethod().invoke(subscriber.getProxiedSubscriber().getClass().cast(subscriber.getProxiedSubscriber()), topic, eventObject);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e)
			{
				e.printStackTrace();
				System.out.println(101);
			}
		}
	}

	/**
	 * Offers a synchronous thread safe topic asynchronously (on a new thread for each subscriber) publish() extending the EventBus by bushee. Does only work with Annotations?!
	 *
	 * @param topic
	 *            The topic to reach the subscribers of this topic
	 * @param eventObject
	 *            The object to transfer to the subscribers
	 */
	public synchronized static void publishAsyncSafe(final String topic, final Object eventObject)
	{
		final ArrayList<Object> proxySubscribers = new ArrayList<Object>(EventBus.getSubscribers(topic));

		for (final Object proxySubscriber : proxySubscribers)
		{
			final AtomicReference<ProxyTopicSubscriber> proxySubscriberAtomic = new AtomicReference<ProxyTopicSubscriber>((ProxyTopicSubscriber) proxySubscriber);

			new Thread(() ->
			{
				try
				{
					final ProxyTopicSubscriber subscriber = proxySubscriberAtomic.get();

					if (subscriber.getSubscriptionMethod() != null)
					{
						subscriber.getSubscriptionMethod().invoke(subscriber.getProxiedSubscriber().getClass().cast(subscriber.getProxiedSubscriber()), topic, eventObject);
						// else should throw a programming failure exception OR asking sth. like
						// ignore?
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e1)
				{
					e1.printStackTrace();
					System.out.println(101);
				} catch (final ClassCastException e2)
				{
					e2.printStackTrace();
					System.out.println("It was because of: " + proxySubscriber.getClass().getSimpleName());
					System.out.println("At the topic " + topic + " occured an error!");
				}
			}).start();
		}
	}
}
