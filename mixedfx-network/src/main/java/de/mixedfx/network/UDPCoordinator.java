package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.inspector.Inspector;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.bushe.swing.event.EventTopicSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

@Component
@Log4j2(topic = "Network")
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

	@Autowired
	private NetworkManager networkManager;
	@Autowired
	private UDPIn in;
	@Autowired
	private UDPOut out;

    public UDPCoordinator() {
		/*
		 * Get events of UDPOut and UDPIn
		 */
		UDPCoordinator.service.subscribe(UDPCoordinator.ERROR, this);
		UDPCoordinator.service.subscribe(UDPCoordinator.RECEIVE, this);
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

	private final List<InetAddress> cached = new ArrayList<>();

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
					for (final InetAddress inetAddress : Collections.list(nic.getInetAddresses())) {
						if (inetAddress.getHostAddress().equals(packet.getAddress().getHostAddress())) {
							ownOne = true;
						}
					}
				}
			} catch (final SocketException ignored) {
			}
			if (ownOne)
				return;

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
				if (CollectionUtils.exists(UDPCoordinator.allAdresses, udp -> UDPDetected.getByAddress(newDetected.address).test((UDPDetected) udp))) {
					// UDP Sender is already registered
					CollectionUtils
							.select(UDPCoordinator.allAdresses, udp -> UDPDetected.getByAddress(newDetected.address).test((UDPDetected) udp))
							.forEach(udp -> {
								final UDPDetected localDetected = (UDPDetected) udp;
								if (newDetected.getTimeStamp().after(localDetected.getTimeStamp())) {
									log.trace("New UDP message received!");
									// New UDP message of known NIC
									// Register change of state for this participant
									localDetected.update(newDetected.getTimeStamp());
									// Fire replace event for listeners
									UDPCoordinator.allAdresses.set(UDPCoordinator.allAdresses.indexOf(localDetected), localDetected);
									log.trace("Updated " + newDetected);
								} else {
									return; // Old UDP Packet, newer one was already received!
								}
							});
				} else {
					// New UDP message of unknown NIC
					UDPCoordinator.allAdresses.add(newDetected);
					log.debug("New " + newDetected);
				}

				/*
				 * Connect to other one!
				 */
				// Check cache
				log.trace("Cached requests: " + this.cached);
				synchronized (this.cached) {
					if (this.cached.contains(newDetected.address))
						return;
				}
				this.cached.add(newDetected.address);

				// Connect
				final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, Inspector.getThreadFactory());
				final Future handler = executor.submit(() -> {
					networkManager.t.startFullTCP(newDetected.address);
					synchronized (UDPCoordinator.this.cached) {
						UDPCoordinator.this.cached.remove(newDetected.address);
					}
					return null;
				});
				executor.schedule(() -> {
					if (!handler.isDone()) {
						log.warn("A TCP connection needed to much time to establish! Time waited: "
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
				}, NetworkConfig.TCP_CONNECTION_ESTABLISHING_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (final Exception e) {
				UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
			}
		} else if (topic.equals(UDPCoordinator.ERROR)) {
			networkManager.t.stopTCPFull();
			this.stopUDPFull();
			EventBusExtended.publishSyncSafe(NetworkManager.NETWORK_FATALERROR, data);
		}
	}
}
