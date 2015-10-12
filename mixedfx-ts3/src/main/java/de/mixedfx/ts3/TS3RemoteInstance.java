package de.mixedfx.ts3;

import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by Jerry on 10.10.2015.
 */
@Component
public class TS3RemoteInstance extends TS3Instance {
    public boolean start(String ip) throws IOException {
        return super.start(ip, PORT);
    }
}
