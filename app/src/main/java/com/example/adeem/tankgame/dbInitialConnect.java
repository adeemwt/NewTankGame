package com.example.adeem.tankgame;
//this does nothing
import com.firebase.client.Firebase;

/**
 * Created by adeem on 16/09/2016.
 */
public class dbInitialConnect extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
       Firebase.setAndroidContext(this);
    }
}

