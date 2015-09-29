import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.user.User;

import java.util.UUID;

/**
 * Created by Jerry on 25.09.2015.
 */
public class NetworkTester {
    public static void main(String[] args) {
        ConnectivityManager manager = new ConnectivityManager();
        manager.start(new User() {
            private String id;
            {
                this.id = UUID.randomUUID().toString();
            }
            @Override
            public Object getIdentifier() {
                return this.id;
            }

            @Override
            public void mergeMe(User newUser) {

            }

            @Override
            public void setMeUp() {

            }
        });
    }
}
