package de.mixedfx.ts3.de.mixedfx.ts3.cdi;

import de.mixedfx.logging.Log;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Jerry on 10.10.2015.
 */
@Configuration
public class TS3Instance {
    @Bean
    @Qualifier(value = "TS3")
    public Logger produceLogger() {
        return Log.CONTEXT.getLogger("TS3");
    }

    @Autowired
    @Qualifier(value = "TS3")
    Logger LOGGER;
}
