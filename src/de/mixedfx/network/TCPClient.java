package de.mixedfx.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TCPClient
{
	public Connection	connection;

	public void start(final InetAddress ip, final int port) throws IOException
	{
		IOException exception = null;
		Socket socket = null;
		for (int i = 0; i < TCPCoordinator.PORT_TRIES; i++)
			try
		{
				socket = new Socket(ip, port);
				this.connection = new Connection(0, socket);
				break;
		}
		catch (final SocketException | UnknownHostException e)
		{
			exception = e;
		}
		if (socket == null)
			throw exception;
	}

	public void stop()
	{
		if (this.connection != null)
			this.connection.close();
	}
}