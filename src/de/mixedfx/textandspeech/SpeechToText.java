package de.mixedfx.textandspeech;

import java.io.File;
import java.nio.file.Files;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.GSpeechDuplex;
import com.darkprograms.speech.recognizer.GSpeechResponseListener;

import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import javaFlacEncoder.FLACFileWriter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class SpeechToText
{
	/**
	 * May change this language before listen again.
	 */
	public static Language language = Language.GERMAN;

	private static GSpeechDuplex dup;
	private static Microphone mic;
	private static long stopDelay = 250;
	private static volatile boolean listening;

	/**
	 * Starts to listen endless for a Unfortunately the API doesn't give a possibility to fetch an wrong API-Key.
	 * 
	 * @param apiKey
	 *            The API key from Google. There exist some limits in usage. Known is a limit of 60 seconds listening time and probably a maximum request. To get a new API Key follow the steps (about
	 *            1 minute) in section "Akquiring Keys" on http://www.chromium.org/developers/how-tos/api-keys
	 * @param trigger
	 *            If true this listens synchronously!
	 * @param callback
	 *            The callback {@link #onResponse(com.darkprograms.speech.recognizer.GoogleResponse)} is asynchronous.
	 * @throws NoMicroFoundException
	 *             Throws this exception if no microphone was found!
	 */
	public static void startListening(String apiKey, BooleanProperty trigger, GSpeechResponseListener callback) throws NoMicroFoundException
	{
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
			Log.assets.warn("No microphone found. Can't listen to the speaking!");
			throw new NoMicroFoundException();
		}

		trigger.set(false);
		trigger.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				listening = newValue;

				if (newValue)
				{
					Inspector.runNowAsDaemon(() ->
					{
						try
						{
							Log.textAndSpeech.info("Start capturing audio!");
							File file = new File("CRAudioTest.flac");// The File to record the buffer to. You can also create your own buffer using the getTargetDataLine() method.
							mic.captureAudioToFile(file);// Begins recording
							while (listening)
								Thread.sleep(stopDelay);// Recording
							mic.close();// Stops recording
							Log.textAndSpeech.info("Stop capturing!");
							Log.textAndSpeech.info("Send audio to Google!");
							byte[] data = Files.readAllBytes(mic.getAudioFile().toPath());// Saves data into memory.
							dup.recognize(data, (int) mic.getAudioFormat().getSampleRate()); // Sends x milliseconds voice recording to Google
							mic.getAudioFile().delete();// Deletes Buffer file
							Log.textAndSpeech.info("Removed old file. Wait for Google's response.");
						} catch (Exception ex)
						{
							Log.textAndSpeech.fatal("Can't write or read from file to which the voice via the micro is captured!");
						}
					});
				}
			}
		});
	}
}
