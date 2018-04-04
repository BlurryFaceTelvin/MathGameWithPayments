package com.example.blurryface.mathgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView highScoreText;
    SharedPreferences preferences;
    //make the highscore public
    public static int hiScore;
    int defaultHighScore=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        highScoreText = findViewById(R.id.highScoreText);
        preferences = getSharedPreferences("score",MODE_PRIVATE);
        hiScore = preferences.getInt("highscore",defaultHighScore);
        highScoreText.setText(String.valueOf(hiScore));
    }
    public void onPlay(View view){
        Intent intent = new Intent(MainActivity.this,GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void onQuit(View view){
        System.exit(0);
    }
}
