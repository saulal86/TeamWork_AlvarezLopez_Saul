package com.example.teamwork_alvarezlopez_saul.Chat.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

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
    FloatingActionButton botoncontactos, atras, info;
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

        atras = binding.back;

        atras.setOnClickListener(v -> {
            finish();
        });

        info = binding.info;

        info.setOnClickListener(v -> {
            muestraalerta("Info", "Aquí te saldrán las conversaciones recientes con el " +
                    "último mensaje enviado o recibido, tocando sobre el nombre del usuario con " +
                    "el cual tienes esa conversacion reciente podras acceder a su conversación." +
                    "\n" + "Toque sobre el boton '+' para iniciar una nueva conversación con los usuarios de la app.");
        });

        iniciar();
        escuchaconvers();
    }
    private void iniciar() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void escuchaconvers(){
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
                        chatMessage.idenvia = senderId;
                        chatMessage.idrecibe = receiverId;
                        if (preferenceManager.getString(Constantes.KEY_USERS_ID).equals(senderId)) {
                            chatMessage.conversionName = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_ID);
                        } else {
                            chatMessage.conversionName = documentChange.getDocument().getString(Constantes.KEY_SENDER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constantes.KEY_SENDER_ID);
                        }
                        chatMessage.mensaje = documentChange.getDocument().getString(Constantes.KEY_ULTIMO_MENSAJE);
                        chatMessage.fechaObject = documentChange.getDocument().getDate(Constantes.KEY_TIEMPO);
                        conversations.add(chatMessage);
                    }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                        for (int i = 0; i < conversations.size(); i++){
                            String senderId = documentChange.getDocument().getString(Constantes. KEY_SENDER_ID);
                            String receiverId = documentChange.getDocument().getString(Constantes.KEY_RECEIVER_ID);
                            if (conversations.get(i).idenvia.equals(senderId) && conversations.get(i).idrecibe.equals(receiverId)) {
                                conversations.get(i).mensaje = documentChange.getDocument().getString(Constantes.KEY_ULTIMO_MENSAJE);
                                conversations.get(i).fechaObject = documentChange.getDocument().getDate(Constantes.KEY_TIEMPO);
                                break;
                            }
                        }
                    }

                }
                Collections.sort(conversations,(obj1, obj2) ->obj2.fechaObject.compareTo(obj1.fechaObject));
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

    public void muestraalerta(String title, String message) {
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
