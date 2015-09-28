import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;
import de.mixedfx.speechtotext.SpeechToText;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Created by India_000 on 28.09.2015.
 */
public class STTTester {
    public static void main(String [] args) throws InterruptedException {
        BooleanProperty trigger = new SimpleBooleanProperty();
        SpeechToText.startListening("", trigger, googleResponse -> System.out.println(googleResponse.getResponse()));
        trigger.set(true);
        Thread.sleep(5000);
        trigger.set(false);
        while(true)
            ;
    }
}
