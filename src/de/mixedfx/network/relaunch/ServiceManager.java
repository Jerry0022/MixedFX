package de.mixedfx.network.relaunch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceManager
{
	/**
	 * Use {@link UniqueService} or {@link P2PService}!
	 *
	 * @author Jerry
	 */
	private interface Stoppable
	{
		/**
		 * <p>
		 * Every time {@link NetworkManager#online} is set to
		 * {@link NetworkManager.OnlineStates#Offline} this method is called.
		 * </p>
		 * <p>
		 * ATTENTION: This method blocks network activity. Therefore do not execute long running
		 * tasks directly here!
		 * </p>
		 * <p>
		 * Is called before {@link ParticipantManager} is stopped. Therefore
		 * {@link ParticipantManager#PARTICIPANTS} and {@link ParticipantManager#MY_PID} depends on
		 * if this application is a client or a host.
		 * </p>
		 */
		public void stop();
	}

	public interface UniqueService extends Stoppable
	{
		/**
		 * <p>
		 * Every time {@link NetworkManager#online} is set to
		 * {@link NetworkManager.OnlineStates#Online} this method is called asynchronously.
		 * </p>
		 * The {@link ParticipantManager} already has a pid and all other participants currently
		 * registered.
		 */
		public void client();

		/**
		 * <p>
		 * Every time {@link NetworkManager#online} is set to
		 * {@link NetworkManager.OnlineStates#Online} this method is called.
		 * </p>
		 * <p>
		 * ATTENTION: This method blocks network activity. Therefore do not execute long running
		 * tasks here directly!
		 * </p>
		 * <p>
		 * Is called after {@link ParticipantManager} was started. Therefore
		 * {@link ParticipantManager#MY_PID} is set to 1 and already added to the
		 * {@link ParticipantManager#PARTICIPANTS}.
		 * </p>
		 */
		public void host();
	}

	public interface P2PService extends Stoppable
	{
		/**
		 * Every time {@link NetworkManager#online} is set to
		 * {@link NetworkManager.OnlineStates#Online} this method is called.
		 */
		public void start();
	}

	protected static List<Stoppable>	list	= Collections.synchronizedList(new ArrayList<Stoppable>());

	static
	{

	}

	protected static void client()
	{
		synchronized (ServiceManager.list)
		{
			for (final Stoppable synced : ServiceManager.list)
			{
				if (synced instanceof UniqueService)
				{
					((UniqueService) synced).client();
				}
				else
					if (synced instanceof P2PService)
					{
						((P2PService) synced).start();
					}
			}
		}
	}

	protected static void host()
	{
		synchronized (ServiceManager.list)
		{
			for (final Stoppable service : ServiceManager.list)
			{
				if (service instanceof UniqueService)
				{
					((UniqueService) service).host();
				}
				else
					if (service instanceof P2PService)
					{
						((P2PService) service).start();
					}
			}
		}
	}

	protected static void stop()
	{
		synchronized (ServiceManager.list)
		{
			for (final Stoppable service : ServiceManager.list)
			{
				service.stop();
			}
		}
	}

	public static void register(final Stoppable synced)
	{
		synchronized (ServiceManager.list)
		{
			ServiceManager.list.add(synced);
		}
	}

	public static void unregister(final Stoppable synced)
	{
		synchronized (ServiceManager.list)
		{
			ServiceManager.list.remove(synced);
		}
	}
}
