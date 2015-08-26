package de.mixedfx.java.single;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * From http://sourceforge.net/projects/javasic/
 */

/**
 * SingleInstanceController is a class, which enables you to check, if your application is already running on the system. In this case, you can communicate with the older application. This class works
 * application and port dependent.
 *
 * @author Stefan Kiesel
 * @since 1.5
 * @version 1.0
 */
public class SingleInstanceController
{
	private boolean									result		= false;
	private File									file		= null;
	private ObjectOutputStream						oos			= null;
	private ObjectInputStream						ois			= null;
	private ServerSocket							server		= null;
	private Socket									client		= null;
	private ArrayList<ApplicationStartedListener>	listener	= null;
	private String									appname		= null;

	/**
	 * Creates a new SingleInstanceController with a spefic application name (this name is importend to compare the running applications. Try to use an unique name for each application) and a
	 * location, where the used port number should be/is saved.
	 *
	 * @param file
	 *            port location
	 * @param appname
	 *            unique identification for each application (not instance)
	 * @see #SingleInstanceController(String)
	 */
	public SingleInstanceController(final File file, final String appname)
	{

		this.file = file;
		this.appname = appname;
		this.listener = new ArrayList<ApplicationStartedListener>();
	}

	/**
	 * Creates a new SingleInstanceController without a spefic location to save the port number. You should better use {@link #SingleInstanceController(File, String)}, because any application uses by
	 * default this path.
	 *
	 * @param appname
	 *            unique identification for each application (not instance)
	 * @see #SingleInstanceController(File, String)
	 */
	public SingleInstanceController(final String appname)
	{
		this(new File(System.getProperty("java.io.tmpdir") + "/923jhakE53Kk9235b43.6m7"), appname);
	}

	/**
	 * Adds an ApplicationStartedListener
	 *
	 * @param asl
	 *            the ApplicationStartedListener
	 * @see #removeApplicationStartedListener(ApplicationStartedListener)
	 */
	public void addApplicationStartedListener(final ApplicationStartedListener asl)
	{
		this.listener.add(asl);
	}

	/**
	 * Notifies the listeners, that an other instance of this application was started and successful connected to this instance.
	 */
	protected void applicationStartet()
	{

		for (final ApplicationStartedListener asl : this.listener)
		{
			asl.applicationStarted();
		}
	}

	/**
	 * Notifies the listeners, that an other application tried to connect to this port.
	 */
	protected void foreignApplicationStarted(final String name)
	{

		for (final ApplicationStartedListener asl : this.listener)
		{
			asl.foreignApplicationStarted(name);
		}
	}

	/**
	 * Detect a free port number between 2000 and 10000
	 *
	 * @return the first free port or -1 if no port is free.
	 */
	private int getFreeServerSocket()
	{

		for (int i = 2000; i < 10000; i++)
		{
			try
			{
				this.server = new ServerSocket(i);
				return i;
			} catch (final IOException ignore)
			{
			}
		}
		return -1;
	}

	/**
	 * Detect the last set port number
	 *
	 * @return the last set port number
	 */
	private int getPortNumber()
	{

		try
		{
			final BufferedReader buffy = new BufferedReader(new FileReader(this.file));
			final int port = Integer.parseInt(buffy.readLine().trim());
			buffy.close();
			return port;
		} catch (final Exception e)
		{
			return -1;
		}
	}

	/**
	 * Checks, if an other instance of this application is already running on the system
	 *
	 * @return true if it is sure, that an other instance is running. Otherwise false
	 */
	public boolean isOtherInstanceRunning()
	{

		if (!this.file.exists())
		{
			return false;
		}
		return this.sendMessageToRunningApplication(new ClassCheck(this.appname));
	}

	/**
	 * Notifies the listeners, that an incomming message arrived
	 *
	 * @param obj
	 *            Objekt fuer die Listener
	 */
	protected void messageArrived(final Object obj)
	{

		for (final ApplicationStartedListener asl : this.listener)
		{
			asl.messageArrived(obj);
		}
	}

