package com.example.adeem.tankgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class SignUpActivity extends AppCompatActivity  implements View.OnClickListener{
Firebase mRef;

    Button confirm;
    TextView nametxt;
    TextView passtxt;
    TextView confirmPasstxt;
    SharedPreferences prefs;

    SignUpActivity thisclass = this;

    final Activity Me = this;
    String MY_PREFS_NAME = "usernamePREFS";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.confirm = (Button) findViewById(R.id.signupBTN);
        this.nametxt = (TextView) findViewById(R.id.nameTXT);
        this.passtxt = (TextView)  findViewById(R.id.passTXT);
        this.confirmPasstxt = (TextView)  findViewById(R.id.confirmTXT);

        this.confirm.setOnClickListener(this);
//GIVE ME 5 PLS



    }

    @Override
    public void onClick(View v) {
        final String name = this.nametxt.getText().toString();
        final String pass = this.passtxt.getText().toString();
        if(pass.equals(this.confirmPasstxt.getText().toString())){

            mRef = new Firebase("https://tankgameproject-85eb4.firebaseio.com/" + name + "_pass");//here we copy the url ... so the "users" here is kind of a key that gets a value
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String text = dataSnapshot.getValue(String.class);
                    if(text == null){
                        mRef.setValue(pass);
                        Toast.makeText(getApplicationContext(), "the user was added successfully!! ", Toast.LENGTH_SHORT).show();

                        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("username",name);

                        editor.commit();

                        Me.onBackPressed();
//                   Intent startAct2Intent = new Intent(thisclass, MainActivity.class);
//                    startActivity(startAct2Intent);
                    }
                    else{

                        Toast.makeText(getApplicationContext(), "the username already exists, please try again !", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });


        }
        else{
            Toast.makeText(getApplicationContext(), "passwords dont match, please try again!.", Toast.LENGTH_SHORT).show();

        }
    }
}
