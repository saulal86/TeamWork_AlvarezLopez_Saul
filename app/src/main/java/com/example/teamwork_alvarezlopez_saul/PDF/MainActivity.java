package com.example.teamwork_alvarezlopez_saul.PDF;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamwork_alvarezlopez_saul.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("record");
    InfoObjetos infoobj = new InfoObjetos();
    Button botonguardarescribir, botonescribir;
    EditText editTextnombre, editTextcantidad;
    Spinner spinner;
    String[] listaitems;
    double[] valoritems;
    ArrayAdapter<String> adaptador;
    long invoiceNo = 0;
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
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

        callOnClickListener();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                invoiceNo =snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void callOnClickListener() {
        botonguardarescribir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoobj.invoiceNo = invoiceNo +1;
                infoobj.customerName = String.valueOf(editTextnombre.getText());
                infoobj.date = new Date().getTime();
                infoobj.fuelType = spinner.getSelectedItem().toString();
                infoobj.fuelQty = Double.parseDouble(String.valueOf(editTextnombre.getText()));
                infoobj.customerName = String.valueOf(Double.parseDouble(String.valueOf(editTextcantidad.getText())));
                infoobj.amount = Double.valueOf(decimalFormat.format(infoobj.getFuelQty()*valoritems[spinner.getSelectedItemPosition()]));

                myRef.child(String.valueOf(invoiceNo+1)).setValue(infoobj);

            }
        });
    }

    public void viewbyid(){
        botonguardarescribir = findViewById(R.id.botonguardarescribir);
        botonescribir = findViewById(R.id.botonescribir);
        editTextnombre = findViewById(R.id.editTextnombre);
        editTextcantidad = findViewById(R.id.editTextCantidad);
        spinner = findViewById(R.id.spinner);

        listaitems = new String[]{"Petroleo", "Diesel"};
        valoritems = new double[]{72.56, 36.56};
        adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaitems);
        spinner.setAdapter(adaptador);
    }
}