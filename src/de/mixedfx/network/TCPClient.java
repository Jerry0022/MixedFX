package de.mixedfx.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient
{
	public Connection	connection;

	public void start(final InetAddress ip, final int port) throws IOException
	{
		final Socket socket = new Socket(ip, port);
		this.connection = new Connection(0, socket);
	}

	public void stop()
	{
		this.connection.close();
	}
}