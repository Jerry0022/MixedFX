package de.mixedfx.network;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

@Component
@Log4j2(topic = "Network")
class TCPServer
{
	@Autowired
	NetworkManager networkManager;

	/**
	 * Is set after start was called.
	 */
	public ListProperty<TCPClient> connectionList;
	private Registrar registrar;

	public void start() throws IOException
	{
		IOException exception = null;
		for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
		{
			try
			{
				this.registrar = new Registrar(NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS);
				this.connectionList = this.registrar.connectionList;
				final Thread registrarThread = new Thread(this.registrar);
				registrarThread.setDaemon(true);
				registrarThread.start();
				break;
			} catch (final SocketException | UnknownHostException e)
			{
				exception = e;
			}
		}
		if (this.registrar == null)
			throw exception;
	}

	/**
	 * Stops the {@link Registrar} and all bound connections.
	 */
	public void stop()
	{
		if (this.registrar != null)
		{
			this.registrar.terminate();
			this.registrar = null;
		}
	}

	/**
	 * Starts listening on the registered port to establish new connections!
	 */
	private class Registrar implements Runnable
	{
		public volatile ListProperty<TCPClient> connectionList;

		/**
		 * Is needed to wait for connection. If someone tries to connect to serverSocket, serverSocket returns a normal Socket.
		 */
		private final ServerSocket serverSocket;

		/**
		 * @throws IOException
		 *             Throws an Exception if e. g. port is not available
		 */
		public Registrar(final int port) throws IOException
		{
			this.serverSocket = new ServerSocket(port);
			this.connectionList = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
			log.debug(this.getClass().getSimpleName() + " initialized on " + this.serverSocket.getLocalSocketAddress());
		}

		@Override
		public void run()
		{
			boolean running = true;
			while (running)
			{
				try
				{
					final Socket clientSocket = this.serverSocket.accept();
					synchronized (networkManager.t.tcpClients)
					{
						this.connectionList.add(new TCPClient().start(clientSocket));
					}
					log.debug("TCP Registrar successfully registered client!");
				} catch (final IOException e)
				{
					// In case of termination or connection failure => nothing to do!
					log.debug(this.getClass().getSimpleName() + " closed!");
					running = false;
				}
			}
		}

		public void terminate()
		{
			try
			{
				this.serverSocket.close();
			} catch (final IOException e)
			{
				// In case of termination => nothing to do!
			}

			synchronized (networkManager.t.tcpClients)
			{
				this.connectionList.forEach(de.mixedfx.network.TCPClient::stop);
				this.connectionList.clear();
			}
		}
	}
}
