package com.example.adeem.tankgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;



public class LoginActivity extends AppCompatActivity  implements View.OnClickListener  {



    private SharedPreferences prefs;
    private String my_pref_name;
    private Firebase mRef;

    final  Activity Me = this;
    private AutoCompleteTextView nameView;
    private EditText passwordView;
    private Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameView = (AutoCompleteTextView) findViewById(R.id.signinNametxt);
        passwordView = (EditText) findViewById(R.id.signInPassTXT);

        Button mEmailSignInButton = (Button) findViewById(R.id.signInBTN);
        mEmailSignInButton.setOnClickListener(this);

        res = getResources();
        my_pref_name = res.getString(R.string.SharedPreferencesPrefsName);

    }

    @Override
    public void onClick(View v) {
        final String name = this.nameView.getText().toString();
        final String pass = this.passwordView.getText().toString();

        //Connect to fireBase and check if value already exists
        //if yes allow login
            mRef = new Firebase("https://tankgameproject-85eb4.firebaseio.com/users/" + name+"/pass/");
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String text = dataSnapshot.getValue(String.class);
                    if(text == null){
                        Toast.makeText(getApplicationContext(), res.getString(R.string.error_invalid_user), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(text.equals(pass)) {
                            Toast.makeText(getApplicationContext(), res.getString(R.string.signedIn), Toast.LENGTH_SHORT).show();
                            prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
                            SharedPreferences.Editor editor = getSharedPreferences(my_pref_name, MODE_PRIVATE).edit();
                            editor.putString(res.getString(R.string.SharedPreferencesUserName),name);
                            editor.commit();
                            Me.onBackPressed();
                        }else
                            Toast.makeText(getApplicationContext(), res.getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

    }
}

