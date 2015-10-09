package de.mixedfx.ts3;

import de.mixedfx.logging.Log;
import de.mixedfx.windows.ahk.AHKManager;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;

/**
 * Created by Jerry on 04.10.2015.
 */
public class TS3Manager {
    public interface TS3Listener {
        void started();

        void closed();
    }

    private static Logger LOGGER = Log.CONTEXT.getLogger("TeamSpeak3");

    public static void start(TS3Listener listener) throws IOException {
        LOGGER.debug("Wait for TeamSpeak 3!");
        AHKManager.runAHKCommands("WinWait, ahk_exe ts3client_win64.exe ahk_exe ts3client_win32.exe", true);
        listener.started();


        LOGGER.info("Teamspeak 3 was started!");

    }
}
