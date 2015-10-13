import de.mixedfx.windows.NetworkAdapter;
import de.mixedfx.windows.WindowsMonitoring;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Created by India_000 on 28.09.2015.
 */
@ComponentScan(basePackages = "de.mixedfx.windows")
public class WindowsTester {
    @PostConstruct
    public void go() {

    }

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
