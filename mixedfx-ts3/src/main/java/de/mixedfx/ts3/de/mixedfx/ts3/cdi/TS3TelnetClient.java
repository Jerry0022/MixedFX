package de.mixedfx.ts3.de.mixedfx.ts3.cdi;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

@Component
public class TS3TelnetClient {
    @Autowired
    @Qualifier(value = "TS3")
    private Logger logger;

    private TelnetClient telnet;
    private BufferedReader in;
    private PrintStream out;

    /**
     * @param server
     * @param port
     * @throws IOException If TeamSpeak 3 Client Plugin is not available. E. g. TeamSpeak 3 is not opened.
     */
    public void start(String server, int port) throws IOException {
        logger.info("HAMMER");
        telnet = new TelnetClient();
        // Connect to the specified server
        telnet.connect(server, port);
        // Get input and output stream references
        in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
        out = new PrintStream(telnet.getOutputStream(), true);
        // Read first ouput of ts3 telnet
        read();
    }

    /**
     * Reads the output of TeamSpeak
     *
     * @return Returns the output as complete TS3Response.
     * @throws IOException Throws exception if something went wrong while reading.
     */
    public synchronized TS3Response read() throws IOException {
        TS3Response response = new TS3Response();
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.trim().isEmpty())
                response.addLine(line);
            if (response.isComplete())
                break;
        }

        return response;
    }

    public synchronized TS3Event readEvent() throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return new TS3Event(line);
            }
        }
        throw new TS3NotReadyException();
    }

    /**
     * Writes one line to telnet.
     *
     * @param command The command to execute.
     */
    public synchronized void write(String command) {
        out.println(command);
    }

    /**
     * Writes a command and waits for the answer.
     *
     * @param command The command to execute.
     * @return Returns the {@link TS3Response} from telnet.
     * @throws IOException Is thrown if sth. went wrong while reading.
     */
    public synchronized TS3Response sendCommand(String command) throws IOException {
        write(command);
        return read();
    }

    /**
     * Disconnects telnet client.
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        telnet.disconnect();
    }
}