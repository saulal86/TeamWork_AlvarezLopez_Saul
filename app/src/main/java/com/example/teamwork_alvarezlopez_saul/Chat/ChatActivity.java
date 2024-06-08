package com.example.teamwork_alvarezlopez_saul.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.databinding.ActivityChatBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        init();
        setListeners();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                preferenceManager.getString(Constantes.KEY_USERS_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        if (receiverUser != null) {
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constantes.KEY_SENDER_ID, preferenceManager.getString(Constantes.KEY_USERS_ID));
            message.put(Constantes.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constantes.KEY_MESSAGE, binding.inputMessage.getText().toString());
            message.put(Constantes.KEY_TIMESTAMP, new Date());
            database.collection(Constantes.KEY_COLLECTION_CHAT).add(message);
            binding.inputMessage.setText(null);
        } else {
            // Maneja el caso en que receiverUser es nulo
            Log.e("ChatActivity", "Receiver user is null");
        }
    }

    private void listenMessages(){
        if (receiverUser == null) {
            Log.e("ChatActivity", "Receiver user is null in listenMessages");
            return;
        }
        database.collection(Constantes.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constantes.KEY_SENDER_ID, preferenceManager.getString(Constantes.KEY_USERS_ID))
                .whereEqualTo(Constantes.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constantes.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constantes.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constantes.KEY_RECEIVER_ID, preferenceManager.getString(Constantes.KEY_USERS_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constantes.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constantes.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constantes.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constantes.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            chatMessages.sort((obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(count, chatMessages.size() - count);
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constantes.KEY_USER);
        if (receiverUser != null) {
            binding.textName.setText(receiverUser.email);
        } else {
            // Maneja el caso en que receiverUser es nulo
            binding.textName.setText("User not found");
        }
    }

    private void setListeners() {
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}
