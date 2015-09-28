package de.mixedfx.ts3;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Properties;

public class TS3Tester {
    public static void main(String[] args) {
        try {
            TeamSpeak3.getInstance();
            TeamSpeak3.getInstance().getClients().stream().filter(user -> user.isNormalUser()).forEach(client -> System.out.println(client.getName()));
            //TeamSpeak3.getInstance().registerEventListener(string -> System.err.println("Event: " + string));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("TS3 is not online?");
        }
        try {
            Thread.sleep(30000);
            TeamSpeak3.getInstance().disconnect();
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }
}
