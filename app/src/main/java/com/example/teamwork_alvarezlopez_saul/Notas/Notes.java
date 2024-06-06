package com.example.teamwork_alvarezlopez_saul.Notas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.Calendario.CalendarActivity;
import com.example.teamwork_alvarezlopez_saul.Chat.MainActivity;
import com.example.teamwork_alvarezlopez_saul.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Notes extends AppCompatActivity {
    EditText editor, nombrearchivo;
    Button botoncrear, botoneditar;
    FloatingActionButton infoButton;
    private GestureDetector gestos;
    private String userId; // Variable para almacenar userId

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        editor = findViewById(R.id.editor);
        nombrearchivo = findViewById(R.id.nombrearchivo);
        botoncrear = findViewById(R.id.botoncrear);
        botoneditar = findViewById(R.id.botoneditar);
        infoButton = findViewById(R.id.info);
        gestos = new GestureDetector(this, new GestureListener());

        // Obtener los datos de la intención
        userId = getIntent().getStringExtra("userId");

        botoncrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editortexto = editor.getText().toString();
                String nombre = nombrearchivo.getText().toString();
                try {
                    OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Context.MODE_PRIVATE));
                    archivo.write(editortexto);
                    archivo.flush();
                    archivo.close();
                    editor.setText("");
                    nombrearchivo.setText("");
                    Toast.makeText(Notes.this, "El archivo fue creado con éxito y sus datos fueron insertados", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(Notes.this, "No se pudo crear el archivo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        botoneditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = nombrearchivo.getText().toString();
                try {
                    InputStreamReader archivo = new InputStreamReader(openFileInput(nombre));
                    BufferedReader br = new BufferedReader(archivo);
                    String linea = br.readLine();
                    StringBuilder contenido = new StringBuilder();
                    while (linea != null) {
                        contenido.append(linea).append("\n");
                        linea = br.readLine();
                    }
                    br.close();
                    archivo.close();
                    editor.setText(contenido.toString());
                } catch (IOException e) {
                    Toast.makeText(Notes.this, "No existe el archivo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoAlert("Información", "-Si presionas dos veces en el fondo de la aplicación cerrarando así sesión y también la aplicación\n" +
                        "\n-Si deslizas de derecha a izquierda accederás al calendario de actividades");
            }
        });

        FloatingActionButton calendario = findViewById(R.id.calendario);
        calendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        FloatingActionButton chat = findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }

    private void showInfoAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Aceptar", null);
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSignOutAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                finishAffinity();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestos.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            showSignOutAlert("Aviso", "¿Estás seguro que quieres cerrar sesión?");
            return true;
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            if(e2.getX() < e1.getX()){
                openCalendarActivity();
            }
            return true;
        }
    }

    private void openCalendarActivity() {
        Intent intent = new Intent(this, CalendarActivity.class);
        intent.putExtra("userId", userId); // Pasar el userId
        startActivity(intent);
        finish();
    }
}