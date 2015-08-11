package de.mixedfx.test;

import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;

import de.mixedfx.logging.Log;
import de.mixedfx.textandspeech.SpeechToText;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class STTTester
{

	public static void main(String[] args)
	{
		BooleanProperty bol = new SimpleBooleanProperty(false);

		SpeechToText.startListening("", bol, new GSpeechResponseListener()
		{
			@Override
			public void onResponse(GoogleResponse gr)
			{
				System.out.println("Google thinks you said: " + gr.getResponse());
				System.out.println("with " + ((gr.getConfidence() != null) ? (Double.parseDouble(gr.getConfidence()) * 100) : null) + "% confidence.");
				System.out.println("Google also thinks that you might have said:" + gr.getOtherPossibleResponses());
			}
		});

		Log.textAndSpeech.info("Start listening!");
		bol.set(true);
		try
		{
			Thread.sleep(8000);
		} catch (InterruptedException e)
		{
		}
		bol.set(false);
		Log.textAndSpeech.info("Stop listening!");
		while (true)
			;
	}

}
