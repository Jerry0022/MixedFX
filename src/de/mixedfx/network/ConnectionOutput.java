package de.mixedfx.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.network.messages.Message;

public class ConnectionOutput implements Runnable
{
	private static final Class<?>		parentClass	= Connection.class;

	private volatile boolean			running		= true;

	private final ObjectOutputStream	objectOutputStream;
	private volatile ArrayList<Message>	outputMessageCache;
	private final EventBusService		eventBusParent;

	protected ConnectionOutput(final int clientID, final OutputStream outputStream) throws IOException
	{
		this.eventBusParent = EventBusService.getEventBus(ConnectionOutput.parentClass, clientID);
		this.objectOutputStream = new ObjectOutputStream(outputStream);
		this.outputMessageCache = new ArrayList<Message>();

		System.out.println(this.getClass().getSimpleName() + " initialized!");
	}

	protected void sendMessage(final Message message)
	{
		synchronized (this.outputMessageCache)
		{
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
					System.out.println("Output Cache Size: " + this.outputMessageCache.size());
					try
					{
						this.objectOutputStream.reset(); // GBC can collect written objects
						this.objectOutputStream.writeObject(this.outputMessageCache.get(0));
						this.outputMessageCache.remove(0);
						System.out.println(this.getClass().getSimpleName() + " message sent!");
					}
					catch (final IOException e)
					{
						this.outputMessageCache.clear();
						if (this.terminate())
						{
							System.out.println("OutputStream lost!");
							this.eventBusParent.publishAsync(Connection.TOPICS.CONNECTION_LOST.toString(), this);
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
				try
				{
					Thread.sleep(50);
				}
			catch (final InterruptedException e)
				{
					// TODO: Handle Exception
					e.printStackTrace();
				}

			System.out.println("Terminating " + this.getClass().getSimpleName() + "!");
			this.running = false;

			try
			{
				this.objectOutputStream.close();
			}
			catch (final IOException e)
			{
				// In rare cases could be called twice, therefore soft
				// Exception is needed, no impact!
				// TODO: Handle Exception
				// e.printStackTrace();
				System.err.println("WRITE ERROR! No reason to fear about this :D!");
			}
			System.out.println(this.getClass().getSimpleName() + " terminated!");
			return true;
		}
		else
			return false;
	}
}
