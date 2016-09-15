package com.example.adeem.tankgame;
import com.google.firebase.analytics.FirebaseAnalytics;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button signin;
    Button signup;
    Button highScores;
    Button startGame;
    Button startGameMulti;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        this.signin = (Button)findViewById(R.id.signInBTN);
        this.signup =  (Button)findViewById(R.id.SignUpBTN);
        this.highScores = (Button)findViewById(R.id.highScoresBTN);
        this.startGame =  (Button)findViewById(R.id.startBTN);
        this.startGameMulti =  (Button)findViewById(R.id.multiBTN);
        this.signup.setOnClickListener(this);
        this.signin.setOnClickListener(this);
        this.startGame.setOnClickListener(this);
        this.startGameMulti.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        int buttonId = view.getId();
        Intent startAct2Intent;
         if(buttonId == R.id.signInBTN){
            startAct2Intent = new Intent(this, LoginActivity.class);
            startActivity(startAct2Intent);
        } else if(buttonId == R.id.SignUpBTN){
             startAct2Intent = new Intent(this, SignUpActivity.class);
             startActivity(startAct2Intent);
        }else if(buttonId == R.id.highScoresBTN){
             startAct2Intent = new Intent(this, SignUpActivity.class); // change
             startActivity(startAct2Intent);
         }else if(buttonId == R.id.startBTN){
             startAct2Intent = new Intent(this, SignUpActivity.class);// change
             startActivity(startAct2Intent);
         }else if(buttonId == R.id.multiBTN){
             startAct2Intent = new Intent(this, SignUpActivity.class);// change
             startActivity(startAct2Intent);
         }


    }
}
