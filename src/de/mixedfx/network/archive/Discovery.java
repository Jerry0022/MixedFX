package de.mixedfx.network.archive;

import java.net.InetAddress;

import javafx.collections.ListChangeListener;

public class Discovery
{
	public final static int	PORT_UDP_CLIENT	= 8888;
	public final static int	PORT_UDP_SERVER	= 8888;

	public enum Messages
	{
		DISCOVERY_REQUEST, DISCOVERY_ANSWER;
	}

	public enum Errors
	{
		PortFailed;
	}

	private DiscoveryIndia		india;
	private final Runnable		indiaTask;
	private final Runnable		kolumbusTask;
	private DiscoveryKolumbus	kolumbus;

	/**
	 * @throws Exception
	 *             Throws exception if there is a port or SecurityManager issue.
	 */
	public Discovery() throws Exception
	{
		this.indiaTask = () ->
		{
			try
			{
				Discovery.this.india = new DiscoveryIndia();
				Discovery.this.india.discovered.addListener((ListChangeListener<InetAddress>) c ->
				{
					// TODO Start TCP Connection
					// TODO If TCP Connection fails do removal
					if (c.next())
						Discovery.this.india.removeIP(c.getAddedSubList().get(0));
				});
				Discovery.this.india.startListening();
			}
			catch (final Exception e)
			{
				System.out.println(e);
			}
		};

		this.kolumbusTask = () ->
		{
			try
			{
				Discovery.this.kolumbus = new DiscoveryKolumbus();
			}
			catch (final Exception e)
			{
				System.out.println(e);
			}
			Discovery.this.kolumbus.startDiscovering();
		};
	}

	public void start()
	{
		// this.startThread(this.indiaTask);
		this.startThread(this.kolumbusTask);
	}

	public void stop()
	{
		if (this.india != null)
			this.india.stopListening();
		if (this.kolumbus != null)
			this.kolumbus.stopDiscovering();
	}

	private void startThread(final Runnable runnable)
	{
		final Thread t = new Thread(runnable);
		t.setDaemon(true);
		t.start();
	}
}
