package com.example.blurryface.mathgame;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.africastalking.AfricasTalking;
import com.africastalking.models.payment.checkout.CheckoutResponse;
import com.africastalking.models.payment.checkout.MobileCheckoutRequest;
import com.africastalking.services.PaymentService;
import com.africastalking.utils.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Random;

import dmax.dialog.SpotsDialog;

import static com.example.blurryface.mathgame.MainActivity.hiScore;

public class GameActivity extends AppCompatActivity {
    Toolbar toolbar;
    MyCountDownTimer countDownTimer;
    TextView timer,operation,num1,num2,currentLevel,scorePoint;
    EditText answer;
    int level,score;
    PaymentService paymentService;
    SpotsDialog payDialog;
    //keep our high score and normal score
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    OkHttpClient client;
    Request request;
    int status;
    boolean onFirstResume;
    SpotsDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //initialise textviews and editText
        operation = findViewById(R.id.operationText);
        num1 = findViewById(R.id.num1Text);
        num2 = findViewById(R.id.num2Text);
        timer = findViewById(R.id.timerText);
        answer = findViewById(R.id.answerEditText);
        currentLevel = findViewById(R.id.levelText);
        scorePoint = findViewById(R.id.scoreTextView);
        //initialise our toolbar and give it a title
        toolbar = findViewById(R.id.game_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Math Game");
        //initialise the paydialog
        payDialog = new SpotsDialog(this,"Loading Mpesa");
        dialog = new SpotsDialog(this,"Processing");
        level = Integer.parseInt(currentLevel.getText().toString());
        score = Integer.parseInt(scorePoint.getText().toString());
        SetQuestion();
        startTimer();
        //initialise Africastalking sdk
        try {
            AfricasTalking.initialize("192.168.1.81",35897,true);
        }catch (Exception e){
            e.printStackTrace();
        }
        //initialise our shared preferences
        sharedPreferences = getSharedPreferences("score",MODE_PRIVATE);
        //set our status to 0 to mean first resume
        onFirstResume = true;
        status = 0;
    }

    public void onNext(View view){
        int answerGiven = Integer.parseInt(answer.getText().toString());
        answer.setText("");
        if(isCorrect(answerGiven)) {
            updateScores();
            SetQuestion();
            startTimer();
        }
    }
    public void updateScores(){
        countDownTimer.cancel();
        for (int i = 1; i <=level; i++) {
                score=score+ i;
            }
        level++;
        currentLevel.setText(String.valueOf(level));
        scorePoint.setText(String.valueOf(score));
    }
    public void startTimer(){
        //set the timer for ten seconds that has an interval of 500 milliseconds
        countDownTimer = new MyCountDownTimer(10000,500);
        countDownTimer.start();
    }
    public void SetQuestion(){
        //setting the math question
        int myLevel = Integer.parseInt(currentLevel.getText().toString());
        int range = myLevel*3;
        Random random = new Random();
        num1.setText(String.valueOf(random.nextInt(range)+1));
        num2.setText(String.valueOf(random.nextInt(range)+1));
        //getting random operators
        switch (random.nextInt(4)){
            case 0:
                operation.setText("*");
                break;
            case 1:
                operation.setText("+");
                break;
            case 2:
                operation.setText("-");
                break;
            case 3:
                operation.setText("/");
                break;
            default:
                operation.setText("0");

        }
    }

    public boolean isCorrect(int AnswerGiven){
        int firstNum = Integer.parseInt(num1.getText().toString());
        int secondNum = Integer.parseInt(num2.getText().toString());
        int answ=0;
        switch (operation.getText().charAt(0)){
            case '+':
                answ = firstNum+secondNum;
                break;
            case '-':
                answ = firstNum-secondNum;
                break;
            case '*':
                answ = firstNum*secondNum;
                break;
            case '/':
                answ = firstNum/secondNum;
                break;
        }
        if(answ==AnswerGiven)
            return true;
        else
            return false;
    }

    public class Paying extends AsyncTask<Void,String,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                paymentService = AfricasTalking.getPaymentService();
                MobileCheckoutRequest checkoutRequest = new MobileCheckoutRequest("MusicApp","KES 10","0703280748");
                paymentService.checkout(checkoutRequest, new Callback<CheckoutResponse>() {
                    @Override
                    public void onSuccess(CheckoutResponse data) {
                        payDialog.dismiss();
                        Toast.makeText(GameActivity.this,data.status,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        payDialog.dismiss();
                        Log.e("err",throwable.getMessage());
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            long seconds = l/1000;
            //starts the count down
            timer.setText(String.format("%02d", seconds/60) + ":" + String.format("%02d", seconds%60));

        }

        @Override
        public void onFinish() {
            payDialog.show();
            countDownTimer.cancel();
            //count down ends and user hasnt answered the question
            final AlertDialog.Builder paymentDialog = new AlertDialog.Builder(GameActivity.this);
            paymentDialog.setMessage("Do you want to pay for extra time");
            paymentDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    payDialog.dismiss();
                    //check our scores if player scores more than the high score update our high score
                    if(score>hiScore){
                        editor = sharedPreferences.edit();
                        hiScore = score;
                        editor.putInt("highscore",hiScore);
                        editor.apply();
                    }
                    //send to game over Screen
                    Intent intent = new Intent(GameActivity.this,GameOverActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("scores",String.valueOf(score));
                    startActivity(intent);
                }
            });
            paymentDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new Paying().execute();
                    //makes sure its not the first resume
                    status = 5;

                }
            });
            AlertDialog alertDialog = paymentDialog.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        countDownTimer.cancel();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //when user first gets to the activity
        if(onFirstResume){
            onFirstResume = false;
            Log.e("resume",String.valueOf(status));
        }else if(!onFirstResume&&status==5) {
            //after mpesa pop up
            status = 3;
            Log.e("resume",String.valueOf(status));

            dialog.show();
            //wait for ten seconds to confirm
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    confirmPayment();
                }
            }, 10000);
        }else{
            Log.e("resume","normal");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(status==5){
            //pause by the checkout
            status = 5;
            Log.e("pause",String.valueOf(status));
        }
        else {
            //normal pause
            status=3;
            Log.e("pause",String.valueOf(status));
        }
    }
    public void confirmPayment(){
            client = new OkHttpClient();
            request = new Request.Builder().url("http://192.168.1.81:30001/transaction/status").build();
            client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    dialog.dismiss();
                    Log.e("failure",e.getMessage());
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    dialog.dismiss();
                    String status = response.body().string();
                    //if user either cancels or has insufficient funds we go to game over
                    if(status.equals("Failed")){
                        //if it fails to pay sends you to game over page
                        showMessage("failed");
                        Intent intent = new Intent(GameActivity.this,GameOverActivity.class);
                        intent.putExtra("scores",String.valueOf(score));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }else if(status.equals("Success")){
                        //if successful add the time and player gets another chance to continue
                        showMessage("successful");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startTimer();
                            }
                        });


                    }

                }
            });
        }

        public void showMessage(final String message){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                Toast.makeText(GameActivity.this,"Your payment has "+message,Toast.LENGTH_LONG).show();
                }
         });
        }
}

