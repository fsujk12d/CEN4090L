package com.cen4090.mobilympics;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void OnHostGameClick(View view){
        //TODO - Launch HostOptionsDialog
    }

    public void onFindGameClick(View view){
        //TODO - Launch FindOptionsDialog
    }

    public void OnLeaderboardClick(View view){
        //TODO - Launch LeaderboardActivity
    }

    public void OnOptionsClick(View view){
        //TODO - Launch OptionsDialog
        //TODO - NOTE: Use pre-built Preference Screen stuff from chapter 6.9 of the textbook
    }
}