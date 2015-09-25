package de.mixedfx.ts3;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Jerry on 23.09.2015.
 */
public class TS3Tester {
    private static TS3Tester ourInstance;
    private AutomatedTelnetClient client;
    private WeakReference<Callback> eventListener;

    /**
     * @throws IOException Throws an exception if no instance of TeamSpeak 3 was found!
     */
    private TS3Tester() throws IOException {
        this.eventListener = new WeakReference<>(null);
        client = new AutomatedTelnetClient("localhost", 25639);
        client.readUntil("schandlerid=1");
        Thread thread = new Thread(() ->
        {
            try {
                TS3Tester.this.startEventListener("localhost", 25639);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static TS3Tester getInstance() throws IOException {
        ourInstance = new TS3Tester();
        return ourInstance;
    }

    public static void main(String[] args) {
        try {
            getInstance(); // .getClients().stream().filter(user -> user.isNormalUser()).forEach(client -> System.out.println(client.getName()))
            getInstance().registerEventListener(string -> System.err.println("Event: " + string));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws IOException Throws an exception if no instance of TeamSpeak 3 was found!
     */
    public void startEventListener(String server, int port) throws IOException {
        AutomatedTelnetClient client = new AutomatedTelnetClient(server, port);
        String response = client.sendCommand("currentschandlerid");

        int schandlerID = Integer.valueOf(response.split("\\n")[2].split("=")[1]);
        client.write("clientnotifyregister schandlerid=" + schandlerID + " event=any");
        String event;
        while (true) {
            event = client.readUntil("\n");
            if (event != null && this.eventListener != null && this.eventListener.get() != null)
                this.eventListener.get().callback(event);
            if (event == null || event.startsWith("notifyconnectstatuschange schandlerid=" + schandlerID + " status=disconnected"))
                break;
        }
    }

    /**
     * Only one listener at a time can listen to this.
     *
     * @param callback The callback which is registered as WEAK listener.
     */
    public void registerEventListener(Callback callback) {
        this.eventListener = new WeakReference<>(callback);
    }

    public void unregisterEventListener(Callback callback) {
        this.eventListener = new WeakReference<>(null);
    }

    public ArrayList<TSUser> getClients() {
        ArrayList<TSUser> result = new ArrayList<>();
        String participantsUnformatted = client.sendCommand("clientlist");
        String[] participants = participantsUnformatted.split("\\|");
        for (String participant : participants)
            result.add(new TSUser(participant));
        return result;
    }

    public void disconnect() {
        client.disconnect();
    }

    public interface Callback {
        void callback(String response);
    }

    public class TSUser {
        private Properties property = new Properties();

        public TSUser(String original) {
            try {
                for (String prop : original.split("\\s+")) {
                    prop = prop.replace("\\s", " "); // Replace space markers
                    property.load(new StringReader(prop));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int getID() {
            return Integer.valueOf(property.getProperty("clid"));
        }

        public int getChannelID() {
            return Integer.valueOf(property.getProperty("cid"));
        }

        public int getDatabaseID() {
            return Integer.valueOf(property.getProperty("client_database_id"));
        }

        /**
         * @return Returns true if it is a usual user. False if it is a generated Server Client.
         */
        public boolean isNormalUser() {
            return Integer.valueOf(property.getProperty("client_type")) == 0;
        }

        public String getName() {
            return property.getProperty("client_nickname");
        }
    }
}
