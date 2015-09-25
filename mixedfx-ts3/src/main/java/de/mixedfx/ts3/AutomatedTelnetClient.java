package de.mixedfx.ts3;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created by Jerry on 23.09.2015.
 */
public class AutomatedTelnetClient {

    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;

    public AutomatedTelnetClient(String server, int port) throws IOException {
        // Connect to the specified server
        telnet.connect(server, port);
        // Get input and output stream references
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
    }

    /**
     * @param pattern If empty everything is read until the output
     */
    public String readUntil(String pattern) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuilder sb = new StringBuilder();
            while (true) {
                int asciiID = in.read();
                char ch = (char) asciiID;

                // The end of the input stream?
                if (asciiID == -1)
                    return null;

                // Append character to input message
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern))
                        return sb.toString();
                    else if (sb.toString().endsWith("msg=command\\snot\\sfound"))
                        throw new IllegalStateException("This was not a correct command.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(String command) {
        try {
            write(command);
            return readUntil("msg=ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}