	/**
	 * Registers this application as started. All later started instances can communicate with this instance. You can register the instance when an other instance is still running, too.
	 *
	 * @return true if the application was successful registered
	 */
	public boolean registerApplication()
	{

		try
		{
			if (!this.file.exists())
			{
				if (!this.file.getParentFile().mkdirs() && !this.file.getParentFile().exists())
				{
					return false;
				}
				if (!this.file.createNewFile())
				{
					return false;
				}
			}
			final BufferedWriter wuffy = new BufferedWriter(new FileWriter(this.file));
			final int port = this.getFreeServerSocket();
			if (port != -1)
			{
				this.startServer();
			}
			wuffy.write(String.valueOf(port));
			wuffy.close();
			return true;
		} catch (final IOException e)
		{
			return false;
		}
	}

	/**
	 * Removes an ApplicationStartedListener
	 *
	 * @param asl
	 *            the ApplicationStartedListener
	 * @see #addApplicationStartedListener(ApplicationStartedListener)
	 */
	public void removeApplicationStartedListener(final ApplicationStartedListener asl)
	{
		this.listener.remove(asl);
	}

	/**
	 * Sends a message to the first started instance. Before calling this method, ensure that an other instance is running.
	 *
	 * @param obj
	 *            the object, you would like to send
	 * @return true if the object was sended successful
	 * @see #isOtherInstanceRunning()
	 */
	public boolean sendMessageToRunningApplication(final Object obj)
	{

		this.result = false;
		try
		{
			this.client = new Socket("localhost", this.getPortNumber());
			// new Thread to avoid a deadlock
			new Thread(() ->
			{
				try
				{
					SingleInstanceController.this.oos = new ObjectOutputStream(SingleInstanceController.this.client.getOutputStream());
					SingleInstanceController.this.ois = new ObjectInputStream(SingleInstanceController.this.client.getInputStream());
					SingleInstanceController.this.oos.writeObject(obj);
					SingleInstanceController.this.oos.flush();
					SingleInstanceController.this.result = SingleInstanceController.this.ois.readBoolean();
				} catch (final IOException e)
				{
					SingleInstanceController.this.result = false;
				}
			}).start();
			// After 1 second = server not reachable
			for (int i = 0; i < 10; i++)
			{
				if (this.result == true)
				{
					break;
				}
				try
				{
					Thread.sleep(100);
				} catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			this.client.close();
			return this.result;
		} catch (final IOException e)
		{
			return false;
		}
	}

	/**
	 * Starts the server to communicate with other applications/instances
	 */
	private void startServer()
	{

		new Thread(() ->
		{
			while (true)
			{
				try
				{
					SingleInstanceController.this.client = SingleInstanceController.this.server.accept();
					if (SingleInstanceController.this.client.getInetAddress().getCanonicalHostName().equalsIgnoreCase("localhost"))
					{
						new Thread(() ->
						{
							try
							{
								SingleInstanceController.this.oos = new ObjectOutputStream(SingleInstanceController.this.client.getOutputStream());
								SingleInstanceController.this.ois = new ObjectInputStream(SingleInstanceController.this.client.getInputStream());
								final Object obj = SingleInstanceController.this.ois.readObject();
								if (obj instanceof ClassCheck)
								{
									if (obj.toString().equals(SingleInstanceController.this.appname))
									{
										SingleInstanceController.this.oos.writeBoolean(true);
										SingleInstanceController.this.applicationStartet();
									} else
									{
										SingleInstanceController.this.oos.writeBoolean(false);
										SingleInstanceController.this.foreignApplicationStarted(obj.toString());
									}
								} else
								{
									SingleInstanceController.this.messageArrived(obj);
									SingleInstanceController.this.oos.writeBoolean(true);
								}
								SingleInstanceController.this.oos.flush();
								SingleInstanceController.this.client.close();
							} catch (final IOException e1)
							{
								e1.printStackTrace();
							} catch (final ClassNotFoundException e2)
							{
								e2.printStackTrace();
							}
						}).start();
					}
				} catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}
}
