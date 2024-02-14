package com.example.justintimecycling.ui;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justintimecycling.R;
import com.example.justintimecycling.adapters.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Locale.UK;

public class NavigationInstructions extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView listView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHashMap;
    private TextView etaText, timeText, distanceText;

    private HashMap<Integer, ArrayList<String>> instructions = new HashMap<>();
    private String departureTime = "", arrivalTime = "";
    private int distanceProgress, totalTime;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_instructions_layout);
        listView = findViewById(R.id.exp_list_view);
        etaText = findViewById(R.id.eta_text);
        timeText = findViewById(R.id.time_left_text);
        distanceText = findViewById(R.id.distance_left_text);

        Intent intent = getIntent();

        instructions = (HashMap<Integer, ArrayList<String>>) intent.getSerializableExtra("instructions");
        departureTime = intent.getStringExtra("departureTime");
        totalTime = intent.getIntExtra("totalTime", 0);
        distanceProgress = intent.getIntExtra("distanceProgress", 0);
        arrivalTime = intent.getStringExtra("arrivalTime");

        initializeData();
        showTripProgress();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listHashMap);
        listView.setAdapter(listAdapter);
    }

    private void initializeData() {
        listDataHeader = new ArrayList<>();
        listHashMap = new HashMap<>();

        for(int i = 0; i < instructions.size(); i++)
            if(instructions.get(i).get(0).equals("Train")) {
                listDataHeader.add(instructions.get(i).get(1));
            } else if(instructions.get(i).get(0).equals("Cycle")) {
                listDataHeader.add(instructions.get(i).get(1));
            }

        for(int i = 0;i < instructions.size(); i++) {
            List<String> directions = new ArrayList<>();
            for(int j = 2; j < instructions.get(i).size(); j++) {
                String str = instructions.get(i).get(j);
                directions.add(str);
            }
            listHashMap.put(listDataHeader.get(i), directions);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showTripProgress() {
        String time, distance;

        if(!departureTime.equals(""))
            Toast.makeText(this, "Your first train will depart at " + departureTime, Toast.LENGTH_LONG).show();

        Log.d("fmg", "fg" + totalTime);
        if(totalTime < 0)
            totalTime += 86400;

        int p1 = totalTime % 60;
        int p2 = totalTime / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;
        if(p2!=0)
            time = p2+":"+p3+":"+p1;
        else
            time = "00:"+p3+":"+p1;
        timeText.setText(time);

        if(distanceProgress > 999)
            distance = String.format(UK, "%.2f km", distanceProgress*0.001);
        else
            distance = distanceProgress + "m";

        distanceText.setText(distance);
        etaText.setText(arrivalTime);
    }
}
