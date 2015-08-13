package de.mixedfx.network.rebuild;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

class TCPClient
{
	public InetAddress	remoteAddress;
	private Connection	connection;

	public TCPClient start(final InetAddress ip) throws IOException
	{
		IOException exception = null;
		Socket socket = null;
		for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
		{
			try
			{
				socket = new Socket(ip, NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS);
				start(socket);
				break;
			} catch (final SocketException | UnknownHostException e)
			{
				exception = e;
			}
		}
		if (socket == null)
		{
			throw exception;
		}
		return this;
	}

	public TCPClient start(final Socket socket) throws IOException
	{
		this.remoteAddress = socket.getInetAddress();
		this.connection = new Connection(socket);
		return this;
	}

	public Connection stop()
	{
		if (this.connection != null)
		{
			this.connection.close();
		}
		return this.connection;
	}
}