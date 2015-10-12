package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.eventbus.EventBusServiceInterface;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.network.messages.GoodByeMessage;
import de.mixedfx.network.messages.Message;
import lombok.extern.log4j.Log4j2;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@Log4j2
public class Connection implements EventBusServiceInterface {
    protected static final String MESSAGE_CHANNEL_SEND = "MESSAGE_CHANNEL_SEND";
    protected static final String MESSAGE_CHANNEL_RECEIVED = "MESSAGE_CHANNEL_RECEIVED";
    protected static final String CONNECTION_CHANNEL_LOST = "CONNECTION_CHANNEL_LOST";

    public final InetAddress ip;

    private final Socket clientSocket;
    private final ConnectionOutput outputConnection;
    private final ConnectionInput inputConnection;

    private EventBusService eventBus;

    public Connection(final Socket clientSocket) throws IOException {
        log.debug("Initializing " + this.getClass().getSimpleName() + " with " + clientSocket.getInetAddress());

        this.ip = clientSocket.getInetAddress();

        this.clientSocket = clientSocket;
        this.clientSocket.setKeepAlive(true);
        this.clientSocket.setSoLinger(true, 0);
        this.clientSocket.setTcpNoDelay(true);
        /*
         * TODO Sometimes the outputstream is not flushing and therefore the inputstream constructor is blocking! Use this.clientSocket.setSO_Linger() or .setNoTCPDelay to fix this!
		 */

        this.initilizeEventBusAndSubscriptions();

        this.outputConnection = new ConnectionOutput(clientSocket.getOutputStream(), ip);
        Inspector.runNowAsDaemon(this.outputConnection);

        this.inputConnection = new ConnectionInput(clientSocket.getInputStream(), ip);
        Inspector.runNowAsDaemon(this.inputConnection);

        log.debug(this.getClass().getSimpleName() + " initialized!");
    }

    @Override
    public void initilizeEventBusAndSubscriptions() {
        this.eventBus = new EventBusService(this.getClass() + this.clientSocket.getRemoteSocketAddress().toString().split(":")[0]);
        this.eventBus.subscribe(MESSAGE_CHANNEL_RECEIVED, this);
        this.eventBus.subscribe(CONNECTION_CHANNEL_LOST, this);
        AnnotationProcessor.process(this);
    }

    @Override
    @EventTopicSubscriber(topic = MESSAGE_CHANNEL_SEND)
    public synchronized void onEvent(final String topic, final Object event) {
        switch (topic) {
            case MESSAGE_CHANNEL_SEND: {
                final Message message = (Message) event;
                if (message.getToIP() == null || message.getToIP().equals(this.ip)) {
                    message.setToIP(this.ip);
                    this.outputConnection.sendMessage(message);
                }
                break;
            }
            case MESSAGE_CHANNEL_RECEIVED: {
                final Message message = (Message) this.inputConnection.getNextMessage();
                message.setFromIP(this.ip);
                if (message instanceof GoodByeMessage) {
                    log.debug("Got GoodByeMessage!");
                    this.close();
                    EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this);
                } else
                    EventBusExtended.publishAsyncSafe(MessageBus.MESSAGE_RECEIVE, message);
                break;
            }
            default:
                this.close();
                EventBusExtended.publishSyncSafe(TCPCoordinator.CONNECTION_LOST, this);
                break;
        }
    }

    public synchronized void close() {
        log.debug("Closing " + this.getClass().getSimpleName());

        AnnotationProcessor.unprocess(this);
        this.eventBus.unsubscribe(CONNECTION_CHANNEL_LOST, this);
        this.eventBus.unsubscribe(MESSAGE_CHANNEL_RECEIVED, this);

        try {
            while (!this.outputConnection.outputMessageCache.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
            // Terminate first to processing last remaining steps.
            this.outputConnection.terminate();
            this.inputConnection.terminate();
            // Close socket to be sure that everything was closed.
            this.clientSocket.close();
        } catch (final IOException ignored) {
        }

        log.debug(this.getClass().getSimpleName() + " closed!");
    }
}
