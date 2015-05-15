package de.mixedfx.network;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.eventbus.EventBusServiceInterface;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.ParticipantMessage;

public class Connection implements EventBusServiceInterface
{
	public static final String	MESSAGE_CHANNEL_SEND	= "MESSAGE_CHANNEL_SEND";

	public enum TOPICS
	{
		MESSAGE_CHANNEL_RECEIVED, CONNECTION_CHANNEL_LOST;
	}

	private final HashMap<String, Integer>	uid_pid_map;

	private final int						clientID;
	private final Socket					clientSocket;
	private EventBusService					eventBus;

	private volatile ConnectionOutput		outputConnection;
	private volatile ConnectionInput		inputConnection;

	public Connection(final int clientID, final Socket clientSocket) throws IOException
	{
		System.out.println("Initializing " + this.getClass().getSimpleName());

		this.uid_pid_map = new HashMap<>();

		this.clientID = clientID;
		this.clientSocket = clientSocket;

		this.initilizeEventBusAndSubscriptions();

		this.outputConnection = new ConnectionOutput(this.clientID, clientSocket.getOutputStream());
		final Thread outputConnectionThread = new Thread(this.outputConnection);
		outputConnectionThread.setDaemon(true);
		outputConnectionThread.start();

		this.inputConnection = new ConnectionInput(this.clientID, clientSocket.getInputStream());
		final Thread inputConnectionThread = new Thread(this.inputConnection);
		inputConnectionThread.setDaemon(true);
		inputConnectionThread.start();

		System.out.println(this.getClass().getSimpleName() + " initialized!");
	}

	@Override
	public void initilizeEventBusAndSubscriptions()
	{
		this.eventBus = new EventBusService(this.getClass(), this.clientID);
		this.eventBus.subscribe(TOPICS.MESSAGE_CHANNEL_RECEIVED.toString(), this);
		this.eventBus.subscribe(TOPICS.CONNECTION_CHANNEL_LOST.toString(), this);

		AnnotationProcessor.process(this);
	}

	@Override
	@EventTopicSubscriber(topic = Connection.MESSAGE_CHANNEL_SEND)
	public synchronized void onEvent(final String topic, final Object event)
	{
		if (topic.equals(Connection.MESSAGE_CHANNEL_SEND))
		{
			final Message message = (Message) event;

			if (NetworkConfig.status.get().equals(States.Server))
				message.fromServer = true;

			if (message.fromServer && message instanceof ParticipantMessage)
			{
				final ParticipantMessage pMessage = (ParticipantMessage) message;
				if (this.uid_pid_map.containsKey(pMessage.uID) && this.uid_pid_map.get(pMessage.uID) == null)
					this.uid_pid_map.replace(pMessage.uID, pMessage.ids.get(0));
			}

			if (NetworkConfig.status.get().equals(States.Server))
				this.outputConnection.sendMessage(message);
			else
				if (NetworkConfig.status.get().equals(States.BoundToServer))
					if (message.fromServer && this.clientID != TCPCoordinator.localNetworkMainID.get())
						this.outputConnection.sendMessage(message);
					else
						if (!message.fromServer && this.clientID == TCPCoordinator.localNetworkMainID.get())
							this.outputConnection.sendMessage(message);
						else
							if (message.goodbye && this.clientID != TCPCoordinator.localNetworkMainID.get())
								this.outputConnection.sendMessage(message);
		}
		else
			if (topic.equals(TOPICS.MESSAGE_CHANNEL_RECEIVED.toString()))
			{
				final Message message = (Message) this.inputConnection.getNextMessage();

				if (message.goodbye)
				{
					this.close();
					EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this.clientID);
					return;
				}

				if (!NetworkConfig.status.get().equals(States.Server))
				{
					if (this.clientID == TCPCoordinator.localNetworkMainID.get())
					{
						message.fromServer = true;
						EventBusExtended.publishAsyncSafe(MessageBus.MESSAGE_RECEIVE, message); // Publish
						// internally
					}
					else
					{
						message.fromServer = false;
						if (message instanceof ParticipantMessage)
						{
							final ParticipantMessage pMessage = (ParticipantMessage) message;
							this.uid_pid_map.put(pMessage.uID, null);
						}
					}
					EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, message); // FORWARD!
				}
				else
				{
					message.fromServer = false;
					if (message instanceof ParticipantMessage)
					{
						final ParticipantMessage pMessage = (ParticipantMessage) message;
						this.uid_pid_map.put(pMessage.uID, null);
					}
					EventBusExtended.publishAsyncSafe(MessageBus.MESSAGE_RECEIVE, message); // Publish
					// internally
				}
			}
			else
			{
				this.close();
				EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this.clientID);
			}
		System.out.println(this.uid_pid_map.toString());
	}

	public synchronized void close()
	{
		System.out.println("Closing " + this.getClass().getSimpleName());

		AnnotationProcessor.unprocess(this);
		this.eventBus.unsubscribe(TOPICS.MESSAGE_CHANNEL_RECEIVED.toString(), this);
		this.eventBus.unsubscribe(TOPICS.CONNECTION_CHANNEL_LOST.toString(), this);

		try
		{
			this.outputConnection.terminate();
			this.inputConnection.terminate();
			this.clientSocket.close(); // Now we can close the Socket
		}
		catch (final IOException e)
		{}

		System.out.println(this.getClass().getSimpleName() + " closed!");
	}
}
