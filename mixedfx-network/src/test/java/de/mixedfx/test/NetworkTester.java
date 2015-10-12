package de.mixedfx.test;

import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.MessageBus;
import de.mixedfx.network.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Created by Jerry on 12.10.2015.
 */
@ComponentScan
@Configuration
public class NetworkTester {
    //@Autowired
    private ConnectivityManager<User> manager;

    @Autowired
    MessageBus bus;

    @Bean
    public ConnectivityManager<User> getConnectivityManager() {
        return new ConnectivityManager<>();
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConnectivityManager.class, NetworkTester.class);
        System.out.println(context.getBean(MessageBus.class));
        System.exit(0);
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
    }
}
