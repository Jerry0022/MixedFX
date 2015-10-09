package de.mixedfx.network;


import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Created by Jerry on 25.09.2015.
 */
public class NetworkTester {
    public static void main(String[] args) {
        WeldContainer weld = new Weld().initialize();
        ConnectivityManagerUser hello = weld.instance().select(ConnectivityManagerUser.class).get();
        for (int i = 0; i < 10; i++) {
            System.out.println("Other Main");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}