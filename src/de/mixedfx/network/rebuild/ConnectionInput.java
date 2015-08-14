package de.mixedfx.network.rebuild;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.logging.Log;

public class ConnectionInput implements Runnable
{
	private static final Class<?> parentClass = Connection.class;

	private volatile boolean isRunning = true;

	private final ObjectInputStream			objectInputStream;
	private final ArrayList<Serializable>	inputMessageCache;
	private final EventBusService			eventBusParent;

	protected ConnectionInput(final InputStream inputStream, InetAddress ip) throws IOException
	{
		this.eventBusParent = EventBusService.getEventBus(ConnectionInput.parentClass + ip.toString());
		this.objectInputStream = new ObjectInputStream(inputStream);

		this.inputMessageCache = new ArrayList<Serializable>();

		Log.network.trace(this.getClass().getSimpleName() + " initialized!");
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
			} else
			{
				return null;
			}
		}
	}

	@Override
	public void run()
	{
		Object receivedObject;
		while (this.isRunning)
		{
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
					Log.network.trace(this.getClass().getSimpleName() + " received message!");
					this.eventBusParent.publishAsync(Connection.MESSAGE_CHANNEL_RECEIVED, this);
				} else
				{
					Log.network.warn(new Exception("Not a message received! Object rejected!"));
				}
			} catch (final EOFException e)
			{
			} // Nothing received, still waiting
			catch (ClassNotFoundException | IOException e)
			{
				if (this.isRunning)
				{
					if (e instanceof NotSerializableException || e.getCause() instanceof NotSerializableException)
					{
						Log.network.error(new Exception("A class is not serializable! Implement Serializable interface!"));
					}

					synchronized (this.inputMessageCache)
					{
						this.inputMessageCache.clear();
						this.terminate();
						Log.network.debug(this.getClass().getSimpleName() + " lost stream! Exception: " + e.getMessage());
						if (this.eventBusParent != null)
							this.eventBusParent.publishSync(Connection.CONNECTION_CHANNEL_LOST, this);
					}
				}
			} catch (final Exception e)
			{
				e.printStackTrace();
			}
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
			} catch (final IOException e)
			{
			}
			Log.network.trace(this.getClass().getSimpleName() + " terminated!");
			return true;
		} else
		{
			return false;
		}
	}
}
