package com.example.adeem.tankgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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



    SharedPreferences prefs;
    String MY_PREFS_NAME = "usernamePREFS";
    Firebase mRef;
    LoginActivity thisclass = this;
    // UI references.

   final  Activity Me = this;
    private AutoCompleteTextView nameView;
    private EditText passwordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        nameView = (AutoCompleteTextView) findViewById(R.id.signinNametxt);
        passwordView = (EditText) findViewById(R.id.signInPassTXT);

        Button mEmailSignInButton = (Button) findViewById(R.id.signInBTN);
        mEmailSignInButton.setOnClickListener(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


    }

    @Override
    public void onClick(View v) {
        final String name = this.nameView.getText().toString();
        final String pass = this.passwordView.getText().toString();
            boolean flag= false;

            mRef = new Firebase("https://tankgameproject-85eb4.firebaseio.com/" + name + "_pass");//here we copy the url ... so the "users" here is kind of a key that gets a value
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String text = dataSnapshot.getValue(String.class);
                    if(text == null){
                        Toast.makeText(getApplicationContext(), "the user does not exist! ", Toast.LENGTH_SHORT).show();
                    }

                    else{
                        if(text.equals(pass)) {
                            Toast.makeText(getApplicationContext(), "currect sign In ", Toast.LENGTH_SHORT).show();


                            prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString("username",name);

                            editor.commit();

                            Me.onBackPressed();
//                            Intent startAct2Intent = new Intent(thisclass, MainActivity.class);
//                            startActivity(startAct2Intent);
                        }else
                            Toast.makeText(getApplicationContext(), "NOPE WRONG PASS", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

    }
}

