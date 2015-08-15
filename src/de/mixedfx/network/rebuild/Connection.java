package de.mixedfx.network.rebuild;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.eventbus.EventBusServiceInterface;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.messages.GoodByeMessage;
import de.mixedfx.network.rebuild.messages.Message;

public class Connection implements EventBusServiceInterface
{
	protected static final String	MESSAGE_CHANNEL_SEND		= "MESSAGE_CHANNEL_SEND";
	protected static final String	MESSAGE_CHANNEL_RECEIVED	= "MESSAGE_CHANNEL_RECEIVED";
	protected static final String	CONNECTION_CHANNEL_LOST		= "CONNECTION_CHANNEL_LOST";

	public final InetAddress ip;

	private final Socket			clientSocket;
	private final ConnectionOutput	outputConnection;
	private final ConnectionInput	inputConnection;

	private EventBusService eventBus;

	public Connection(final Socket clientSocket) throws IOException
	{
		Log.network.debug("Initializing " + this.getClass().getSimpleName() + " with " + clientSocket.getInetAddress());

		this.ip = clientSocket.getInetAddress();

		this.clientSocket = clientSocket;
		// this.clientSocket.setKeepAlive(true);
		// this.clientSocket.setSoLinger(true, 0);
		// this.clientSocket.setTcpNoDelay(true);
		// this.clientSocket.setSoTimeout(5000);
		/*
		 * TODO Sometimes the outputstream is not flushing and therefore the inputstream constructor is blocking! Use this.clientSocket.setSO_Linger() or .setNoTCPDelay to fix this!
		 */

		this.initilizeEventBusAndSubscriptions();

		this.outputConnection = new ConnectionOutput(clientSocket.getOutputStream(), ip);
		Inspector.runNowAsDaemon(this.outputConnection);

		this.inputConnection = new ConnectionInput(clientSocket.getInputStream(), ip);
		Inspector.runNowAsDaemon(this.inputConnection);

		Log.network.debug(this.getClass().getSimpleName() + " initialized!");
	}

	@Override
	public void initilizeEventBusAndSubscriptions()
	{
		this.eventBus = new EventBusService(this.getClass() + this.clientSocket.getRemoteSocketAddress().toString().split(":")[0]);
		this.eventBus.subscribe(Connection.MESSAGE_CHANNEL_RECEIVED, this);
		this.eventBus.subscribe(Connection.CONNECTION_CHANNEL_LOST, this);
		AnnotationProcessor.process(this);
	}

	@Override
	@EventTopicSubscriber(topic = Connection.MESSAGE_CHANNEL_SEND)
	public synchronized void onEvent(final String topic, final Object event)
	{
		if (topic.equals(Connection.MESSAGE_CHANNEL_SEND))
		{
			final Message message = (Message) event;
			if (message.getToIP() == null || message.getToIP().equals(this.ip))
			{
				message.setToIP(this.ip);
				this.outputConnection.sendMessage(message);
			}
		} else if (topic.equals(Connection.MESSAGE_CHANNEL_RECEIVED))
		{
			final Message message = (Message) this.inputConnection.getNextMessage();
			message.setFromIP(this.ip);
			if (message instanceof GoodByeMessage)
			{
				Log.network.debug("Got GoodByeMessage!");
				this.close();
				EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this);
			} else
				EventBusExtended.publishAsyncSafe(MessageBus.MESSAGE_RECEIVE, message);
		} else
		{
			this.close();
			EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this);
		}
	}

	public synchronized void close()
	{
		Log.network.debug("Closing " + this.getClass().getSimpleName());

		AnnotationProcessor.unprocess(this);
		this.eventBus.unsubscribe(Connection.CONNECTION_CHANNEL_LOST, this);
		this.eventBus.unsubscribe(Connection.MESSAGE_CHANNEL_RECEIVED, this);

		try
		{
			while (!this.outputConnection.outputMessageCache.isEmpty())
				;
			// Terminate first to processing last remaining steps.
			this.outputConnection.terminate();
			this.inputConnection.terminate();
			// Close socket to be sure that everything was closed.
			this.clientSocket.close();
		} catch (final IOException e)
		{
		}

		Log.network.debug(this.getClass().getSimpleName() + " closed!");
	}
}
