package de.mixedfx.network;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			(new Discovery()).start();
			;
		}
		catch (Exception e)
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
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			t++;
		}
	}

	/**
	 * Creates a new thread as daemon and starts it with the given task.
	 * 
	 * @param task
	 */
	private static void startThread(Runnable task)
	{
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
	}

}
