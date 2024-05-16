package com.example.teamwork_alvarezlopez_saul.editor;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class EditorTextos extends AppCompatActivity {
    EditText editor, nombrearchivo;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_textos);
        editor = findViewById(R.id.editor);
        nombrearchivo = findViewById(R.id.nombrearchivo);
    }
    public void crear(View v){
        String editortexto = editor.getText().toString();
        String nombre = nombrearchivo.getText().toString();
        try{
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Context.MODE_PRIVATE));
            archivo.write(editortexto);
            archivo.flush();
            archivo.close();
            editor.setText("");
            nombrearchivo.setText("");
            Toast.makeText(this, "El archivo fue creado con exito y sus datos fueron insertados", Toast.LENGTH_SHORT);
        }catch (IOException e){
            Toast.makeText(this, "No se puedo crear el archivo", Toast.LENGTH_SHORT);
        }
    }

    public void editar(View v){
        String nombre = nombrearchivo.getText().toString();
        try{
            InputStreamReader archivo = new InputStreamReader(openFileInput(nombre));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String contenido = "";
            while(linea != null){
                contenido = contenido+=linea+"\n";
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            editor.setText(contenido);
            }catch (IOException e){
            Toast.makeText(this, "No existe el archivo", Toast.LENGTH_SHORT);
        }
    }
}
