package de.mixedfx._network;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import de.mixedfx.eventbus.EventBusService;

public class ConnectionInput implements Runnable
{
	private static final Class<?>			parentClass	= Connection.class;

	private volatile boolean				isRunning	= true;

	private final ObjectInputStream			objectInputStream;
	private final ArrayList<Serializable>	inputMessageCache;
	private final EventBusService			eventBusParent;

	protected ConnectionInput(final int clientID, final InputStream inputStream) throws IOException
	{
		this.eventBusParent = EventBusService.getEventBus(ConnectionInput.parentClass, clientID);
		this.objectInputStream = new ObjectInputStream(inputStream);

		this.inputMessageCache = new ArrayList<Serializable>();

		System.out.println(this.getClass().getSimpleName() + " initialized!");
	}

	protected Serializable getNextMessage()
	{
		synchronized (this.inputMessageCache)
		{
			if (!this.inputMessageCache.isEmpty())
			{
				final Serializable message = this.inputMessageCache.get(0);
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

				if (receivedObject instanceof Serializable)
				{
					final Serializable receivedMessage = (Serializable) receivedObject;
					synchronized (this.inputMessageCache)
					{
						this.inputMessageCache.add(receivedMessage);
					}
					System.out.println(this.getClass().getSimpleName() + " received message!");
					this.eventBusParent.publishAsync(Connection.TOPICS.MESSAGE_CHANNEL_RECEIVED.toString(), this);
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
						this.eventBusParent.publishAsync(Connection.TOPICS.CONNECTION_CHANNEL_LOST.toString(), this);
					}
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
	}

	protected synchronized boolean terminate()
	{
		if (this.isRunning)
		{
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