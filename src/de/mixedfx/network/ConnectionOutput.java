package de.mixedfx.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.logging.Log;

public class ConnectionOutput implements Runnable
{
	private static final Class<?>				parentClass	= Connection.class;

	private volatile boolean					running		= true;

	private final ObjectOutputStream			objectOutputStream;
	private volatile ArrayList<Serializable>	outputMessageCache;
	private final EventBusService				eventBusParent;

	protected ConnectionOutput(final int clientID, final OutputStream outputStream) throws IOException
	{
		this.eventBusParent = EventBusService.getEventBus(ConnectionOutput.parentClass, clientID);
		this.objectOutputStream = new ObjectOutputStream(outputStream);
		this.outputMessageCache = new ArrayList<Serializable>();

		Log.network.trace(this.getClass().getSimpleName() + " initialized!");
	}

	protected void sendMessage(final Serializable message)
	{
		synchronized (this.outputMessageCache)
		{
			Log.network.trace(this.getClass().getSimpleName() + " sends a Message!");
			this.outputMessageCache.add(message);
		}
	}

	@Override
	public void run()
	{
		while (this.running)
		{
			synchronized (this.outputMessageCache)
			{
				if (this.outputMessageCache.size() > 0)
				{
					Log.network.trace("TCP Output Cache Size: " + this.outputMessageCache.size());
					try
					{
						this.objectOutputStream.reset(); // GBC can collect written objects
						this.objectOutputStream.writeObject(this.outputMessageCache.get(0));
						this.outputMessageCache.remove(0);
						Log.network.trace(this.getClass().getSimpleName() + " message sent!");
					}
					catch (final IOException e)
					{
						this.outputMessageCache.clear();
						if (this.terminate())
						{
							Log.network.trace("OutputStream lost!");
							this.eventBusParent.publishAsync(Connection.CONNECTION_CHANNEL_LOST, this);
						}
					}
				}
			}
			// Only send in 10 milliseconds interval
			try
			{
				Thread.sleep(10);
			}
			catch (final InterruptedException e)
			{}
		}
	}

	protected synchronized boolean terminate()
	{
		if (this.running)
		{
			while (!this.outputMessageCache.isEmpty())
			{
				try
				{
					Thread.sleep(50);
				}
				catch (final InterruptedException e)
				{}
			}

			Log.network.trace("Terminating " + this.getClass().getSimpleName() + "!");
			this.running = false;

			try
			{
				this.objectOutputStream.close();
			}
			catch (final IOException e)
			{
				// In rare cases could be called twice, therefore soft
				// Exception is needed, no impact!
				Log.network.trace("WRITE ERROR! No reason to fear about this :D!");
			}
			Log.network.trace(this.getClass().getSimpleName() + " terminated!");
			return true;
		}
		else
		{
			return false;
		}
	}
}
