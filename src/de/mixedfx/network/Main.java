package de.mixedfx.network;

import org.bushe.swing.event.annotation.AnnotationProcessor;

public class Main
{
	public static void main(final String[] args)
	{
		try
		{
			AnnotationProcessor.process(new Main());
			new Discovery().start();
		}
		catch (final Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Catch exceptions and inform GUI
		// Runnable server = new Runnable()
		// {
		//
		// @Override
		// public void run()
		// {
		// try
		// {
		// DiscoveryManager serverManager = new DiscoveryManager();
		// serverManager.startServer();
		// }
		// catch (PortUnreachableException | UnknownNetworkErrorException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// };
		// startThread(server);
		//
		// Runnable client = new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// try
		// {
		// DiscoveryManager clientManager = new DiscoveryManager();
		// clientManager.startClient();
		// }
		// catch (PortUnreachableException | UnknownNetworkErrorException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// };
		// startThread(client);
		int t = 0;
		while (t < 5)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			t++;
		}
	}

	@org.bushe.swing.event.annotation.EventTopicSubscriber(topic = "PortFailed")
	public void show(final String topic, final Exception e)
	{
		e.printStackTrace();
	}

	/**
	 * Creates a new thread as daemon and starts it with the given task.
	 *
	 * @param task
	 */
	private static void startThread(final Runnable task)
	{
		final Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
	}

}
