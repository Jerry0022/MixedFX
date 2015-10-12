package de.mixedfx.ts3;

import de.mixedfx.java.ComplexString;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Jerry on 04.10.2015.
 */
@ToString
public class TS3Response {
    private
    @Getter
    boolean complete;

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
        if (complete)
            throw new IllegalStateException("Response already fully got?");

        if (line.contains("selected schandlerid=")) {
            complete = true;
            error = false;
        } else if (line.contains("error id=") && line.contains("msg=")) {
            complete = true;
            errorMessage = line.split("msg=")[1];
            error = !errorMessage.trim().equalsIgnoreCase("ok");
        } else {
            this.response.add(line);
        }
    }
}
