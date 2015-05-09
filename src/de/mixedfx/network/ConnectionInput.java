package de.mixedfx.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.network.messages.Message;

public class ConnectionInput implements Runnable
{
	private static final Class<?>		parentClass	= Connection.class;

	private volatile boolean			isRunning	= true;

	private final ObjectInputStream		objectInputStream;
	private final ArrayList<Message>	inputMessageCache;
	private final EventBusService		eventBusParent;

	protected ConnectionInput(final int clientID, final InputStream inputStream) throws IOException
	{
		this.eventBusParent = EventBusService.getEventBus(ConnectionInput.parentClass, clientID);
		this.objectInputStream = new ObjectInputStream(inputStream);

		this.inputMessageCache = new ArrayList<Message>();

		System.out.println(this.getClass().getSimpleName() + " initialized!");
	}

	protected Message getNextMessage()
	{
		synchronized (this.inputMessageCache)
		{
			if (!this.inputMessageCache.isEmpty())
			{
				final Message message = this.inputMessageCache.get(0);
				this.inputMessageCache.remove(0);
				return message;
			}
			else
				return null;
		}
	}

	protected boolean isMessageRemaining()
	{
		synchronized (this.inputMessageCache)
		{
			return !this.inputMessageCache.isEmpty();
		}
	}

	@Override
	public void run()
	{
		Object receivedObject;
		while (this.isRunning)
			try
			{
				receivedObject = this.objectInputStream.readObject();

				if (receivedObject instanceof Message)
				{
					final Message receivedMessage = (Message) receivedObject;
					synchronized (this.inputMessageCache)
					{
						this.inputMessageCache.add(receivedMessage);
					}
					System.out.println(this.getClass().getSimpleName() + " received message!");
					this.eventBusParent.publishAsync(Connection.TOPICS.MESSAGE_RECEIVED.toString(), this);
				}
				else
					throw new Exception("Not a message received! Object rejected!");
		}
			catch (final EOFException e)
			{} // Nothing received, still waiting
		catch (ClassNotFoundException | IOException e)
			{
			if (this.isRunning)
			{
				if (e instanceof NotSerializableException || e.getCause() instanceof NotSerializableException)
				{
					System.out.println("A class is not serializable! Implement Serializable Interface!");
					System.out.print(e.getMessage());
				}

				synchronized (this.inputMessageCache)
				{
					this.inputMessageCache.clear();
					this.terminate();
					System.out.println(this.getClass().getSimpleName() + " lost stream!");
					this.eventBusParent.publishAsync(Connection.TOPICS.CONNECTION_LOST.toString(), this);
				}
			}
		}
		catch (final Exception e)
		{
			// TODO: Handle Exception
			e.printStackTrace();
		}
	}

	protected synchronized boolean terminate()
	{
		if (this.isRunning)
		{
			System.out.println("Terminating ConnectionInput!");
			this.isRunning = false;
			try
			{
				this.objectInputStream.close();
			}
			catch (final IOException e)
			{}
			System.out.println(this.getClass().getSimpleName() + " terminated!");
			return true;
		}
		else
			return false;
	}
}
