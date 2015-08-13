package de.mixedfx.network.rebuild;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.bushe.swing.event.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.java.ApacheTools;
import de.mixedfx.logging.Log;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class UDPCoordinator implements EventTopicSubscriber<Object>
{
	public static final String	RECEIVE	= "RECEIVE";
	public static final String	ERROR	= "ERROR";

	public static final EventBusService service = new EventBusService("UDPCoordinator");

	/**
	 * Just a list of all who made them known at least once (maybe aren't still active). An replaced event to the listener is only submitted if the state changed, not if the last contact was updated.
	 * This list isn't cleared as long as the udp connection is running!
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
		} catch (Exception e)
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

	private boolean oldMessage;

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void onEvent(final String topic, final Object data)
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
			} catch (final SocketException e)
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
							localDetected.update(newDetected.getTimeStamp());
							UDPCoordinator.allAdresses.set(UDPCoordinator.allAdresses.indexOf(localDetected), localDetected);
							Log.network.trace("Updated " + newDetected);
						} else
						{
							oldMessage = true;
						}
					});
				} else
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

				/*
				 * Connect to other one!
				 */
				NetworkManager.t.startFullTCP(newDetected.address);
			} catch (Exception e)
			{
				UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
			}
		} else if (topic.equals(UDPCoordinator.ERROR))
		{
			NetworkManager.t.stopTCPFull();
			this.stopUDPFull();

			EventBusExtended.publishAsyncSafe(NetworkManager.NETWORK_FATALERROR, data);
		}
	}
}
