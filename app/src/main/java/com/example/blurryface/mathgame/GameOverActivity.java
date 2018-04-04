package com.example.blurryface.mathgame;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.example.blurryface.mathgame.MainActivity.hiScore;

public class GameOverActivity extends AppCompatActivity {
    TextView score,highScore;
    int high;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        score = findViewById(R.id.yourScoreText);
        highScore=findViewById(R.id.yourhighScoreText);
        sharedPreferences = getSharedPreferences("score",MODE_PRIVATE);
        high = sharedPreferences.getInt("highscore", hiScore);
        highScore.setText(String.valueOf(high));
        String playerscore;
        playerscore = getIntent().getStringExtra("scores");
        score.setText(playerscore);
    }
    public void onReplay(View view){
        Intent intent = new Intent(GameOverActivity.this,GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void onQuit(View view){
        Intent intent = new Intent(GameOverActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}
