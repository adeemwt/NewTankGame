package com.example.adeem.tankgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Iterator;

public class highScores extends AppCompatActivity implements View.OnClickListener{

    Spinner spinner;
  //  ExpandableListView  PlayerList;
    ToggleButton button;

    TextView txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        txt = (TextView)  findViewById(R.id.testingtxt);
        txt.setText("");
        button = (ToggleButton) findViewById(R.id.highscoresButton);
      //  PlayerList = (ExpandableListView) findViewById(R.id.highscoresListView) ;
        spinner = (Spinner) findViewById(R.id.spinnerDiffeculty);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Diff_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
//

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

        fillSingle();

       button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(button.isChecked())
        fillSingle();
        else
            fillMulti();
    }


    public void fillMulti(){

    }
    public void fillSingle(){

        Firebase ref = new Firebase("https://tankgameproject-85eb4.firebaseio.com/users/");
        // Attach an listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                txt.setText("There are " + snapshot.getChildrenCount() + " blog posts");
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
//                    BlogPost post = postSnapshot.getValue(BlogPost.class);
//                    System.out.println(post.getAuthor() + " - " + post.getTitle());
      //           txt.setText(txt.getText()+"\n" + postSnapshot.getValue(String.class)); mabe youll go learn for yor exam and illl finish it? adeem?
                    String usernameKey =  postSnapshot.getKey();
                    txt.setText(txt.getText()+"\n" +usernameKey);
                    Iterator it  =postSnapshot.getChildren().iterator();

                    for (DataSnapshot postSnapshot2: postSnapshot.getChildren())
                    {
                        if(postSnapshot2.getKey().equals("singlePlayer")){
                            for (DataSnapshot postSnapshot3: postSnapshot2.getChildren()){
                                if(postSnapshot3.getKey().equals(spinner.getSelectedItem().toString())){
                                    txt.setText(txt.getText() +" the score : " + postSnapshot3.getValue());
                                }
                            }
                        }
                    }
//                    Firebase ref2 = new Firebase("https://tankgameproject-85eb4.firebaseio.com/users/" + usernameKey+"/signlePlayer/" + spinner.getSelectedItem().toString()  );
//                    ref2.addValueEventListener(new ValueEventListener() {
//                                                   @Override
//                                                   public void onDataChange(DataSnapshot dataSnapshot) {
//                                                       if(dataSnapshot.getValue(String.class)!=null);
//                                                       txt.setText(txt.getText() + " the score is " +dataSnapshot.getValue(String.class) );
//
//                                                   }
//
//                                                   @Override
//                                                   public void onCancelled(FirebaseError firebaseError) {
//
//                                                   }
//                                               }
//                    );
//                    while(it.hasNext()) {
//                        Object element = it.next();
//                        if()
//                        System.out.print(element + " ");
//                    }


                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


}

