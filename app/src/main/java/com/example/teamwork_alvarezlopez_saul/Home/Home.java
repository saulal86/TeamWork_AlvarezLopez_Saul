package com.example.teamwork_alvarezlopez_saul.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.Calendario.CalendarActivity;
import com.example.teamwork_alvarezlopez_saul.Chat.activities.MainActivity;
import com.example.teamwork_alvarezlopez_saul.Notas.Notes;
import com.example.teamwork_alvarezlopez_saul.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;

public class Home extends AppCompatActivity {

    private BottomAppBar bottomAppBar;
    private String userId;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId = getIntent().getStringExtra("userId");
        fab = findViewById(R.id.FAB);
        fab.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finishAffinity();
        });


        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemid = item.getItemId();

        if (itemid == R.id.notas){
            Intent intent = new Intent(getApplicationContext(), Notes.class);
            startActivity(intent);
        }else if(itemid == R.id.agenda){
            Intent intent2 = new Intent(getApplicationContext(), CalendarActivity.class);
            intent2.putExtra("userId", userId);
            startActivity(intent2);
        }else{
            Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
            intent3.putExtra("userId", userId);
            startActivity(intent3);
        }
        return true;
    }
}
