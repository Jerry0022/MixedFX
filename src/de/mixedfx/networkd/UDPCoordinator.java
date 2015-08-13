package de.mixedfx.networkd;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.bushe.swing.event.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.java.ApacheTools;
import de.mixedfx.logging.Log;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.GoodByeMessage;
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
		UDPCoordinator.allAdresses.clear();

		try
		{
			this.in.start();
			this.out.start();
		}
		catch (Exception e)
		{
			service.publishSync(UDPCoordinator.ERROR, e);
		}
	}

	public synchronized void stopUDPFull()
	{
		if (this.out != null)
		{
			this.out.close();
		}
		if (this.in != null)
		{
			this.in.close();
		}
	}

	private boolean oldMessage = false;

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void onEvent(final String topic, final Object data)
	{
		synchronized (NetworkConfig.STATUS)
		{
			if (topic.equals(UDPCoordinator.RECEIVE))
			{
				final DatagramPacket packet = (DatagramPacket) data;

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
				if (ownOne)
					return;

				/*
				 * Read packet!
				 */
				ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
				try
				{
					ObjectInputStream is = new ObjectInputStream(in);
					UDPDetected newDetected = (UDPDetected) is.readObject();
					newDetected.address = packet.getAddress();
					/*
					 * Register / Update the client in local list of UDP contacts.
					 */

					// Add all sending NICs to list
					final Predicate predicate = ApacheTools.convert(UDPDetected.getByAddress(newDetected.address));
					if (CollectionUtils.exists(UDPCoordinator.allAdresses, predicate))
					{
						CollectionUtils.select(UDPCoordinator.allAdresses, predicate).forEach(t ->
						{
							final UDPDetected localDetected = (UDPDetected) t;
							if (newDetected.getTimeStamp().after(localDetected.getTimeStamp()))
							{
								// New UDP message of known NIC
								// Register change of state for this participant
								localDetected.update(newDetected.getTimeStamp(), newDetected.getStatus());
								UDPCoordinator.allAdresses.set(UDPCoordinator.allAdresses.indexOf(localDetected), localDetected);
								Log.network.trace("Updated " + newDetected);
							}
							else
							{
								oldMessage = true;
							}
						});
					}
					else
					{
						// New UDP message of unknown NIC
						UDPCoordinator.allAdresses.add(newDetected);
						Log.network.debug("New " + newDetected);
					}

					if (oldMessage)
					{
						Log.network.trace("Old UDP message received!");
						oldMessage = false;
						return; // Old UDP Packet, newer one was already received!
					}

					// Is remote online? If not, no action!
					if (newDetected.getStatus().equals(NetworkConfig.States.UNBOUND))
						return;

					if (NetworkConfig.STATUS.get().equals(States.UNBOUND))
					{
						// If I'm searching and the other one is a server or bound to server then let's connect
						Log.network.trace("UDP found another one to which I can connect!");
						NetworkManager.t.startFullTCP(newDetected.address);
					}
					else
					{
						// Register change in NIC for this participant if in same network!
						boolean notNull = NetworkConfig.networkExistsSince.get() != null && newDetected.getNetworkSince() != null;
						if (notNull && NetworkConfig.networkExistsSince.get().equals(newDetected.getNetworkSince()))
							updatePIDNetworks(newDetected.getPid(), newDetected.address);

						// Is the other one maybe in an older network? Then reconnect!
						if (newDetected.getNetworkSince() != null && newDetected.getNetworkSince().before(NetworkConfig.networkExistsSince.get()))
						{
							// Force reconnect
							Log.network.info("Older server detected on " + newDetected.address.getHostAddress() + " => Force reconnect to this server!");
							EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, new GoodByeMessage());
							Inspector.runNowAsDaemon(() ->
							{
								// Shutdown every participant in this network and force reconnect immediately
								ConnectivityManager.force();
							});
						}
					}
				}
				catch (Exception e)
				{
					UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
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

	// TODO! Shift to somewhere else!
	/**
	 * 
	 * 
	 * @param gotPID
	 * @param address
	 */
	private void updatePIDNetworks(int gotPID, InetAddress address)
	{
		// Return if I'm the host
		Log.network.trace("Got udp message with PID: " + gotPID);
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
			Log.network.debug("User with pid " + gotPID + " not found! See also: " + UserManager.allUsers);
			return;
		}

		// Return if user is not yet identified
		if (!UserManager.isIdentified(foundUser))
			return;

		// Remove old networks
		for (InetAddress inetAddress : foundUser.networks.keySet())
		{
			long lastUpdate = foundUser.networks.get(inetAddress);
			if ((new Date().getTime() - lastUpdate) > NetworkConfig.UDP_BROADCAST_INTERVAL * NetworkConfig.RECONNECT_TOLERANCE)
				foundUser.networks.remove(inetAddress);
		}

		// Update current or add it to list
		foundUser.networks.put(address, new Date().getTime());

		Log.network.debug("Updated networks " + foundUser.networks + " of user " + foundUser);
	}
}
