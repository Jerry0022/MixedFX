package de.mixedfx.network;

import java.io.IOException;
import java.net.Socket;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.eventbus.EventBusServiceInterface;
import de.mixedfx.network.Overall.NetworkStatus;
import de.mixedfx.network.messages.Message;

public class Connection implements EventBusServiceInterface
{
	public static final String	MESSAGE_SEND	= "MESSAGE_SEND";

	public enum TOPICS
	{
		MESSAGE_RECEIVED, CONNECTION_LOST;
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
		this.eventBus.subscribe(TOPICS.MESSAGE_RECEIVED.toString(), this);
		this.eventBus.subscribe(TOPICS.CONNECTION_LOST.toString(), this);

		AnnotationProcessor.process(this);
	}

	@Override
	@EventTopicSubscriber(topic = Connection.MESSAGE_SEND)
	public synchronized void onEvent(final String topic, final Object event)
	{
		if (topic.equals(Connection.MESSAGE_SEND))
		{
			final Message message = (Message) event;
			if (Overall.status.equals(NetworkStatus.Server))
			{
				message.fromServer = true;
				this.outputConnection.sendMessage(message);
			}
			else
				if (Overall.status.equals(NetworkStatus.BoundToServer))
					if (message.fromServer && this.clientID != TCPCoordinator.localNetworkMainID.get())
						this.outputConnection.sendMessage(message);
					else
						if (!message.fromServer && this.clientID == TCPCoordinator.localNetworkMainID.get())
							this.outputConnection.sendMessage(message);
		}
		else
			if (topic.equals(TOPICS.MESSAGE_RECEIVED.toString()))
			{
				final Message message = this.inputConnection.getNextMessage();
				System.out.println("Message received, I'm: " + this.clientID);
				if (message.fromServer)
					EventBusExtended.publishSyncSafe(Connection.MESSAGE_SEND, message); // FORWARD!
				// TODO Publish to local application in each case
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
