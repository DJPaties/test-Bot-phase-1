package com.example.wake;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer speechRecognizer;
    private DirectLineAPI directLineAPI;
    private TextView textView;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            startListening();
        }
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "Language not supported.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Initialization failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        directLineAPI = new DirectLineAPI("c_dOkc928KI.YIxL7E51ypIGf55-Bd6BbpIID77HiHR7uRo1em5Umj4");


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                startListening();
            }
        }}
    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.setRecognitionListener(recognitionListener);
        speechRecognizer.startListening(intent);
    }
    private boolean heyRobotResponseTriggered = false;
    private RecognitionListener recognitionListener = new RecognitionListener() {

        private boolean heyRobotResponseTriggered = false;
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (voiceResults == null) {
                textView.setText("No voice results");
            } else {
                boolean recognizedCommand = false;
                String recognizedText = "";
                for (String match : voiceResults) {
                    if (match.toLowerCase().contains("hey robot")) {
                        recognizedCommand = true;
                        recognizedText = "Hello, human!";
                        break;
                    }
                    if (match.toLowerCase().contains("i want help")) {
                        recognizedCommand = true;
                        recognizedText = "How may I assist you, human?";
                        break;
                    }
                    if (match.toLowerCase().contains("make me happy")) {
                        recognizedCommand = true;
                        recognizedText = "pew pew pew ahmad mohsen";
                        break;
                    }
                }

                if (recognizedCommand) {
                    textView.setText(recognizedText);
                    generateSpeech(recognizedText);
                } else {

                    new SendMessageTask().execute(voiceResults.get(0).toLowerCase() + "?");
                }
            }
            restartListeningService();
        }



        @Override
        public void onReadyForSpeech(Bundle params) {
        }
        @Override
        public void onBeginningOfSpeech() {
        }
        @Override
        public void onRmsChanged(float rmsdB) {
        }
        @Override
        public void onBufferReceived(byte[] buffer) {
        }
        @Override
        public void onEndOfSpeech() {
            restartListeningService();
        }
        @Override
        public void onError(int error) {
        }
        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };
    private void restartListeningService() {
        speechRecognizer.cancel();
        startListening();
    }
    public void generateSpeech(String text) {
        String txt = textView.getText().toString();
        if (!txt.isEmpty()) {

        //    if (!txt.equals("No voice results")) { // Check if the message is not "No voice results"
              //  if (textToSpeech.isSpeaking()) {
                   // textToSpeech.stop(); // Stop the current speech if it's still speaking
             //   }
                textToSpeech.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
            }}
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }super.onDestroy();
    }
    private class SendMessageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            directLineAPI.generateToken();
            directLineAPI.startConversation();
            directLineAPI.sendMessage(params[0]);
            return directLineAPI.getMessage();
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
            generateSpeech(result);
        }
    }}





