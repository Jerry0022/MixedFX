package de.mixedfx.test;

import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Created by Jerry on 12.10.2015.
 */
@ComponentScan
@Configuration
public class NetworkTester {
    @Autowired
    private ConnectivityManager<User> manager;

    @Bean
    public ConnectivityManager<User> getConnectivityManager() {
        return new ConnectivityManager<>();
    }

    @PostConstruct
    public void go() {
        manager.start(new User() {
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

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(NetworkTester.class);
    }
}
