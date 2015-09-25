import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.user.User;

/**
 * Created by Jerry on 25.09.2015.
 */
public class NetworkTester {
    public static void main(String[] args) {
        ConnectivityManager manager = new ConnectivityManager();
        manager.start(new User() {
            @Override
            public Object getIdentifier() {
                return null;
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
