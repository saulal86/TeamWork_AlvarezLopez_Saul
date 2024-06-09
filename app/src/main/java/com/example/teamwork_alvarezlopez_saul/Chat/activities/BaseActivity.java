package com.example.teamwork_alvarezlopez_saul.Chat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.Chat.utilities.Constantes;
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constantes.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constantes.KEY_USERS_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constantes.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constantes.KEY_AVAILABILITY, 1);
    }
}
