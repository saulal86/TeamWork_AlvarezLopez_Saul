package com.example.teamwork_alvarezlopez_saul.PDF;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamwork_alvarezlopez_saul.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("record");
    Button botonguardarescribir, botonescribir;
    EditText editTextnombre, editTextQty;
    Spinner spinner;
    String[] listaitems;
    double[] valoritems;
    ArrayAdapter<String> adaptador;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewbyid();


    }

    public void viewbyid(){
        botonguardarescribir = findViewById(R.id.botonguardarescribir);
        botonescribir = findViewById(R.id.botonescribir);
        editTextnombre = findViewById(R.id.editTextnombre);
        editTextQty = findViewById(R.id.editTextQty);
        spinner = findViewById(R.id.spinner);

        listaitems = new String[]{"Petroleo", "Diesel"};
        valoritems = new double[]{72.56, 36.56};
        adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaitems);
        spinner.setAdapter(adaptador);
    }
}