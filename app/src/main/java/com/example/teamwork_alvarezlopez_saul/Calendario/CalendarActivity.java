package com.example.teamwork_alvarezlopez_saul.Calendario;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamwork_alvarezlopez_saul.Notas.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import com.example.teamwork_alvarezlopez_saul.R;

public class CalendarActivity extends AppCompatActivity {

    private ArrayList<CalendarConstructor> projectList;
    private CalendarAdapter projectAdapter;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Error: User ID no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        projectList = new ArrayList<>();
        projectAdapter = new CalendarAdapter(this, projectList);
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(userId).child("proyectos");

        ListView listViewProjects = findViewById(R.id.listViewProjects);
        listViewProjects.setAdapter(projectAdapter);

        listViewProjects.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteProject(position);
                return true;
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddProjectDialog();
            }
        });

        FloatingActionButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Notes.class);
                intent.putExtra("userId", userId); // Pasar userId de vuelta a Notes Activity
                startActivity(intent);
                finish();
            }
        });

        loadProjectsFromFirebase();
    }

    private void loadProjectsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                projectList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CalendarConstructor proyecto = snapshot.getValue(CalendarConstructor.class);
                    proyecto.setId(snapshot.getKey());
                    projectList.add(proyecto);
                }
                Collections.reverse(projectList);
                projectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("Error", "Hubo un error al almacenar los datos");
            }
        });
    }

    private void deleteProject(int position) {
        CalendarConstructor projectToDelete = projectList.get(position);
        String projectId = projectToDelete.getId();

        databaseReference.child(projectId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CalendarActivity.this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CalendarActivity.this, "Error al eliminar el proyecto", Toast.LENGTH_SHORT).show();
                }
            }
        });

        projectList.remove(position);
        projectAdapter.notifyDataSetChanged();
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_addwork, null);
        builder.setView(dialogView);

        final EditText editTextProjectName = dialogView.findViewById(R.id.editTextProjectName);
        final EditText editTextSubject = dialogView.findViewById(R.id.editTextSubject);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        final EditText editTextDueDate = dialogView.findViewById(R.id.editTextDueDate);

        editTextDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editTextDueDate);
            }
        });

        builder.setTitle("Agrega una nueva tarea")
                .setPositiveButton("Agregar", null)
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.cancel());

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String projectName = editTextProjectName.getText().toString();
                String subject = editTextSubject.getText().toString();
                String description = editTextDescription.getText().toString();
                String dueDate = editTextDueDate.getText().toString();

                if (!projectName.isEmpty() && !subject.isEmpty() && !description.isEmpty() && !dueDate.isEmpty()) {
                    String projectId = databaseReference.push().getKey();
                    CalendarConstructor newProject = new CalendarConstructor(projectId, projectName, subject, description, dueDate);
                    databaseReference.child(projectId).setValue(newProject);
                    projectList.add(0, newProject); // Añadir nuevo proyecto al inicio de la lista
                    projectAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(CalendarActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePickerDialog(final EditText editTextDueDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        editTextDueDate.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}
