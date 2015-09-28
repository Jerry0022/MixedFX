import de.mixedfx.logging.Log;
import de.mixedfx.windows.NetworkAdapter;
import de.mixedfx.windows.WindowsMonitoring;

import java.util.Arrays;
import java.util.List;

/**
 * Created by India_000 on 28.09.2015.
 */
public class WindowsTester {
    public static void main(String [] args) throws InterruptedException {
        WindowsMonitoring.startMonitoring(new WindowsMonitoring.Callback<NetworkAdapter>() {
            @Override
            public void action(List<NetworkAdapter> object) {
                System.out.println(object.get(0).toString());
            }

            @Override
            public List<NetworkAdapter> getItems() {
                return Arrays.asList(new NetworkAdapter("Ethernet 2"));
            }
        });

        while(true)
        {
            Thread.sleep(1000);
        }
    }
}
