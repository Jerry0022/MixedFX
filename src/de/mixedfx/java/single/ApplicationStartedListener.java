package de.mixedfx.java.single;

/*
 * From http://sourceforge.net/projects/javasic/
 */
public interface ApplicationStartedListener
{

	/**
	 * Called if an instance of the same application was started and successful connected to the server.
	 */
	public void applicationStarted();

	/**
	 * Called if an instance of an other application has tried to connect to this port.
	 *
	 * @param name
	 *            the appname
	 */
	public void foreignApplicationStarted(String name);

	/**
	 * Called if an instance of the same application sended a message
	 *
	 * @param obj
	 *            the message
	 */
	public void messageArrived(Object obj);
}
