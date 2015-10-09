package de.mixedfx.ts3;

import de.mixedfx.java.ComplexString;
import lombok.Getter;

/**
 * Created by Jerry on 04.10.2015.
 */
public class TS3Response {
    private
    @Getter
    boolean done;

    private
    @Getter
    boolean error;

    private
    @Getter
    String errorMessage;

    private
    @Getter
    ComplexString response;

    public TS3Response() {
        this.response = new ComplexString();
        this.error = true;
        this.errorMessage = "";
    }

    public void addLine(String line) {
        if (done)
            throw new IllegalStateException("Response already fully got?");

        if (line.contains("selected schandlerid=")) {
            done = true;
            error = false;
        } else if (line.contains("error id=") && line.contains("msg=")) {
            done = true;
            errorMessage = line.split("msg=")[1];
            error = errorMessage.equalsIgnoreCase("ok") ? true : false;
        } else {
            this.response.add(line);
        }
    }
}
