package de.mixedfx.texttospeech;

import de.mixedfx.speechtotext.IncorrectInputLanguage;
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;

/**
 * Created by India_000 on 27.09.2015.
 */
public class Main {
    public static void main(String [] args)
    {
        LocalMaryInterface lm = null;
        try {
            lm = new LocalMaryInterface();
            System.out.println( lm.getAvailableVoices());
        } catch (MaryConfigurationException e) {
            e.printStackTrace();
        }

        TTSConfig config = new TTSConfig();
        config.lang = Language.GERMAN;
        config.male = true;
        try {
            TextToSpeech.say("Hallo, ich heiﬂe Jeremy!", config);
        } catch (IncorrectInputLanguage incorrectInputLanguage) {
            incorrectInputLanguage.printStackTrace();
        }

        System.exit(0);

        try {
            TextToSpeech.loadVoices();
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
