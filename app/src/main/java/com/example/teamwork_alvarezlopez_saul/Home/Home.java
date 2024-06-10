package com.example.teamwork_alvarezlopez_saul.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.Calendario.CalendarActivity;
import com.example.teamwork_alvarezlopez_saul.Chat.activities.MainActivity;
import com.example.teamwork_alvarezlopez_saul.Notas.Notes;
import com.example.teamwork_alvarezlopez_saul.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    private String userId;
    private MenuItem previousMenuItem;
    private TextView itemNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId = getIntent().getStringExtra("userId");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        itemNameTextView = findViewById(R.id.item_name);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item == previousMenuItem && itemNameTextView.getVisibility() == TextView.VISIBLE) {
                    executeMenuItemAction(item.getItemId());
                    itemNameTextView.setVisibility(TextView.GONE);
                    previousMenuItem = null;  // Reset previous item
                } else {
                    showMenuItemName(item);
                    previousMenuItem = item;
                }
                return true;
            }
        });
    }

    private void showMenuItemName(MenuItem item) {
        itemNameTextView.setText(item.getTitle());
        itemNameTextView.setVisibility(TextView.VISIBLE);
    }

    private void executeMenuItemAction(int itemId) {
        if (itemId == R.id.navigation_notas) {
            Intent intent = new Intent(getApplicationContext(), Notes.class);
            startActivity(intent);
        } else if (itemId == R.id.navigation_agenda) {
            Intent intent2 = new Intent(getApplicationContext(), CalendarActivity.class);
            intent2.putExtra("userId", userId);
            startActivity(intent2);
        } else if (itemId == R.id.navigation_chat) {
            Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
            intent3.putExtra("userId", userId);
            startActivity(intent3);
        } else if (itemId == R.id.navigation_logout) {
            FirebaseAuth.getInstance().signOut();
            finishAffinity();
        }
    }
}
