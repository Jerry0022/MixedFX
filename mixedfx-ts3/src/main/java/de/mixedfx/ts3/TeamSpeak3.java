package de.mixedfx.ts3;

import de.mixedfx.inspector.Inspector;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Properties;

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
        client.readUntil("schandlerid=1");

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
        AutomatedTelnetClient client = new AutomatedTelnetClient(server, port);
        String response = client.sendCommand("currentschandlerid");

        Properties prop = new Properties();
        prop.load(new StringReader(response));
        int schandlerID = Integer.valueOf(prop.getProperty("schandlerid"));

        client.write("clientnotifyregister schandlerid=" + schandlerID + " event=any");
        String event;
        while (true) {
            event = client.readUntil("\n");
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
        String participantsUnformatted = client.sendCommand("clientlist");
        String[] participants = participantsUnformatted.split("\\|");
        for (String participant : participants)
            result.add(new TS3User(participant));
        return result;
    }

    public void disconnect() {
        client.disconnect();
    }

    public String getHostaddress() {
        return hostaddress;
    }
}
