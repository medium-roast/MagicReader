package com.example.MagicReader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class MainActivityVoiceController {
    private final Activity activity;
    private final TextToSpeech textToSpeech;
    private final Handler mainThreadHandler;

    private final String UTTERANCE_ID = "ResultText";

    public MainActivityVoiceController(Activity activity) {
        this.activity = activity;
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
        // create the TTS instance
        textToSpeech = new TextToSpeech(activity.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.US);
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Log.e("TTS", "Initialization failed!");
                }
            }
        });
    }

    public void readResult() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView resultView = activity.findViewById(R.id.resultView);
                String data = resultView.getText().toString();
                int speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                if (speechStatus == TextToSpeech.ERROR) {
                    Log.e("TTS", "Error in converting Text to Speech!");
                }
            }
        });
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
