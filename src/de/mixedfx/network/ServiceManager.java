package de.mixedfx.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.network.messages.RegisteredMessage;

/**
 * Use {@link UniqueService} or {@link P2PService}!
 *
 * @author Jerry
 */
public class ServiceManager
{
	private interface Stoppable
	{
		/**
		 * <p>
		 * Every time {@link NetworkConfig#status} is set to {@link NetworkConfig.States#Unbound}
		 * this method is called synchronously.
		 * </p>
		 * <p>
		 * Is called before {@link ParticipantManager} is stopped. Therefore the content of
		 * {@link ParticipantManager#PARTICIPANTS} and {@link ParticipantManager#MY_PID} depends on
		 * if this application was a network host or client (PARTICIPANTS is maybe empty and MY_PID
		 * may contain 0 = not yet identified).
		 * </p>
		 *
		 * <p>
		 * DON'T EXECUTE LONG RUNNING TASKS HERE, since it blocks the network activity!
		 * </p>
		 */
		public void stop();
	}

	/**
	 * A service which exists from the start to the end of a network if the service is registered by
	 * calling {@link ServiceManager#register(Stoppable)}! Whereby {@link UniqueService#host()} is
	 * only called once in the network and {@link UniqueService#client()} is called on all the other
	 * applications!
	 *
	 * @author Jerry
	 */
	public interface UniqueService extends Stoppable
	{
		/**
		 * <p>
		 * Every time {@link NetworkConfig#status} is set to
		 * {@link NetworkConfig.States#BoundToServer} this method is called synchronously.
		 * </p>
		 * <p>
		 * The {@link ParticipantManager} already has a pid and all other participants currently
		 * registered!
		 * </p>
		 * <p>
		 * DON'T EXECUTE LONG RUNNING TASKS HERE, since it blocks the network activity!
		 * </p>
		 */
		public void client();

		/**
		 * <p>
		 * Every time {@link NetworkConfig#status} is set to {@link NetworkConfig.States#Server}
		 * this method is called synchronously.
		 * </p>
		 * <p>
		 * Is called after {@link ParticipantManager} was started. Therefore
		 * {@link ParticipantManager#MY_PID} is set to 1 and already added to the
		 * {@link ParticipantManager#PARTICIPANTS}.
		 * </p>
		 * <p>
		 * DON'T EXECUTE LONG RUNNING TASKS HERE, since it blocks the network activity!
		 * </p>
		 */
		public void host();

		/**
		 * <p>
		 * Default behavior is returning null (for all UniqueServices) - message will be forwarded.
		 * If at least one UniqueService returns a message object, which will be sent directly, the
		 * original message won't be forwarded.
		 * </p>
		 * <p>
		 * DON'T EXECUTE LONG RUNNING TASKS HERE, since it blocks the network activity!
		 * </p>
		 *
		 * @param message
		 * @return If return null there is no interest in this message - no special action will be
		 *         taken. If it returns a message this message will be sent to the receivers.
		 */
		public RegisteredMessage receive(RegisteredMessage message);
	}

	/**
	 * A network dependant service which exists in the same way on all computers!
	 *
	 * @author Jerry
	 */
	public interface P2PService extends Stoppable
	{
		/**
		 * <p>
		 * Every time {@link NetworkConfig#status} is set to
		 * {@link NetworkConfig.States#BoundToServer} this method is called synchronously.
		 * </p>
		 */
		public void start();
	}

	private static List<Stoppable>	list	= Collections.synchronizedList(new ArrayList<Stoppable>());

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

	protected static void hostCheckMessage(final RegisteredMessage message)
	{
		synchronized (ServiceManager.list)
		{
			boolean atLeastOne = false;
			for (final Stoppable service : ServiceManager.list)
			{
				if (service instanceof UniqueService)
				{
					final RegisteredMessage forwardMessage = ((UniqueService) service).receive(message);
					if (forwardMessage != null)
					{
						atLeastOne = true;
						EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, forwardMessage);
					}
				}
			}
			if (!atLeastOne)
			{
				EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, message);
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
