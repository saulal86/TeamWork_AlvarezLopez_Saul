package com.example.teamwork_alvarezlopez_saul.Chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.teamwork_alvarezlopez_saul.Chat.models.ChatMessage;
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.Constantes;
import com.example.teamwork_alvarezlopez_saul.Chat.listeners.ConversionListener;
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.PreferenceManager;
import com.example.teamwork_alvarezlopez_saul.Chat.adapters.RecentConversationsAdapter;
import com.example.teamwork_alvarezlopez_saul.Chat.models.User;
import com.example.teamwork_alvarezlopez_saul.databinding.ActivityMainChatBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainChatBinding binding;
    private PreferenceManager preferenceManager;
    FloatingActionButton botoncontactos;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        botoncontactos = binding.fabNewChat;

        botoncontactos.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(intent);
        });

        init();
        listenConversations();
    }
    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenConversations(){
        database.collection(Constantes.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constantes.KEY_SENDER_ID, preferenceManager.getString(Constantes.KEY_USERS_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constantes.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constantes.KEY_RECEIVER_ID, preferenceManager.getString(Constantes.KEY_USERS_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
            if(value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        String senderId = documentChange.getDocument().getString(Constantes.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_ID);
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = senderId;
                        chatMessage.receiverId = receiverId;
                        if (preferenceManager.getString(Constantes.KEY_USERS_ID).equals(senderId)) {
                            chatMessage.conversionName = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_ID);
                        } else {
                            chatMessage.conversionName = documentChange.getDocument().getString(Constantes.KEY_SENDER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constantes.KEY_SENDER_ID);
                        }
                        chatMessage.message = documentChange.getDocument().getString(Constantes.KEY_LAST_MESSAGE);
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constantes.KEY_TIMESTAMP);
                        conversations.add(chatMessage);
                    }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                        for (int i = 0; i < conversations.size(); i++){
                            String senderId = documentChange.getDocument().getString(Constantes. KEY_SENDER_ID);
                            String receiverId = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_ID);
                            if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                                conversations.get(i).message = documentChange.getDocument().getString(Constantes.KEY_LAST_MESSAGE);
                                conversations.get(i).dateObject = documentChange.getDocument().getDate(Constantes.KEY_TIMESTAMP);
                                break;
                            }
                        }
                    }

                }
                Collections.sort(conversations,(obj1, obj2) ->obj2.dateObject.compareTo(obj1.dateObject));
                conversationsAdapter.notifyDataSetChanged();
                binding. conversationsRecyclerView.smoothScrollToPosition(0);
                binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }
        };

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constantes.KEY_USER, user);
        startActivity(intent);
    }
}
