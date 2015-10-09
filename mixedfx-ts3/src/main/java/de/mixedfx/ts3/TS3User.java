package de.mixedfx.ts3;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class TS3User {
    private Properties property = new Properties();

    /**
     *
     * @param original The user as text String formatted by TeamSpeak 3
     */
    public TS3User(String original) {
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
        return Integer.parseInt(property.getProperty("clid"));
    }

    public int getChannelID() {
        return Integer.parseInt(property.getProperty("cid"));
    }

    public int getDatabaseID() {
        return Integer.parseInt(property.getProperty("client_database_id"));
    }

    /**
     * @return Returns true if it is a usual user. False if it is a generated Server Client.
     */
    public boolean isNormalUser() {
        return Integer.parseInt(property.getProperty("client_type")) == 0;
    }

    public String getName() {
        return property.getProperty("client_nickname");
    }
}
