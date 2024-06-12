package com.example.teamwork_alvarezlopez_saul.Notas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Notes extends AppCompatActivity {
    EditText editor, nombrearchivo;
    Button botoncrear, botoneditar;

    FloatingActionButton atras, info;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        editor = findViewById(R.id.editor);
        nombrearchivo = findViewById(R.id.nombrearchivo);
        botoncrear = findViewById(R.id.botoncrear);
        botoneditar = findViewById(R.id.botoneditar);
        atras = findViewById(R.id.buttonback);
        info = findViewById(R.id.buttoninfo);

        botoncrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editortexto = editor.getText().toString();
                String nombre = nombrearchivo.getText().toString();
                String userId = auth.getCurrentUser().getUid();
                Map<String, Object> note = new HashMap<>();
                note.put("title", nombre);
                note.put("content", editortexto);
                note.put("userId", userId);

                db.collection("notes")
                        .add(note)
                        .addOnSuccessListener(documentReference -> {
                            editor.setText("");
                            nombrearchivo.setText("");
                            Toast.makeText(Notes.this, "Nota creada con éxito", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(Notes.this, "Error al crear la nota", Toast.LENGTH_SHORT).show());
            }
        });

        botoneditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = nombrearchivo.getText().toString();
                String userId = auth.getCurrentUser().getUid();

                db.collection("notes")
                        .whereEqualTo("title", nombre)
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                DocumentReference docRef = task.getResult().getDocuments().get(0).getReference();
                                docRef.get().addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String content = documentSnapshot.getString("content");
                                        editor.setText(content);
                                    } else {
                                        Toast.makeText(Notes.this, "No existe la nota", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(Notes.this, "No existe la nota", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(Notes.this, "Error al buscar la nota", Toast.LENGTH_SHORT).show());
            }
        });

        atras.setOnClickListener(v -> {
            finish();
        });

        info.setOnClickListener(v -> {
            showAlert("Información", "Pon un nombre a tu nota y creala, después dale " +
                    "la informacion que quieras en el recuadro de escritura y guardala con el botón " +
                    "indicado, luego puedes buscar esa misma nota poniendo su nombre en el campo " +
                    "superior y usando el boton de 'Buscar nota' pudiendo asi editarla.");
        });
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
