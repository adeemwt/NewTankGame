package com.example.adeem.tankgame;

import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;
import com.example.adeem.tankgame.multiplayer_mainPage;
/**
 * Created by adeem on 02/11/2016.
 */

public class ActionListenerHandler  implements WifiP2pManager.ActionListener {
    multiplayer_mainPage activity;
    String actionDisplayText;

    public ActionListenerHandler(multiplayer_mainPage activity, String actionDisplayText){
        this.activity = activity;
        this.actionDisplayText = actionDisplayText;
    }

    @Override
    public void onSuccess() {
        Toast.makeText(activity.getApplicationContext(), actionDisplayText + " Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(activity.getApplicationContext(), actionDisplayText + " Failed", Toast.LENGTH_SHORT).show();
    }
}
