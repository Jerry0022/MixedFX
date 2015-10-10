package de.mixedfx.ts3.de.mixedfx.ts3.cdi;

import de.mixedfx.inspector.Inspector;
import de.mixedfx.java.ComplexString;
import de.mixedfx.windows.ahk.AHKManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Jerry on 09.10.2015.
 */
@Component
public class TS3LocalInstance extends TS3Instance {
    public static final String HOSTADDRESS = "localhost";
    public static final int PORT = 25639;

    @Autowired
    private TS3TelnetClient telnetClient;
    @Autowired
    private TS3TelnetClient telnetEventClient;

    private WeakReference<TS3EventCallback> callback;
    private volatile boolean ready;

    /**
     * Waits for TS3, starts connection and listens to events. If TS3 was closed afterwards it returns.
     * It is blocking and not looping. This method can be called in a loop.
     *
     * @return True if process was successful, false if TS3 client has no ClientQuery Plugin enabled.
     * @throws IOException Only thrown if ahk files can't be written to disk.
     */
    public boolean start() throws IOException {
        LOGGER.info("START");
        ComplexString basicLines = new ComplexString();
        basicLines.add("#NoTrayIcon");
        basicLines.add("DetectHiddenWindows, On");
        basicLines.add("GroupAdd, TS3, ahk_exe ts3client_win32.exe"); // 32 bit
        basicLines.add("GroupAdd, TS3, ahk_exe ts3client_win64.exe"); // 64 bit

        System.out.println("Wait for TS3 instance!");
        // Wait for TeamSpeak to start
        ComplexString waitForOpen = new ComplexString(basicLines);
        waitForOpen.add("WinWait, ahk_group TS3");
        AHKManager.runAHKCommands(waitForOpen, true);
        System.out.println("TS3 instance found, establishing connection.");

        // Set up connection
        synchronized (this) {
            try {
                this.telnetClient.start(HOSTADDRESS, PORT);
                System.out.println("TS3 telnet connection established!");
                System.out.println("Starting event listener!");
                Inspector.runNowAsDaemon(() -> {
                    try {
                        startEventListener(HOSTADDRESS, PORT);
                    } catch (IOException e) {
                        System.out.println("Event listener stopped working.");
                    }
                });
                ready = true;
            } catch (IOException e) {
                ready = false;
            }
        }
        System.out.println("Event listener started.");

        if (ready) {
            // Wait for TS3 to close
            System.out.println("Wait for TS3 instance to close.");
            ComplexString waitForClose = new ComplexString(basicLines);
            waitForClose.add("WinWaitClose, ahk_group TS3");
            AHKManager.runAHKCommands(waitForClose, true);
            ready = false;
            System.out.println("TS3 instance was closed.");
            return true;
        } else {
            System.out.println("TS3 instance has no ClientQuery Plugin enabled!");
            return false;
        }
    }

    /**
     * @throws IOException Throws an exception if no instance of TeamSpeak 3 was found!
     */
    private void startEventListener(String server, int port) throws IOException {
        // Establish connection
        this.telnetEventClient.start(server, port);
        // Get schandler id
        int schandlerID = getSchandlerID();

        // Register event listener in TS3
        this.telnetEventClient.write("clientnotifyregister schandlerid=" + schandlerID + " event=any");
        TS3Event event;
        while (true) {
            try {
                event = this.telnetEventClient.readEvent();
                if (this.callback != null && this.callback.get() != null)
                    synchronized (this.telnetEventClient) {
                        this.callback.get().callback(event);
                    }
            } catch (TS3NotReadyException e) {
                break;
            }
        }
        System.out.println("TeamSpeak was closed before the event listener stopped listening.");
    }

    /**
     * @return Returns true if a connection to TeamSpeak was established.
     */
    public boolean isReady() {
        return this.ready;
    }

    public void waitForReadyness() {
        while (!isReady()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }

    /*
     * EVENTS
     */

    /**
     * @param TS3EventCallback If an event is thrown the TS3EventCallback gets notified. If null no TS3EventCallback is notified.
     */
    public void setOnEvent(TS3EventCallback TS3EventCallback) {
        if (this.telnetEventClient == null)
            this.callback = new WeakReference<>(TS3EventCallback);
        else
            synchronized (this.telnetEventClient) {
                this.callback = new WeakReference<>(TS3EventCallback);
            }
    }

    /*
     * GETTER
     */

    /**
     * @return Returns -1 if something went wrong otherwise a positive number.
     */
    public synchronized int getSchandlerID() {
        if (!isReady())
            throw new TS3NotReadyException();
        try {
            TS3Response response = telnetClient.sendCommand("currentschandlerid");
            Properties prop = new Properties();
            prop.load(new StringReader(response.getResponse().toString()));
            if (response.isError())
                return -1;
            else
                return Integer.parseInt(prop.getProperty("schandlerid"));
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * @return Returns all currently online clients or an empty list.
     */
    public synchronized ArrayList<TS3User> getClients() {
        if (!isReady())
            throw new TS3NotReadyException();
        ArrayList<TS3User> result = new ArrayList<>();
        try {
            TS3Response participantsUnformatted = this.telnetClient.sendCommand("clientlist");
            if (!participantsUnformatted.isError()) {
                String[] participants = participantsUnformatted.getResponse().toString().split("\\|");
                for (String participant : participants)
                    result.add(new TS3User(participant));
            }
        } catch (IOException e) {
        }
        return result;
    }
}
