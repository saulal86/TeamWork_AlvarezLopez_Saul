package com.example.teamwork_alvarezlopez_saul.Chat.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamwork_alvarezlopez_saul.Chat.utilities.Constantes;
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.PreferenceManager;
import com.example.teamwork_alvarezlopez_saul.Chat.models.User;
import com.example.teamwork_alvarezlopez_saul.Chat.listeners.UserListener;
import com.example.teamwork_alvarezlopez_saul.Chat.adapters.UsersAdapter;
import com.example.teamwork_alvarezlopez_saul.databinding.ActivityUsersBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;

    private FloatingActionButton back, info;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.usersRecyclerView.setHasFixedSize(true);

        back = binding.back;

        back.setOnClickListener(v -> {
            finish();
        });

        info = binding.info;

        info.setOnClickListener(v -> {
            showAlert("Info", "Toque sobre un usuario para iniciar una conversación con este.");
        });

        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constantes.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constantes.KEY_USERS_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.email = queryDocumentSnapshot.getString(Constantes.KEY_EMAIL);
                            user.token = queryDocumentSnapshot.getString(Constantes.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }


    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constantes.KEY_USER, user);
        startActivity(intent);
        finish();
    }

    public void showAlert(String title, String message) {
        if (!isFinishing() && !isDestroyed()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message);
            alertDialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

}
