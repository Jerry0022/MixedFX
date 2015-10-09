package de.mixedfx.ts3;

import java.io.IOException;

public class TS3Tester {
    public static void main(String[] args) {
        try {
            TeamSpeak3.getInstance().getClients().stream().filter(user -> user.isNormalUser()).forEach(client -> System.out.println(client.getName()));
            TeamSpeak3.getInstance().registerEventListener(string -> System.err.println("Event: " + string));
        } catch (IOException e) {
            System.out.println("TS3 is not online?");
        }
        try {
            Thread.sleep(30000);
            TeamSpeak3.getInstance().disconnect();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
