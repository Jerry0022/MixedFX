package de.mixedfx.network;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.eventbus.EventBusServiceInterface;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.GoodByeMessage;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.ParticipantMessage;
import de.mixedfx.network.messages.RegisteredMessage;

public class Connection implements EventBusServiceInterface
{
	protected static final String	MESSAGE_CHANNEL_SEND		= "MESSAGE_CHANNEL_SEND";
	protected static final String	MESSAGE_CHANNEL_RECEIVED	= "MESSAGE_CHANNEL_RECEIVED";
	protected static final String	CONNECTION_CHANNEL_LOST		= "CONNECTION_CHANNEL_LOST";

	/**
	 * A mapping of all going through uids to pids, whereby the pid value may be null.
	 */
	public final HashMap<String, Integer> uid_pid_map;

	/**
	 * Represents the local connection ID
	 */
	public final int clientID;

	private final Socket			clientSocket;
	private final ConnectionOutput	outputConnection;
	private final ConnectionInput	inputConnection;

	private EventBusService eventBus;

	public Connection(final int clientID, final Socket clientSocket) throws IOException
	{
		Log.network.debug("Initializing " + this.getClass().getSimpleName() + " with " + clientSocket.getRemoteSocketAddress());

		this.uid_pid_map = new HashMap<>();

		this.clientID = clientID;
		this.clientSocket = clientSocket;

		this.initilizeEventBusAndSubscriptions();

		this.outputConnection = new ConnectionOutput(this.clientID, clientSocket.getOutputStream());
		Inspector.runNowAsDaemon(this.outputConnection);

		this.inputConnection = new ConnectionInput(this.clientID, clientSocket.getInputStream());
		Inspector.runNowAsDaemon(this.inputConnection);

		Log.network.debug(this.getClass().getSimpleName() + " initialized!");
	}

	@Override
	public void initilizeEventBusAndSubscriptions()
	{
		this.eventBus = new EventBusService(this.getClass(), this.clientID);
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

			if (NetworkConfig.STATUS.get().equals(States.Server))
			{
				message.fromServer = true;
			}
			// Else it may be from server or not

			if (message.fromServer)
			{
				this.checkParticipantMessage(message);
			}

			if (NetworkConfig.STATUS.get().equals(States.Server))
			{
				this.checkSend(message);
			}
			else if (NetworkConfig.STATUS.get().equals(States.BoundToServer))
			{
				if (message.fromServer)
				{
					// Don't send it back to server if it comes from the server! Just forward it!
					if (this.clientID != TCPCoordinator.localNetworkMainID)
					{
						this.checkSend(message);
						if (message instanceof GoodByeMessage)
						{
							this.outputConnection.sendMessage(message);
							// This connection is closed in TCPCoordinator!
						}
					}
				}
				else
				{
					if (message instanceof GoodByeMessage)
					{
						this.outputConnection.sendMessage(message);
						// This connection is closed in TCPCoordinator!
					}
					else if (this.clientID == TCPCoordinator.localNetworkMainID)
					{
						checkSend(message);
					}
				}
			}
			// Else can't exist. Because this connection only exists if I'm part of the network.
		}
		else if (topic.equals(Connection.MESSAGE_CHANNEL_RECEIVED))
		{
			final Message message = (Message) this.inputConnection.getNextMessage();

			if (message instanceof GoodByeMessage)
			{
				this.close();
				EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this.clientID);
				return;
			}

			if (NetworkConfig.STATUS.get().equals(States.Server))
			{
				message.fromServer = false;
				this.checkParticipantMessage(message);
				this.checkReceive(message);
			}
			else
			{
				if (this.clientID == TCPCoordinator.localNetworkMainID)
				{
					message.fromServer = true;
					this.checkReceive(message); // May publish internally
				}
				else
				{
					message.fromServer = false;
					// Add Participants requests to my list.
					this.checkParticipantMessage(message);
				}

				EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
			}
		}
		else
		{
			this.close();
			EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this.clientID);
		}
	}

	/**
	 * @param message
	 *            The message which shall be checked!
	 */
	private void checkParticipantMessage(final Message message)
	{
		if (message instanceof ParticipantMessage)
		{
			final ParticipantMessage pMessage = (ParticipantMessage) message;

			if (!pMessage.uID.equals(""))
			{
				if (pMessage.ids.isEmpty())
				{
					// PID request of a client. Create entry.
					this.uid_pid_map.put(pMessage.uID, null);
				}
				else
				{
					// PID response from server. Update entry.
					if (this.uid_pid_map.containsKey(pMessage.uID) && this.uid_pid_map.get(pMessage.uID) == null)
					{
						this.uid_pid_map.replace(pMessage.uID, pMessage.ids.get(0));
					}
				}
			}
			else
			{
				// Some PIDs, connected to other clients, were lost
				for (final int lost : pMessage.ids)
				{
					this.uid_pid_map.values().remove(lost);
				}
			}
			Log.network.info("This is my connection id: " + clientID + " and I have the following pids connected: " + uid_pid_map.values());
		}
	}

	/**
	 * If not RegisteredMessage publish message immediately!
	 * 
	 * @param message
	 *            A message which shall be received.
	 */
	private void checkReceive(final Message message)
	{
		if (message instanceof RegisteredMessage)
		{
			final RegisteredMessage regMessage = (RegisteredMessage) message;

			// If it is for me or it is a broadcast and I am not the sender, publish the message
			// internally
			if ((regMessage.receivers.contains(ParticipantManager.MY_PID.get()) || regMessage.receivers.isEmpty()) && regMessage.sender != ParticipantManager.MY_PID.get())
			{
				Log.network.warn("Pubslishing message internally, created on: " + regMessage.creationTime);
				EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_RECEIVE, message); // Publish internally
			}
			// Asks services to process message before forwarding
			if (NetworkConfig.STATUS.get().equals(States.Server))
			{
				ServiceManager.hostCheckMessage(regMessage);
			}
		}
		else
		{
			EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_RECEIVE, message); // Publish
			// internally
		}
	}

	/**
	 * If it is not a RegisteredMessage just send! If the sender is registered (has a pid) then check if I'm a connection where a receiver of the
	 * message is connected to. If no relevant receiver is connected to just do nothing! If broadcast send it immediately
	 * 
	 * @param message
	 *            A message which shall be sent.
	 */
	private void checkSend(final Message message)
	{
		if (message instanceof RegisteredMessage && !((RegisteredMessage) message).receivers.isEmpty())
		{
			boolean atLeastOne = false;
			for (final Integer receiver : ((RegisteredMessage) message).receivers)
			{
				if (this.uid_pid_map.containsValue(receiver))
				{
					atLeastOne = true;
				}
			}
			if (atLeastOne)
			{
				this.outputConnection.sendMessage(message);
			}
		}
		else
		{
			this.outputConnection.sendMessage(message);
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
			// Terminate first to processing last remaining steps.
			this.outputConnection.terminate();
			this.inputConnection.terminate();
			// Close socket to be sure that everything was closed.
			this.clientSocket.close();
		}
		catch (final IOException e)
		{
		}

		Log.network.debug(this.getClass().getSimpleName() + " closed!");
	}
}
