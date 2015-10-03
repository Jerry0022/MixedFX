package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.logging.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class ConnectionOutput implements Runnable {
    private static final Class<?> parentClass = Connection.class;

    private volatile boolean running = true;

    private final ObjectOutputStream objectOutputStream;
    public final ArrayList<Serializable> outputMessageCache;
    private final EventBusService eventBusParent;

    protected ConnectionOutput(final OutputStream outputStream, InetAddress ip) throws IOException {
        this.eventBusParent = EventBusService.getEventBus(ConnectionOutput.parentClass + ip.toString());
        this.objectOutputStream = new ObjectOutputStream(outputStream);
        this.outputMessageCache = new ArrayList<>();

        Log.network.trace(this.getClass().getSimpleName() + " initialized!");
    }

    protected void sendMessage(final Serializable message) {
        synchronized (this.outputMessageCache) {
            Log.network.trace(this.getClass().getSimpleName() + " sends a Message!");
            this.outputMessageCache.add(message);
        }
    }

    @Override
    public void run() {
        while (this.running) {
            synchronized (this.outputMessageCache) {
                if (this.outputMessageCache.size() > 0) {
                    try {
                        this.objectOutputStream.reset(); // GBC can collect written objects
                        this.objectOutputStream.writeObject(this.outputMessageCache.get(0));
                        Log.network.trace(this.getClass().getSimpleName() + " message successfully sent!");
                        this.outputMessageCache.remove(0);
                    } catch (final IOException e) {
                        this.outputMessageCache.clear();
                        if (this.terminate()) {
                            Log.network.debug("OutputStream lost!");
                            this.eventBusParent.publishSync(Connection.CONNECTION_CHANNEL_LOST, this);
                        }
                    }
                }
            }
            // Only sends in xx milliseconds interval
            try {
                Thread.sleep(NetworkConfig.TCP_UNICAST_INTERVAL);
            } catch (final InterruptedException e) {
                Log.network.fatal("TCP unicast interval could not be applied!");
            }
        }
    }

    protected synchronized boolean terminate() {
        if (this.running) {
            while (!this.outputMessageCache.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (final InterruptedException ignored) {
                }
            }

            Log.network.trace("Terminating " + this.getClass().getSimpleName() + "!");
            this.running = false;

            try {
                this.objectOutputStream.close();
            } catch (final IOException e) {
                // In rare cases could be called twice, therefore soft
                // Exception is needed, no impact!
                Log.network.trace("WRITE ERROR! No reason to fear about this :D!");
            }
            Log.network.trace(this.getClass().getSimpleName() + " terminated!");
            return true;
        } else {
            return false;
        }
    }
}
