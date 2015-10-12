package de.mixedfx.ts3;

import lombok.Getter;
import lombok.ToString;

/**
 * Created by Jerry on 10.10.2015.
 */
@ToString
public class TS3Event {
    private
    @Getter
    String eventDescription;

    public TS3Event(String line) {
        this.eventDescription = line;
    }
}
