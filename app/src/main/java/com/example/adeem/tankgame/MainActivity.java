package com.example.adeem.tankgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button signin;
    Button signup;
    Button highScores;
    Button startGame;
    Button startGameMulti;
    Button logoff;
    private TextView textRef;
    private SharedPreferences prefs;
    private Spinner spinner;

    private String my_pref_name ;
    private String SHuserName ;

    private Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.signin = (Button)findViewById(R.id.signInBTN);
        this.signup =  (Button)findViewById(R.id.SignUpBTN);
        this.highScores = (Button)findViewById(R.id.highScoresBTN);
        this.startGame =  (Button)findViewById(R.id.startBTN);
        this.startGameMulti =  (Button)findViewById(R.id.multiBTN);

      //  this.startGameMulti.setVisibility(View.GONE);
        this.signup.setOnClickListener(this);
        this.signin.setOnClickListener(this);
        this.startGame.setOnClickListener(this);
        this.startGameMulti.setOnClickListener(this);
        this.highScores.setOnClickListener(this);
        this.logoff = (Button) findViewById(R.id.logOffBTN);
        this.logoff.setOnClickListener(this);

        this.textRef = (TextView)findViewById(R.id.txtinhere);

        res = getResources();

        my_pref_name = res.getString(R.string.SharedPreferencesPrefsName);
        SHuserName = res.getString(R.string.SharedPreferencesUserName);

        spinner = (Spinner) findViewById(R.id.Diff_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Diff_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
        String restoredText = prefs.getString(SHuserName, null);
        if(restoredText!=null){
            this.textRef.setText(res.getString(R.string.signedInAs) + restoredText);
        }


        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                String restoredText = prefs.getString(SHuserName, null);
                if(restoredText!=null){
                   textRef.setText(res.getString(R.string.signedInAsAfterSignUp) + restoredText);
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onClick(View view) {

        int buttonId = view.getId();
        Intent startAct2Intent;
         if(buttonId == R.id.signInBTN){
            startAct2Intent = new Intent(this, LoginActivity.class);
            startActivity(startAct2Intent);

             prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
        } else if(buttonId == R.id.SignUpBTN){
             startAct2Intent = new Intent(this, SignUpActivity.class);
             startActivity(startAct2Intent);
             prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
        }else if(buttonId == R.id.highScoresBTN){
             startAct2Intent = new Intent(this,highScores.class);
             startActivity(startAct2Intent);

         }else if(buttonId == R.id.startBTN){
             prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
             SharedPreferences.Editor editor = getSharedPreferences(my_pref_name, MODE_PRIVATE).edit();
             editor.putString("difficultly", spinner.getSelectedItem().toString());
             editor.commit();
             String restoredText = prefs.getString(SHuserName, null);
                if(restoredText == null){
                    Toast.makeText(getApplicationContext(),res.getString(R.string.error_userNotSigedIn), Toast.LENGTH_SHORT).show();
                    return;
                }
             startAct2Intent = new Intent(this, inGameMap.class);
             startActivity(startAct2Intent);
         }else if(buttonId == R.id.multiBTN){
             startAct2Intent = new Intent(this,multiplayer_mainPage.class);
             startActivity(startAct2Intent);
         }else if(buttonId == R.id.logOffBTN){
             prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
             SharedPreferences.Editor editor = getSharedPreferences(my_pref_name, MODE_PRIVATE).edit();
             editor.clear();
             this.textRef.setText(res.getString(R.string.userLogOff));
             editor.commit();
         }


        String restoredText = prefs.getString(SHuserName, null);
        if(restoredText == null){
            Toast.makeText(getApplicationContext(),res.getString(R.string.error_userNotSigedIn), Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
