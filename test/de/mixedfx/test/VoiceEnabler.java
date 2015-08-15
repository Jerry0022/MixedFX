package de.mixedfx.test;

import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;

import de.mixedfx.assets.ImageProducer;
import de.mixedfx.gui.RegionManipulator;
import de.mixedfx.textandspeech.IncorrectInputLanguage;
import de.mixedfx.textandspeech.Language;
import de.mixedfx.textandspeech.SpeechToText;
import de.mixedfx.textandspeech.TTSConfig;
import de.mixedfx.textandspeech.TextToSpeech;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class VoiceEnabler extends Pane
{
	private BooleanProperty voiceSwitcher = new SimpleBooleanProperty(false);

	public VoiceEnabler()
	{
		RegionManipulator.bindBackground(this, ImageProducer.getMonoColored(Color.BLUE));
		this.setPrefSize(50, 50);
		this.setMaxSize(50, 50);
		this.setStyle("-fx-cursor: hand");

		// Attention: This action may need some seconds!
		TextToSpeech.init();

		// Set TTS config
		TTSConfig config = new TTSConfig();
		config.lang = Language.GERMAN;
		config.male = false;
		config.block = true;

		// TODO Get username from config
		String userName = "Jeremy";

		voiceSwitcher.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				// Change style of this!
				RegionManipulator.bindBackground(VoiceEnabler.this, newValue.booleanValue() ? ImageProducer.getMonoColored(Color.GREEN) : ImageProducer.getMonoColored(Color.BLUE));
			}
		});

		this.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				voiceSwitcher.set(voiceSwitcher.get() ? false : true);
			}
		});

		// Incoming voice?
		SpeechToText.language = Language.ENGLISH;
		SpeechToText.startListening("AIzaSyBI012J_HtuCeXTpwvcaNB3awo88uAbIGA", voiceSwitcher, new GSpeechResponseListener()
		{
			@Override
			public void onResponse(GoogleResponse gr)
			{
				System.out.println("Antwort von Google: " + gr.getResponse());
				System.out.println("Jetzt auf Deutsch");
				try
				{
					TextToSpeech.say("Hallo " + userName + ", du hast gerade gesagt: " + gr.getResponse(), config);
				} catch (IncorrectInputLanguage e)
				{
					try
					{
						TextToSpeech.say("Es tut mir Leid, aber ich habe dich nicht verstanden!", config);
					} catch (IncorrectInputLanguage e1)
					{
					}
				}
				System.out.println("Jetzt auf Englisch");
				config.lang = Language.ENGLISH;
				try
				{
					TextToSpeech.say("Hello " + userName + "! you have said " + gr.getResponse(), config);
				} catch (IncorrectInputLanguage e)
				{
				}
			}
		});
	}
}
