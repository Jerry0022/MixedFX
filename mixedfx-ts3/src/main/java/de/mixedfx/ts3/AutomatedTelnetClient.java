package de.mixedfx.ts3;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Created by Jerry on 23.09.2015.
 * Customized for Teamspeak 3!
 */
public class AutomatedTelnetClient {
    private TelnetClient telnet;
    private BufferedReader in;
    private PrintStream out;

    public AutomatedTelnetClient(String server, int port) throws IOException {
        telnet = new TelnetClient();
        // Connect to the specified server
        telnet.connect(server, port);
        // Get input and output stream references
        in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
        out = new PrintStream(telnet.getOutputStream(), true);
    }

    public TS3Response read() throws IOException {
        TS3Response response = new TS3Response();
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.trim().isEmpty())
                response.addLine(line);
            if (response.isDone())
                break;
        }

        return response;
    }


    public void write(String value) {
        out.println(value);
    }

    public TS3Response sendCommand(String command) throws IOException {
        write(command);
        return read();
    }

    public void disconnect() throws IOException {
        telnet.disconnect();
    }
}