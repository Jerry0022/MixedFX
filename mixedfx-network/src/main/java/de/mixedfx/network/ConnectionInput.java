package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusService;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

@Log4j2(topic = "Network")
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

		this.inputMessageCache = new ArrayList<>();

		log.trace(this.getClass().getSimpleName() + " initialized!");
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
		while (this.isRunning)
		{
			try
			{
				Object receivedObject = this.objectInputStream.readObject();

				if (receivedObject instanceof Serializable)
				{
					final Serializable receivedMessage = (Serializable) receivedObject;
					synchronized (this.inputMessageCache)
					{
						this.inputMessageCache.add(receivedMessage);
					}
					log.trace(this.getClass().getSimpleName() + " received message!");
					this.eventBusParent.publishAsync(Connection.MESSAGE_CHANNEL_RECEIVED, this);
				} else
				{
					log.warn(new Exception("Not a message received! Object rejected!"));
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
						log.error(new Exception("A class is not serializable! Implement Serializable interface!"));
					}

					synchronized (this.inputMessageCache)
					{
						this.inputMessageCache.clear();
						this.terminate();
						log.debug(this.getClass().getSimpleName() + " lost stream! Exception: " + e.getMessage());
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
			} catch (final IOException ignored)
			{
			}
			log.trace(this.getClass().getSimpleName() + " terminated!");
			return true;
		} else
		{
			return false;
		}
	}
}
