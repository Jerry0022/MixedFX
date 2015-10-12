package de.mixedfx.test;

import de.mixedfx.inspector.Inspector;
import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@ComponentScan(basePackages = "de.mixedfx.network")
@Configuration
public class NetworkTester {
    @Autowired
    private ConnectivityManager<User> manager;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ConnectivityManager.class);
        context.register(NetworkTester.class);
        context.refresh();
        context.getBean(ConnectivityManager.class).start(new User() {
            String id = UUID.randomUUID().toString();

            @Override
            public void mergeMe(User newUser) {
            }

            @Override
            public void setMeUp() {

            }

            @Override
            public Object getIdentifier() {
                return id;
            }
        });

        Inspector.endlessSleep();
    }
}
