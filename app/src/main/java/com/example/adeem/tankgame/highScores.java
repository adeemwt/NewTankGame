package com.example.adeem.tankgame;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class highScores extends AppCompatActivity implements View.OnClickListener{

    final static String BTNLABALE1 = "multi player";
    final static String BTNLABALE2 = "single player";
    Spinner spinner;
    ToggleButton button;
    long sumOfPlayers =0;
    TextView txt;
    ArrayList<playerHS> players = new ArrayList<>();

    public enum sortModes {
        Escore, Mscore, Hscore,KD
    }
    sortModes sortMode = sortModes.Escore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        txt = (TextView)  findViewById(R.id.testingtxt);
        txt.setText("");
        button = (ToggleButton) findViewById(R.id.highscoresButton);
        spinner = (Spinner) findViewById(R.id.spinnerDiffeculty);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Diff_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        button.setTextOff(BTNLABALE1);
        button.setTextOn(BTNLABALE2);
        button.setText(BTNLABALE2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillSingle();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // sometimes you need nothing here
            }
        });
        getDataFromFireBase();
        fillSingle();

       button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(button.isChecked()) {
            fillSingle();
            spinner.setVisibility(View.VISIBLE);
        } else {
            fillMulti();
            spinner.setVisibility(View.GONE);
        }
    }


    public void fillMulti(){

        txt.setText("number of sign in players: " + sumOfPlayers+"\n\n");

        sortMode = sortModes.KD;
        Collections.sort(players);
        for (playerHS player : players)
            txt.setText(txt.getText() + player.printMultiKD());

    }
    public void fillSingle(){

        Resources res = getResources();
        String[] diffSpinner = res.getStringArray(R.array.Diff_spinner);


        txt.setText("number of sign in players: " + sumOfPlayers+"\n\n");

        if(spinner.getSelectedItem().toString().equals(diffSpinner[0])) {
            sortMode = sortModes.Escore;
            Collections.sort(players);
            for (playerHS player : players)
                txt.setText(txt.getText() + player.printEscore());


        }else  if(spinner.getSelectedItem().toString().equals(diffSpinner[1])) {
            sortMode = sortModes.Mscore;
            Collections.sort(players);
            for (playerHS player : players)
                txt.setText(txt.getText() + player.printMscore());


        }else  if(spinner.getSelectedItem().toString().equals(diffSpinner[2])) {
            sortMode = sortModes.Hscore;
            Collections.sort(players);
            for (playerHS player : players)
                txt.setText(txt.getText() + player.printHscore());

        }
    }
    public void getDataFromFireBase(){

        Firebase ref = new Firebase("https://tankgameproject-85eb4.firebaseio.com/users/");
        // Attach an listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                sumOfPlayers = snapshot.getChildrenCount();

                Resources res = getResources();
                String[] diffSpinner = res.getStringArray(R.array.Diff_spinner);

                for (DataSnapshot postSnapshot: snapshot.getChildren()) { // go over all players
                    String usernameKey =  postSnapshot.getKey();
                    playerHS player = new playerHS(usernameKey);

                    for (DataSnapshot postSnapshot2: postSnapshot.getChildren()) // look for singlePlayer
                    {
                        if(postSnapshot2.getKey().equals("singlePlayer")){
                            for (DataSnapshot postSnapshot3: postSnapshot2.getChildren()){ // look for difficulty

                                if(postSnapshot3.getKey().equals(diffSpinner[0])){
                                    player.setEscore(Integer.parseInt((String)postSnapshot3.getValue()));

                                } else if(postSnapshot3.getKey().equals(diffSpinner[1])){
                                    player.setMscore(Integer.parseInt((String)postSnapshot3.getValue()));

                                } else if(postSnapshot3.getKey().equals(diffSpinner[2])){
                                    player.setHscore(Integer.parseInt((String)postSnapshot3.getValue()));
                                }
                            }

                        } /// else multi
                    }
                    players.add(player);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

    }

    public class playerHS implements Comparable{ // for high score printing only
        int Escore=-1;
        int Mscore=-1;
        int Hscore=-1;
        int multiKill=-1;
        int multiDeth=-1;
        String name;

        public playerHS(String name,int Escore,int Mscore,int Hscore,int multiKill,int multiDeth){
            this.Escore=Escore;
            this.Mscore=Mscore;
            this.Hscore=Hscore;
            this.name=name;
            this.multiKill=multiKill;
            this.multiDeth=multiDeth;
        }
        public playerHS(String name){
            this.name=name;
        }

        public void setEscore(int escore) {
            Escore = escore;
        }

        public void setHscore(int hscore) {
            Hscore = hscore;
        }

        public void setMscore(int mscore) {
            Mscore = mscore;
        }

        public void setMultiDeth(int multiDeth) {
            this.multiDeth = multiDeth;
        }

        public void setMultiKill(int multiKill) {
            this.multiKill = multiKill;
        }

        String printEscore(){
        if(Escore != -1)
            return name+" scored :" + Escore+"\n";
        return "";
        }
        String printMscore(){
            if(Mscore != -1)
                return name+" scored :" + Mscore+"\n";
            return "";
        }
        String printHscore(){
            if(Hscore != -1)
                return name+" scored :" + Hscore+"\n";
            return "";
        }
        String printMultiKD(){
            if((multiKill != -1) && (multiDeth != -1))
                return name+" K =" + multiKill+" D = "+multiDeth+"\n";
            return "";
        }

        @Override
        public int compareTo(Object another) {
            playerHS anotherPlayesHS = (playerHS) another;
            if (sortMode == sortModes.Escore)
                return this.Escore - anotherPlayesHS.Escore;

            else if (sortMode == sortModes.Mscore)
                return this.Mscore - anotherPlayesHS.Mscore;

            else if (sortMode == sortModes.Hscore)
                return this.Hscore - anotherPlayesHS.Hscore;

            else  { //sortMode == sortModes.KD default

                if (this.multiKill != anotherPlayesHS.multiKill)
                    return this.multiKill - anotherPlayesHS.multiKill;
                return anotherPlayesHS.multiDeth - this.multiDeth;
            }
        }
    }

}

