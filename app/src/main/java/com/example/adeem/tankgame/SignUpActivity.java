package com.example.adeem.tankgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
    final Activity Me = this;
    String my_pref_name;
    private Resources res;

    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.confirm = (Button) findViewById(R.id.signupBTN);
        this.nametxt = (TextView) findViewById(R.id.nameTXT);
        this.passtxt = (TextView)  findViewById(R.id.passTXT);
        this.confirmPasstxt = (TextView)  findViewById(R.id.confirmTXT);

        this.confirm.setOnClickListener(this);

        res = getResources();
        my_pref_name = res.getString(R.string.SharedPreferencesPrefsName);

    }

    /*
    *
     */
    @Override
    public void onClick(View v) {
        final String name = this.nametxt.getText().toString();
        final String pass = this.passtxt.getText().toString();
        if(pass.equals(this.confirmPasstxt.getText().toString())){

            mRef = new Firebase("https://tankgameproject-85eb4.firebaseio.com/users/" + name+"/pass/");

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    text = dataSnapshot.getValue(String.class);
                   //String text = dataSnapshot.getValue(String.class);
                    if(text == null){
                        mRef.setValue(pass);
                        Toast.makeText(getApplicationContext(), res.getString(R.string.userAdded), Toast.LENGTH_SHORT).show();

                        prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);

                        SharedPreferences.Editor editor = getSharedPreferences(my_pref_name, MODE_PRIVATE).edit();
                        editor.putString(res.getString(R.string.SharedPreferencesUserName),name);

                        editor.commit();

                        Me.onBackPressed();
                    }

                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

           if(text!=null){
                Toast.makeText(getApplicationContext(),res.getString(R.string.error_usernameAlreadyExists ), Toast.LENGTH_SHORT).show();
            }

        }
        else{
            Toast.makeText(getApplicationContext(),res.getString(R.string.error_passwordsDontMatchs), Toast.LENGTH_SHORT).show();

        }
    }


}
