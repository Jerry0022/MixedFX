package de.mixedfx.logging;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.config.Configurator;

@Log4j2(topic = "network")
public class LoggingTester {
    public static void main(String[] args) {
        Configurator.initialize("MixedFX", "log4j2.xml");
        log.error("That's Log4j");
    }
}
