package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.apache.commons.collections.CollectionUtils;
import org.bushe.swing.event.EventTopicSubscriber;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPCoordinator implements EventTopicSubscriber<Object> {
    public static final String RECEIVE = "RECEIVE";
    public static final String ERROR = "ERROR";

    public static final EventBusService service = new EventBusService("UDPCoordinator");

    /**
     * Just a list of all who made them known at least once (maybe aren't still
     * active). An replaced event to the listener is only submitted if the state
     * changed, not if the last contact was updated. This list isn't cleared as
     * long as the udp connection is running!
     */
    public static ListProperty<UDPDetected> allAdresses;

    static {
	UDPCoordinator.allAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
    }

    private final UDPIn in;
    private final UDPOut out;

    public UDPCoordinator() {
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

    public synchronized void startUDPFull() {
	UDPCoordinator.allAdresses.clear();

	try {
	    this.in.start();
	    this.out.start();
	} catch (final Exception e) {
	    UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
	}
    }

    public synchronized void stopUDPFull() {
	if (this.out != null) {
	    this.out.close();
	}
	if (this.in != null) {
	    this.in.close();
	}
    }

    private boolean oldMessage;
	private final List<InetAddress> cached = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void onEvent(final String topic, final Object data) {
	if (topic.equals(UDPCoordinator.RECEIVE)) {
	    final DatagramPacket packet = (DatagramPacket) data;

	    /*
	     * Check all interfaces if it was a broadcast to myself!
	     */
	    boolean ownOne = false;
	    try {
		final Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
		while (nics.hasMoreElements()) {
		    final NetworkInterface nic = nics.nextElement();
		    for (final InetAddress nicAdress : Collections.list(nic.getInetAddresses())) {
			if (nicAdress.getHostAddress().equals(packet.getAddress().getHostAddress())) {
			    ownOne = true;
			}
		    }
		}
		} catch (final SocketException ignored) {
		}
		if (ownOne) {
		return;
	    }

	    /*
	     * Read packet!
	     */
	    final ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
	    try {
		final ObjectInputStream is = new ObjectInputStream(in);
		final UDPDetected newDetected = (UDPDetected) is.readObject();
		newDetected.address = packet.getAddress();
		/*
		 * Register / Update the client in local list of UDP contacts.
		 */

		// Add all sending NICs to list
		// TODO Check if working
		if (CollectionUtils.exists(UDPCoordinator.allAdresses,
			(obj) -> UDPDetected.getByAddress(newDetected.address).test((UDPDetected) obj))) {
		    CollectionUtils
			    .select(UDPCoordinator.allAdresses,
				    (obj) -> UDPDetected.getByAddress(newDetected.address).test(newDetected))
			    .forEach(t -> {
				final UDPDetected localDetected = (UDPDetected) t;
				if (newDetected.getTimeStamp().after(localDetected.getTimeStamp())) {
				    // New UDP message of known NIC
				    // Register change of state for this
				    // participant
				    localDetected.update(newDetected.getTimeStamp());
				    UDPCoordinator.allAdresses.set(UDPCoordinator.allAdresses.indexOf(localDetected),
					    localDetected);
				    Log.network.trace("Updated " + newDetected);
				} else {
				    this.oldMessage = true;
				}
			    });
		} else {
		    // New UDP message of unknown NIC
		    UDPCoordinator.allAdresses.add(newDetected);
		    Log.network.debug("New " + newDetected);
		}

		if (this.oldMessage) {
		    this.oldMessage = false;
		    return; // Old UDP Packet, newer one was already received!
		}
		Log.network.trace("New UDP message received!");

		/*
		 * Connect to other one!
		 */
		Log.network.trace("Cached requests: " + this.cached);
		boolean alreadyWaiting;
		synchronized (this.cached) {
		    if (!this.cached.contains(newDetected.address)) {
			alreadyWaiting = false;
			this.cached.add(newDetected.address);
		    } else {
			alreadyWaiting = true;
		    }
		}
		if (!alreadyWaiting) {
		    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2,
			    Inspector.getThreadFactory());
		    final Future handler = executor.submit(() -> {
			NetworkManager.t.startFullTCP(newDetected.address);
			synchronized (UDPCoordinator.this.cached) {
			    UDPCoordinator.this.cached.remove(newDetected.address);
			}
			return null;
		    });
		    executor.schedule(() -> {
			if (!handler.isDone()) {
			    Log.network.warn("A TCP connection needed to much time to establish! Time waited: "
				    + NetworkConfig.TCP_CONNECTION_ESTABLISHING_TIMEOUT + " milliseconds."
				    + "Connection is now closed! " + newDetected.address);

			    handler.cancel(true);
				// Lock if callable still works...
				try {
				Thread.sleep(NetworkConfig.TCP_CONNECTION_ESTABLISHING_RETRY);
				} catch (final InterruptedException ignored) {
				}
				synchronized (UDPCoordinator.this.cached) {
				UDPCoordinator.this.cached.remove(newDetected.address);
			    }
			}
		    } , NetworkConfig.TCP_CONNECTION_ESTABLISHING_TIMEOUT, TimeUnit.MILLISECONDS);
		}
	    } catch (final Exception e) {
		UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
	    }
	} else if (topic.equals(UDPCoordinator.ERROR)) {
	    NetworkManager.t.stopTCPFull();
	    this.stopUDPFull();

	    EventBusExtended.publishSyncSafe(NetworkManager.NETWORK_FATALERROR, data);
	}
    }
}
