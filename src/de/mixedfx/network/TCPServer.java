package de.mixedfx.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class TCPServer
{
	public ListProperty<Connection>	connectionList;

	private Registrar				registrar;

	public void start() throws IOException
	{
		this.registrar = new Registrar();
		this.connectionList = this.registrar.connectionList;
		final Thread registrarThread = new Thread(this.registrar);
		registrarThread.setDaemon(true);
		registrarThread.start();
	}

	/**
	 * Stops the Registrar and all bound connections.
	 */
	public void stop()
	{
		this.registrar.terminate();
	}

	/**
	 * Starts listening on the registered port to establish new connections!
	 */
	private class Registrar implements Runnable
	{
		public ListProperty<Connection>	connectionList;

		/**
		 * Is needed to wait for connection. If someone tries to connect to serverSocket,
		 * serverSocket returns a normal Socket.
		 */
		private final ServerSocket		serverSocket;

		/**
		 * @throws IOException
		 *             Throws an Exception if e. g. port is not available
		 */
		public Registrar() throws IOException
		{
			this.serverSocket = new ServerSocket(Overall.PORT);
			this.connectionList = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
			System.out.println(this.getClass().getSimpleName() + " initialized on " + this.serverSocket.getLocalSocketAddress());
		}

		@Override
		public void run()
		{
			while (true)
				try
			{
					final Socket clientSocket = this.serverSocket.accept();
					this.connectionList.add(new Connection(TCPCoordinator.localNetworkID.getAndIncrement(), clientSocket));
					System.out.println("Registrar registered client!");
			}
			catch (final IOException e)
				{
				// In case of termination or connection failure => nothing to do!
			}
		}

		public void terminate()
		{
			try
			{
				this.serverSocket.close();
			}
			catch (final IOException e)
			{
				// In case of termination => nothing to do!
			}

			for (final Connection c : this.connectionList)
				c.close();
		}
	}
}
