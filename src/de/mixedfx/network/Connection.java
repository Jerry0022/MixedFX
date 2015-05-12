package de.mixedfx.network;

import java.io.IOException;
import java.net.Socket;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.eventbus.EventBusServiceInterface;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.Message;

public class Connection implements EventBusServiceInterface
{
	public static final String	MESSAGE_CHANNEL_SEND	= "MESSAGE_CHANNEL_SEND";

	public enum TOPICS
	{
		MESSAGE_CHANNEL_RECEIVED, CONNECTION_LOST;
	}

	private final int					clientID;
	private final Socket				clientSocket;
	private EventBusService				eventBus;

	private volatile ConnectionOutput	outputConnection;
	private volatile ConnectionInput	inputConnection;

	public Connection(final int clientID, final Socket clientSocket) throws IOException
	{
		System.out.println("Initializing " + this.getClass().getSimpleName());

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
		this.eventBus.subscribe(TOPICS.CONNECTION_LOST.toString(), this);

		AnnotationProcessor.process(this);
	}

	@Override
	@EventTopicSubscriber(topic = Connection.MESSAGE_CHANNEL_SEND)
	public synchronized void onEvent(final String topic, final Object event)
	{
		if (topic.equals(Connection.MESSAGE_CHANNEL_SEND))
		{
			final Message message = (Message) event;
			final String gsonMessage = Message.toGSON(message);
			if (NetworkConfig.status.get().equals(States.Server))
			{
				message.fromServer = true;
				this.outputConnection.sendMessage(gsonMessage);
			}
			else
				if (NetworkConfig.status.get().equals(States.BoundToServer))
					if (message.fromServer && this.clientID != TCPCoordinator.localNetworkMainID.get())
						this.outputConnection.sendMessage(gsonMessage);
					else
						if (!message.fromServer && this.clientID == TCPCoordinator.localNetworkMainID.get())
							this.outputConnection.sendMessage(gsonMessage);
		}
		else
			if (topic.equals(TOPICS.MESSAGE_CHANNEL_RECEIVED.toString()))
			{
				final Message message = Message.fromGSON((String) this.inputConnection.getNextMessage());
				if (message.fromServer)
					EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, message); // FORWARD!
				EventBusExtended.publishAsyncSafe(MessageBus.MESSAGE_RECEIVE, message);
			}
			else
				EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this.clientID);
	}

	public synchronized void close()
	{
		System.out.println("Closing " + this.getClass().getSimpleName());
		this.outputConnection.terminate();
		this.inputConnection.terminate();

		try
		{
			this.clientSocket.close();
		}
		catch (final IOException e)
		{}

		System.out.println(this.getClass().getSimpleName() + " closed!");
	}
}
