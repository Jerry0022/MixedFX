package de.mixedfx.ts3;

import de.mixedfx.inspector.Inspector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

/**
 * Created by Jerry on 09.10.2015.
 */
@ComponentScan
public class TS3Tester_cdi {
    @Autowired
    private TS3LocalInstance instance;

    public void listen() throws IOException {
        // Register for TS3 events
        instance.setOnEvent(event -> System.err.println(event));
        Inspector.runNowAsDaemon(() -> {
            while (true) {
                System.out.println("Waiting for TS3 to start!");
                try {
                    boolean pluginEnabled = instance.start();
                    if (!pluginEnabled) {
                        System.out.println("In TS3 ClientQuery plugin must be enabled!");
                        break; // Stop this
                    }
                } catch (IOException e) {
                    System.out.println("An unknown exception occurred!");
                    e.printStackTrace();
                }
                System.out.println("TS3 was closed!");
            }
        });
        instance.waitForReadyness();
        System.err.println("Read schandlerid: " + instance.getSchandlerID());
        System.err.println("Read clients: " + instance.getClients());
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("START");
        ApplicationContext context = new AnnotationConfigApplicationContext(TS3Tester_cdi.class);
        context.getBean(TS3Tester_cdi.class).listen();
        System.out.println("Close application in 100 seconds.");
        Thread.sleep(100000);
        System.out.println("STOP");
    }
}
