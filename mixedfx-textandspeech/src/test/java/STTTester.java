import de.mixedfx.inspector.Inspector;
import de.mixedfx.speechtotext.SpeechToText;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

/**
 * Created by India_000 on 28.09.2015.
 */
@ComponentScan(basePackages = "de.mixedfx.speechtotext")
public class STTTester {
    @Autowired
    private SpeechToText speechToText;

    @PostConstruct
    public void go() throws InterruptedException {
        BooleanProperty trigger = new SimpleBooleanProperty();
        speechToText.startListening("", trigger, googleResponse -> System.out.println(googleResponse.getAllPossibleResponses()));
        trigger.set(true);
        Thread.sleep(10000);
        trigger.set(false);
        Thread.sleep(10000);
        trigger.set(true);
        Thread.sleep(10000);
        trigger.set(false);
    }

    public static void main(String [] args) throws InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(STTTester.class);
        Inspector.endlessSleep();
    }
}
