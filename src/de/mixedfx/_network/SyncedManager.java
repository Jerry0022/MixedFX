package de.mixedfx._network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SyncedManager
{
	/**
	 * Use {@link SyncedInterface} or {@link UnsyncedInterface}!
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

	public interface SyncedInterface extends Stoppable
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

	public interface UnsyncedInterface extends Stoppable
	{
		/**
		 * Every time {@link NetworkManager#online} is set to
		 * {@link NetworkManager.OnlineStates#Online} this method is called.
		 */
		public void start();
	}

	protected static List<Stoppable>	list	= Collections.synchronizedList(new ArrayList<Stoppable>());

	protected static void client()
	{
		synchronized (SyncedManager.list)
		{
			for (final Stoppable synced : SyncedManager.list)
				if (synced instanceof SyncedInterface)
					((SyncedInterface) synced).client();
				else
					if (synced instanceof UnsyncedInterface)
						((UnsyncedInterface) synced).start();
		}
	}

	protected static void host()
	{
		synchronized (SyncedManager.list)
		{
			for (final Stoppable synced : SyncedManager.list)
				if (synced instanceof SyncedInterface)
					((SyncedInterface) synced).host();
				else
					if (synced instanceof UnsyncedInterface)
						((UnsyncedInterface) synced).start();
		}
	}

	protected static void stop()
	{
		synchronized (SyncedManager.list)
		{
			for (final Stoppable synced : SyncedManager.list)
				synced.stop();
		}
	}

	public static void register(final Stoppable synced)
	{
		SyncedManager.list.add(synced);
	}

	public static void unregister(final Stoppable synced)
	{
		SyncedManager.list.remove(synced);
	}
}
