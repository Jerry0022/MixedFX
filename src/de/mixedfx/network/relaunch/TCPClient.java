package de.mixedfx.network.relaunch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

class TCPClient
{
	public Connection	connection;

	public void start(final InetAddress ip) throws IOException
	{
		IOException exception = null;
		Socket socket = null;
		for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
		{
			try
			{
				socket = new Socket(ip, NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS);
				this.connection = new Connection(TCPCoordinator.localNetworkMainID.get(), socket);
				break;
			}
			catch (final SocketException | UnknownHostException e)
			{
				exception = e;
			}
		}
		if (socket == null)
		{
			throw exception;
		}
	}

	public void stop()
	{
		if (this.connection != null)
		{
			this.connection.close();
		}
	}
}