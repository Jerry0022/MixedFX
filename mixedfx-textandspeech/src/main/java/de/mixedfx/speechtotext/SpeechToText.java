package de.mixedfx.speechtotext;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.GSpeechDuplex;
import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.texttospeech.Language;
import javaFlacEncoder.FLACFileWriter;
import javafx.beans.property.BooleanProperty;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;

@Component
@Log4j2(topic = "SpeechToText")
public class SpeechToText
{
	/**
	 * May change this language before listen again.
	 */
    private
    @Setter
    Language language = Language.GERMAN;

    private GSpeechDuplex dup;
    private Microphone mic;
    private long stopDelay = 250;
    private volatile boolean listening;

	/**
	 * Starts to listen endless for a Unfortunately the API doesn't give a possibility to fetch an wrong API-Key.
	 * 
	 * @param apiKey
	 *            The API key from Google. There exist some limits in usage. Known is a limit of 60 seconds listening time and probably a maximum request. To get a new API Key follow the steps (about
	 *            1 minute) in section "Akquiring Keys" on http://www.chromium.org/developers/how-tos/api-keys
	 * @param trigger
	 *            If true this listens synchronously!
	 * @param callback
	 *            The callback is asynchronous.
	 * @throws NoMicroFoundException
	 *             Throws this exception if no microphone was found!
	 */
    public void startListening(String apiKey, BooleanProperty trigger, GSpeechResponseListener callback) throws NoMicroFoundException {
        if (apiKey == null || apiKey.isEmpty())
            throw new NoAPIKeyFoundException();

		try
		{
			dup = new GSpeechDuplex(apiKey);// Instantiate the API
			dup.setLanguage(language.getListening());
			dup.addResponseListener(callback);
			mic = new Microphone(FLACFileWriter.FLAC);// Instantiate microphone and have it record FLAC file.
		} catch (Exception e)
		{
            log.warn("No microphone found. Can't listen to the speaking!");
            throw new NoMicroFoundException();
        }

		trigger.set(false);
		trigger.addListener((observable, oldValue, newValue) -> {
            listening = newValue;
            if (newValue)
            {
                Inspector.runNowAsDaemon(() ->
                {
                    try
                    {
                        log.info("Start capturing audio!");
                        File file = new File("CRAudioTest.flac"); // The File to record the buffer to. You can also create your own buffer using the getTargetDataLine() method.
                        mic.captureAudioToFile(file); // Begins recording
                        while (listening)
                            Thread.sleep(stopDelay); // Recording
                        mic.close();// Stops recording
                        log.info("Stop capturing!");
                        log.info("Send audio to Google!");
                        byte[] data = Files.readAllBytes(mic.getAudioFile().toPath());// Saves data into memory.
                        dup.recognize(data, (int) mic.getAudioFormat().getSampleRate()); // Sends x milliseconds voice recording to Google
                        mic.getAudioFile().delete();// Deletes Buffer file
                        log.info("Removed old file. Wait for Google's response.");
                    } catch (Exception ex)
                    {
                        log.fatal("Can't write or read from file to which the voice via the micro is captured!");
                    }
                });
            }
        });
	}
}
