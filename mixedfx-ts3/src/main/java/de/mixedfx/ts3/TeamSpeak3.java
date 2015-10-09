package de.mixedfx.ts3;

import de.mixedfx.inspector.Inspector;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TeamSpeak3 {
    public interface Callback {
        void callback(String response);
    }

    private final static int PORT = 25639;
    private static TeamSpeak3 ourInstance;

    public static TeamSpeak3 getInstance(String hostaddress) throws IOException {
        if(ourInstance == null)
            ourInstance = new TeamSpeak3(hostaddress);
        else if(!ourInstance.getHostaddress().equalsIgnoreCase(hostaddress))
        {
            ourInstance.disconnect();
            ourInstance = new TeamSpeak3(hostaddress);
        }

        return ourInstance;
    }

    public static TeamSpeak3 getInstance() throws IOException {
        if(ourInstance == null)
            ourInstance = new TeamSpeak3("localhost");
        return ourInstance;
    }

    /*
     * OBJECT START
     */

    private String hostaddress;
    private WeakReference<Callback> eventListener;
    private AutomatedTelnetClient client;

    /**
     * @throws IOException Throws an exception if no instance of TeamSpeak 3 was found!
     */
    private TeamSpeak3(String hostaddress) throws IOException {
        this.hostaddress = hostaddress;
        this.eventListener = new WeakReference<>(null);

        client = new AutomatedTelnetClient(hostaddress, PORT);
        client.read();

        Inspector.runNowAsDaemon(()->{
            try {
                TeamSpeak3.this.startEventListener("localhost", PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @throws IOException Throws an exception if no instance of TeamSpeak 3 was found!
     */
    public void startEventListener(String server, int port) throws IOException {
        /*
        AutomatedTelnetClient client = new AutomatedTelnetClient(server, port);
        ComplexString response = client.sendCommand("currentschandlerid");

        Properties prop = new Properties();
        prop.load(new StringReader(response.toString()));
        int schandlerID = Integer.parseInt(prop.getProperty("schandlerid"));

        client.write("clientnotifyregister schandlerid=" + schandlerID + " event=any");
        String event;
        while (true) {
            event = client.readUntil("\n", "\n");
            System.out.println("TS3 Event" + event);
            if (event != null && this.eventListener != null && this.eventListener.get() != null)
                this.eventListener.get().callback(event);
            if (event == null)
                break;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("TeamSpeak was closed before this program ended :)");
        */
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

    /**
     *
     * @return Returns all currently online clients
     */
    public ArrayList<TS3User> getClients() {
        ArrayList<TS3User> result = new ArrayList<>();
        try {
            TS3Response participantsUnformatted = client.sendCommand("clientlist");
            if (!participantsUnformatted.isError()) {
                String[] participants = participantsUnformatted.getResponse().toString().split("\\|");
                for (String participant : participants)
                    result.add(new TS3User(participant));
            }
        } catch (IOException e) {
        }
        return result;
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (IOException ignored) {
        }
    }

    public String getHostaddress() {
        return hostaddress;
    }
}
