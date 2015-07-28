package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.bushe.swing.event.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.java.ApacheTools;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.user.User;
import de.mixedfx.network.user.UserManager;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class UDPCoordinator implements EventTopicSubscriber<Object>
{
	public static final String	RECEIVE	= "RECEIVE";
	public static final String	ERROR	= "ERROR";

	public static final EventBusService service = new EventBusService("UDPCoordinator");

	/**
	 * Just a list of all who made them known at least once (maybe aren't still active). An replaced event to the listener is only submitted if the
	 * state changed, not if the last contact was updated. This list isn't cleared as long as the udp connection is running!
	 */
	public static ListProperty<UDPDetected> allAdresses;

	static
	{
		UDPCoordinator.allAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
	}

	private boolean running;

	private final UDPIn		in;
	private final UDPOut	out;

	public UDPCoordinator()
	{
		/*
		 * Get events of UDPOut and UDPIn
		 */
		UDPCoordinator.service.subscribe(UDPCoordinator.ERROR, this);
		UDPCoordinator.service.subscribe(UDPCoordinator.RECEIVE, this);

		/*
		 * Start UDP Server and if started successfully start UDPClient
		 */
		this.in = new UDPIn();
		this.out = new UDPOut();
	}

	public synchronized void startUDPFull()
	{
		this.running = true;

		NetworkConfig.statusChangeTime.set(new Date());

		this.in.start();
		if (this.running)
		{
			this.out.start();
		}
	}

	public synchronized void stopUDPFull()
	{
		this.running = false;

		if (this.out != null)
		{
			this.out.close();
		}
		if (this.in != null)
		{
			this.in.close();
		}

		UDPCoordinator.allAdresses.clear();

		NetworkConfig.statusChangeTime.set(new Date());
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void onEvent(final String topic, final Object data)
	{
		synchronized (NetworkConfig.status)
		{
			if (topic.equals(UDPCoordinator.RECEIVE))
			{
				final DatagramPacket packet = (DatagramPacket) data;
				final String packetMessage = new String(packet.getData(), 0, packet.getLength());

				/*
				 * Check all interfaces if it was a broadcast to myself!
				 */
				boolean ownOne = false;
				try
				{
					final Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
					while (nics.hasMoreElements())
					{
						final NetworkInterface nic = nics.nextElement();
						for (final InetAddress nicAdress : Collections.list(nic.getInetAddresses()))
						{
							if (nicAdress.getHostAddress().equals(packet.getAddress().getHostAddress()))
							{
								ownOne = true;
							}
						}
					}
				}
				catch (final SocketException e)
				{
				}

				if (!ownOne)
				{
					/*
					 * Register / Update the client in local list of UDP contacts.
					 */
					final UDPDetected newDetected = new UDPDetected(packet.getAddress(), Date.from(Instant.parse(packetMessage.split("\\!")[0])),
							NetworkConfig.States.valueOf(packetMessage.split("\\!")[1]), Date.from(Instant.parse(packetMessage.split("\\!")[2])), Integer.valueOf(packetMessage.split("\\!")[3]));

					// Add all sending NICs to list
					final Predicate predicate = ApacheTools.convert(UDPDetected.getByAddress(newDetected.address));
					if (CollectionUtils.exists(UDPCoordinator.allAdresses, predicate))
					{
						CollectionUtils.select(UDPCoordinator.allAdresses, predicate).forEach(t ->
						{
							final UDPDetected localDetected = (UDPDetected) t;
							if (newDetected.timeStamp.after(localDetected.timeStamp))
							{
								// New UDP message of known NIC
								// Register change of stae for this participant
								final States oldStatus = States.valueOf(localDetected.status.toString());
								localDetected.update(newDetected.status, newDetected.timeStamp);
								if (!oldStatus.equals(newDetected.status))
								{
									UDPCoordinator.allAdresses.set(UDPCoordinator.allAdresses.indexOf(localDetected), localDetected);
								}
								// Register change in NIC for this participant
								updatePIDNetworks(newDetected.pid, newDetected.address);
							}
							else
							{
								return; // Old UDP Packet, newer one was already received!
							}
						});
					}
					else
					{
						// New UDP message of unknown NIC
						UDPCoordinator.allAdresses.add(newDetected);
						updatePIDNetworks(newDetected.pid, newDetected.address);
					}

					/*
					 * If I'm searching and the other one is a server or bound to server then let's connect
					 */
					if (NetworkConfig.status.get().equals(States.Unbound) && (newDetected.status.equals(NetworkConfig.States.Server) || newDetected.status.equals(NetworkConfig.States.BoundToServer)))
					{
						NetworkManager.t.startFullTCP(packet.getAddress());
					}
				}
			}
			else if (topic.equals(UDPCoordinator.ERROR))
			{
				NetworkManager.t.stopTCPFull();
				this.stopUDPFull();

				EventBusExtended.publishAsyncSafe(NetworkManager.NETWORK_FATALERROR, data);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param gotPID
	 * @param address
	 */
	private void updatePIDNetworks(int gotPID, InetAddress address)
	{
		// Return if I'm the host
		if (gotPID == ParticipantManager.UNREGISTERED)
			return;

		User anonymousUser = new User()
		{
			{
				this.pid = gotPID;
			}

			@Override
			public Object getIdentifier()
			{
				return null;
			}

			@Override
			public boolean equals(final User user)
			{
				return user.getIdentifier().equals(this.getIdentifier());
			}
		};

		User foundUser = null;
		try
		{
			foundUser = (User) CollectionUtils.select(UserManager.allUsers, anonymousUser.getByPID()).iterator().next();
		}
		catch (NoSuchElementException e)
		{
			return;
		}

		// Remove old networks
		for (InetAddress inetAddress : foundUser.networks.keySet())
		{
			long lastUpdate = foundUser.networks.get(inetAddress);
			if (TimeUnit.MILLISECONDS.convert((new Date()).getTime() - lastUpdate, TimeUnit.SECONDS) > NetworkConfig.BROADCAST_INTERVAL * NetworkConfig.RECONNECT_TOLERANCE)
				foundUser.networks.remove(inetAddress);
		}

		// Update current or add it to list
		foundUser.networks.put(address, new Date().getTime());
	}
}
