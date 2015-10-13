import de.mixedfx.inspector.Inspector;
import de.mixedfx.speechtotext.IncorrectInputLanguage;
import de.mixedfx.texttospeech.Language;
import de.mixedfx.texttospeech.TTSConfig;
import de.mixedfx.texttospeech.TextToSpeech;
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@ComponentScan(basePackages = {"de.mixedfx.texttospeech"})
@Configuration
public class TTSTester {
    @Autowired
    private LocalMaryInterface localMaryInterface;

    @Bean
    public LocalMaryInterface produceLocalMaryInterface() throws MaryConfigurationException {
        return new LocalMaryInterface();
    }

    @Autowired
    private TextToSpeech textToSpeech;

    @Autowired
    private TTSConfig ttsConfig;

    @PostConstruct
    public void go() {
        System.out.println("Available voices: " + localMaryInterface.getAvailableVoices());

        for (Language lang : Language.values()) {
            for (int i = 0; i < 2; i++) {
                ttsConfig.lang = lang;
                ttsConfig.male = i == 0;
                ttsConfig.block = true;
                try {
                    textToSpeech.say("Hello, my name is Jeremy!", ttsConfig);
                } catch (IncorrectInputLanguage incorrectInputLanguage) {
                    incorrectInputLanguage.printStackTrace();
                }
            }
        }
    }

    public static void main(String [] args) throws MaryConfigurationException {
        ApplicationContext context = new AnnotationConfigApplicationContext(TTSTester.class);
        Inspector.endlessSleep();
    }
}
