package de.mixedfx.windows;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.mixedfx.logging.Log;
import javafx.util.Duration;

public class WindowsMonitoring
{
	public interface Callback<T>
	{
		public void action(List<T> object);

		public List<T> getItems();
	}

	public static final long MONITORING_INTERVAL = (long) Duration.seconds(1).toMillis();

	private static List<Callback<?>>	callbacks	= new ArrayList<Callback<?>>();
	private static Thread				monitoringThread;
	private static boolean				monitoring;

	public static void remove(final Callback<?> callback)
	{
		synchronized (WindowsMonitoring.callbacks)
		{
			WindowsMonitoring.callbacks.remove(callback);
		}
	}

	public static void startMonitoring(final Callback<?> callback)
	{
		synchronized (WindowsMonitoring.callbacks)
		{
			if (!WindowsMonitoring.callbacks.contains(callback))
				WindowsMonitoring.callbacks.add(callback);
		}
		if (WindowsMonitoring.monitoringThread == null)
		{
			WindowsMonitoring.monitoringThread = new Thread(() ->
			{
				while (WindowsMonitoring.monitoring)
				{
					synchronized (WindowsMonitoring.callbacks)
					{
						final boolean firewallsStatus = FirewallController.isEnabled();
						final List<NetworkAdapter> netAdapters = NetworkAdapterController.getList();

						for (final Callback<?> c : WindowsMonitoring.callbacks)
						{
							final List<?> items = c.getItems();
							for (final Object o : items)
							{
								if (o instanceof Program)
								{
									((Program) o).processStatus = ProcessController.isProcessRunning((Program) o);
									((Program) o).serviceStatus = ServiceController.isRunning((Program) o);
								}
								else
									if (o instanceof NetworkAdapter)
									{
										final int index = netAdapters.indexOf(o);
										if (index != -1)
										{
											final NetworkAdapter current = netAdapters.get(index);
											((NetworkAdapter) o).enabled = current.enabled;
											((NetworkAdapter) o).connected = current.connected;
										}
										else
										{
											// Not found!
											((NetworkAdapter) o).enabled = false;
											((NetworkAdapter) o).connected = false;
										}
									}
									else
										if (o instanceof Firewalls)
										{
											((Firewalls) o).status = firewallsStatus;
										}
										else
										{
											Log.windows.error("Can't retrieve status of an object of type: " + o);
											break;
										}
							}
							// Inform callback
							c.action((List) items);
						}
					}
                    long timeBeforeGC = new Date().getTime();
                    System.gc();
                    long difference = timeBeforeGC - new Date().getTime();
                    if(difference < WindowsMonitoring.MONITORING_INTERVAL)
                    {
                        try
                        {
                            Thread.sleep(WindowsMonitoring.MONITORING_INTERVAL - difference);
                        }
                        catch (final Exception e)
                        {}
                    }
				}
			});
			WindowsMonitoring.monitoringThread.setDaemon(true);
			WindowsMonitoring.monitoring = true;
			WindowsMonitoring.monitoringThread.start();
		}
	}

	public static void stopMonitoring()
	{
		WindowsMonitoring.monitoring = false;
		WindowsMonitoring.monitoringThread = null;
	}
}
