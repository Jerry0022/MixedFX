package de.mixedfx.ts3;

import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by Jerry on 09.10.2015.
 */
@Component
public class TS3LocalInstance extends TS3Instance {
    public boolean start() throws IOException {
        return super.start("localhost", PORT);
    }
}
