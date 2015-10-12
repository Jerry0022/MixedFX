package de.mixedfx.network;

import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import de.mixedfx.network.messages.UserMessage;
import de.mixedfx.network.overlay.MasterNetworkHandler;
import de.mixedfx.network.overlay.OverlayNetwork;
import de.mixedfx.network.user.User;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;

@Component
@Configuration
public class ConnectivityManager<T extends User> {
    @Bean
    @Qualifier(value = "Network")
    public Logger produceLogger() {
        return Log.CONTEXT.getLogger("Network");
    }

    @Autowired
    @Qualifier(value = "Network")
    Logger LOGGER;

    @Autowired
    private MessageBus bus;

    @Autowired
    private NetworkManager networkManager;

    private ObjectProperty<State> state;

    private
    @Getter
    Hashtable<InetAddress, UserMessage<T>> tcp_user_map;

    private final ListProperty<T> otherUsers;

    private
    @Getter
    @Setter
    T myUniqueUser;

    public ConnectivityManager() {
        this.otherUsers = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.state = new SimpleObjectProperty<>(State.OFFLINE);
        this.tcp_user_map = new Hashtable<>(16);
    }

    @PostConstruct
    public void init() {
        bus.setConnectivityManager(this);

        /*
         * Update otherUsers list.
         */
        this.bus.registerForReceival(message -> {
            // Just to let the above veto listener work!
        }, true);
        // Attention: Does only subscribe if at least one normal other
        // subscriber to!
        // New or updated user!
        EventBus.subscribeVetoListenerStrongly(MessageBus.MESSAGE_RECEIVE,
                (topic, message) -> {
                    if (message instanceof UserMessage) {
                        LOGGER.debug("UserMessage received: " + message);
                        final UserMessage<T> userMessage = (UserMessage<T>) message;
                        // Only if message is not from me
                        if (!userMessage.getOriginalUser().equals(this.myUniqueUser)) {
                            synchronized (networkManager.t.tcpClients) {
                                // If first other user set state online
                                if (this.tcp_user_map.keySet().isEmpty()) {
                                    this.state.set(State.ONLINE);
                                }
                                // Update mapping anyway
                                this.tcp_user_map.put(userMessage.getFromIP(), userMessage);
                                LOGGER.debug("Put UserMessage " + userMessage + " from ip "
                                        + userMessage.getFromIP() + " to my list!");
                            }
                            synchronized (this.otherUsers) {
                                // Update other users list of networks
                                final T newUser = userMessage.getOriginalUser();
                                newUser.networks.add(MasterNetworkHandler.get(userMessage.getFromIP()));
                                // Add / Replace the user of whom the message came from
                                if (this.otherUsers.contains(newUser)) {
                                    this.otherUsers.get(this.otherUsers.indexOf(newUser)).merge(newUser);
                                } else {
                                    this.otherUsers.add(newUser);
                                }
                            }
                        } else {
                            LOGGER.warn("UserMessage was from me!");
                        }
                        return true;
                    } else {
                        return false;
                    }
                });
        // Old user who disconnected
        networkManager.t.tcpClients.addListener(this::tcpClientsChanged);
    }

    /**
     * On user disconnected update all lists.
     * On new user send new users a welcome message.
     *
     * @param c TCPClient change
     */
    private void tcpClientsChanged(ListChangeListener.Change<? extends TCPClient> c) {
        while (c.next()) {
            synchronized (networkManager.t.tcpClients) {
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(tcpClient -> {
                        if (this.tcp_user_map.containsKey(tcpClient.remoteAddress)) {
                            // Remove from tcp user map
                            final T oldUser = this.tcp_user_map.get(tcpClient.remoteAddress).getOriginalUser();
                            this.tcp_user_map.remove(tcpClient.remoteAddress);

                            // Remove network connection from user in otherUsers list
                            synchronized (this.otherUsers) {
                                User toUpdateUser = this.otherUsers.get(this.otherUsers.indexOf(oldUser));
                                new ArrayList<>(toUpdateUser.networks).stream().
                                        filter(overlayNetwork -> overlayNetwork.getIP().equals(tcpClient.remoteAddress)).
                                        forEach(toUpdateUser.networks::remove);
                                LOGGER.debug("Removed user message from list: " + tcpClient.remoteAddress);
                            }

                            // Remove from otherUsers list
                            synchronized (this.otherUsers) {
                                if (!this.tcp_user_map.containsValue(new UserMessage<>(oldUser))) {
                                    this.otherUsers.remove(oldUser);
                                } else {
                                    LOGGER.info("User is still available over other connection! ");
                                }
                            }
                            if (this.tcp_user_map.keySet().isEmpty()) {
                                this.state.set(State.SEARCHING);
                            }
                        }
                    });
                } else if (c.wasAdded()) {
                    // If new tcp connection, send other one my profile
                    c.getAddedSubList().forEach(tcpClient -> {
                        synchronized (networkManager.t.tcpClients) {
                            final UserMessage<T> message = new UserMessage<>(this.myUniqueUser);
                            message.setToIP(tcpClient.remoteAddress);
                            this.bus.send(message);
                            LOGGER.debug("Sent " + message + " to " + tcpClient.remoteAddress);
                        }
                    });
                }
            }
        }
    }

    /*
     * NETWORK STATE CONTROLLER
     */

    public void restart() {
        this.stop();
        this.start();
    }

    public void restart(@NonNull T user) {
        this.stop();
        this.start(user);
    }

    public void start() {
        if (this.myUniqueUser == null) {
            throw new IllegalStateException("Can't start network without identifying myself. Please set a user first!");
        }

        // Start network
        networkManager.start();
        this.state.set(State.SEARCHING);

        // Start updating overlay networks
        Inspector.runNowAsDaemon(() -> {
            while (networkManager.running) {
                try {
                    Thread.sleep(NetworkConfig.ICMP_INTERVAL);
                } catch (final InterruptedException ignored) {
                }

                synchronized (this.otherUsers) {
                    this.otherUsers.forEach(user -> user.networks.forEach(OverlayNetwork::updateLatency));
                }
            }
        });
    }

    public void start(@NonNull T myUniqueUser) {
        if ((this.myUniqueUser == null)) {
            this.setMyUniqueUser(myUniqueUser);
        } else {
            this.myUniqueUser.merge(myUniqueUser);
        }
        this.start();
    }

    public void stop() {
        networkManager.stop();
        this.state.set(State.OFFLINE);
    }

    public void switchStatus() {
        if (this.state.get().equals(State.OFFLINE)) {
            this.start();
        } else {
            this.stop();
        }
    }

    /**
     * Inform others about an updated user.
     */
    public void updatedUser() {
        // TODO Test this
        final UserMessage<T> message = new UserMessage<>(this.myUniqueUser);
        this.bus.send(message);
    }

    /*
     * PROPERTIES
     */

    public ObjectProperty<State> state() {
        return this.state;
    }

    /**
     * Should be used only by using synchronized on this object!
     */
    public ListProperty<T> otherUsers() {
        return this.otherUsers;
    }
}
