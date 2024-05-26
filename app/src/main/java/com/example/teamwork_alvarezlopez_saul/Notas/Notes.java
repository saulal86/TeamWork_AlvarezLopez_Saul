package com.example.teamwork_alvarezlopez_saul.Notas;

import android.content.Context;
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


import com.example.teamwork_alvarezlopez_saul.InicioSesion.LogIn;
import com.example.teamwork_alvarezlopez_saul.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Notes extends AppCompatActivity {
    EditText editor, nombrearchivo;
    Button botoncrear, botoneditar;
    private GestureDetector gestos;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_textos);

        editor = findViewById(R.id.editor);
        nombrearchivo = findViewById(R.id.nombrearchivo);
        botoncrear = findViewById(R.id.botoncrear);
        botoneditar = findViewById(R.id.botoneditar);
        gestos = new GestureDetector(this, (GestureDetector.OnGestureListener) new GestureListener());

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
                    Toast.makeText(Notes.this, "El archivo fue creado con exito y sus datos fueron insertados", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestos.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            FirebaseAuth.getInstance().signOut();
            finishAffinity();
            return true;
        }
    }
}