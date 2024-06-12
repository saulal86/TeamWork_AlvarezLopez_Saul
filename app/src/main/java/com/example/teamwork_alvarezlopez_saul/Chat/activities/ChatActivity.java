package com.example.teamwork_alvarezlopez_saul.Chat.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.teamwork_alvarezlopez_saul.Chat.models.ChatMessage;
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.Constantes;
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.PreferenceManager;
import com.example.teamwork_alvarezlopez_saul.Chat.models.User;
import com.example.teamwork_alvarezlopez_saul.Chat.adapters.ChatAdapter;
import com.example.teamwork_alvarezlopez_saul.R;
import com.example.teamwork_alvarezlopez_saul.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private boolean isReceiverAvailable = false;

    private FloatingActionButton atras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        init();
        setListeners();
        listenMessages();
        atras = findViewById(R.id.imageBack);
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }
        );
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
            message.put(Constantes.KEY_MENSAJE, binding.inputMessage.getText().toString());
            message.put(Constantes.KEY_TIEMPO, new Date());
            database.collection(Constantes.KEY_COLLECTION_CHAT).add(message);
            if (conversionId != null) {
                updateConversion(binding.inputMessage.getText().toString());
            }else{
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constantes.KEY_SENDER_ID, preferenceManager.getString(Constantes.KEY_USERS_ID));
                conversion.put(Constantes.KEY_SENDER_NAME, preferenceManager.getString(Constantes.KEY_EMAIL));
                conversion.put(Constantes.KEY_RECEIVER_ID,receiverUser.id);
                conversion.put(Constantes.KEY_RECEIVER_NAME,receiverUser.email);
                conversion.put(Constantes.KEY_ULTIMO_MENSAJE,binding.inputMessage.getText().toString());
                conversion.put(Constantes.KEY_TIEMPO, new Date());
                addConversion(conversion);
            }
            binding.inputMessage.setText(null);
        }
    }

    private void listenAvailabilityOfReceiver(){
        database.collection(Constantes. KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constantes.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constantes.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constantes.KEY_FCM_TOKEN);
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }

        });
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
                    chatMessage.idenvia = documentChange.getDocument().getString(Constantes.KEY_SENDER_ID);
                    chatMessage.idrecibe = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_ID);
                    chatMessage.mensaje = documentChange.getDocument().getString(Constantes.KEY_MENSAJE);
                    chatMessage.tiempo = getReadableDateTime(documentChange.getDocument().getDate(Constantes.KEY_TIEMPO));
                    chatMessage.fechaObject = documentChange.getDocument().getDate(Constantes.KEY_TIEMPO);
                    chatMessages.add(chatMessage);
                }
            }
            chatMessages.sort((obj1, obj2) -> obj1.fechaObject.compareTo(obj2.fechaObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(count, chatMessages.size() - count);
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
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

    private void updateConversion(String message) {
        DocumentReference documentReference =
                database.collection(Constantes.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constantes.KEY_ULTIMO_MENSAJE, message,
                Constantes.KEY_TIEMPO, new Date()
        );
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constantes.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void checkForConversion() {
        if(chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constantes.KEY_USERS_ID),
                    receiverUser.id
            );

            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constantes.KEY_USERS_ID)

            );

        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constantes.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constantes.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constantes.KEY_RECEIVER_ID, receiverId)
                .get().addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful()&& task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}
