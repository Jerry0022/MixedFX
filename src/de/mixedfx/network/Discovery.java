package de.mixedfx.network;

import java.net.InetAddress;

import javafx.collections.ListChangeListener;

import org.bushe.swing.event.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;

public class Discovery
{
	public final static int	PORT	= 8888;

	public enum Messages
	{
		DISCOVERY;
	}

	public enum Errors
	{
		PortFailed;
	}

	private DiscoveryIndia		india;
	private Runnable			indiaTask;
	private Runnable			kolumbusTask;
	private DiscoveryKolumbus	kolumbus;

	/**
	 * @throws Exception
	 *             Throws exception if there is a port or SecurityManager issue.
	 */
	public Discovery() throws Exception
	{
		indiaTask = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					india = new DiscoveryIndia();
					india.discovered.addListener(new ListChangeListener<InetAddress>()
					{
						@Override
						public void onChanged(javafx.collections.ListChangeListener.Change<? extends InetAddress> c)
						{
							// TODO Start TCP Connection
							// TODO If TCP Connection fails do removal
							if (c.next())
								india.removeIP(c.getAddedSubList().get(0));
						}
					});
				}
				catch (Exception e)
				{
					EventBusExtended.publishSafe(Errors.PortFailed.toString(), e);
				}
				india.startListening();
			}
		};

		kolumbusTask = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					kolumbus = new DiscoveryKolumbus();
				}
				catch (Exception e)
				{
					EventBusExtended.publishSafe(Errors.PortFailed.toString(), e);
				}
				kolumbus.startDiscovering();
			}
		};

		// TODO DELETE
		EventBusExtended.subscribe(Errors.PortFailed.toString(), new EventTopicSubscriber<Exception>()
		{
			@Override
			public void onEvent(String topic, Exception data)
			{
				data.printStackTrace();
			}
		});
	}

	public void start()
	{
		startThread(indiaTask);
		startThread(kolumbusTask);
	}

	public void stop()
	{
		if (india != null)
			india.stopListening();
		if (kolumbus != null)
			kolumbus.stopDiscovering();
	}

	private void startThread(Runnable runnable)
	{
		Thread t = new Thread(runnable);
		t.setDaemon(true);
		t.start();
	}
}
