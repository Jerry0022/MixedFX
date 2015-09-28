import de.mixedfx.speechtotext.IncorrectInputLanguage;
import de.mixedfx.texttospeech.Language;
import de.mixedfx.texttospeech.TTSConfig;
import de.mixedfx.texttospeech.TextToSpeech;
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;

import java.io.IOException;

/**
 * Created by India_000 on 27.09.2015.
 */
public class TTSTester {
    public static void main(String [] args) throws MaryConfigurationException {
        LocalMaryInterface lm = new LocalMaryInterface();
        System.out.println("Available voices: " + lm.getAvailableVoices());

        for(Language lang : Language.values())
        {
            for(int i=0; i<2; i++)
            {
                TTSConfig config = new TTSConfig();
                config.lang = lang;
                config.male = i==0;
                config.block = true;
                try {
                    TextToSpeech.say("Hello, my name is Jeremy!", config);
                } catch (IncorrectInputLanguage incorrectInputLanguage) {
                    incorrectInputLanguage.printStackTrace();
                }
            }
        }
    }
}
