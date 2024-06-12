package com.example.teamwork_alvarezlopez_saul.Agenda;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
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

public class AgendaActivity extends AppCompatActivity {

    private ArrayList<AgendaConstructor> projectList;
    private AgendaAdapter agendaAdapter;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Error: User ID no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        projectList = new ArrayList<>();
        agendaAdapter = new AgendaAdapter(this, projectList);
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(userId).child("proyectos");

        ListView listViewProjects = findViewById(R.id.listViewProjects);
        listViewProjects.setAdapter(agendaAdapter);

        listViewProjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editartarea(position);
            }
        });

        listViewProjects.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                borratarea(position);
                return true;
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                añadirtarea();
            }
        });

        FloatingActionButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton info = findViewById(R.id.info);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muestraalerta("Info", "Dele al botón '+' para añadir una " +
                        "tarea/examen/proyecto a tu agenda, con este ya creado podrá editarlo " +
                        "tocando sobre la tarea y podrá eliminarlo manteniendo pulsado sobre él.");
            }
        });


        cargatareas();
    }

    private void cargatareas() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                projectList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AgendaConstructor proyecto = snapshot.getValue(AgendaConstructor.class);
                    proyecto.setId(snapshot.getKey());
                    projectList.add(proyecto);
                }
                Collections.reverse(projectList);
                agendaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("Error", "Hubo un error al almacenar los datos");
            }
        });
    }

    private void borratarea(int position) {
        AgendaConstructor projectToDelete = projectList.get(position);
        String projectId = projectToDelete.getId();

        databaseReference.child(projectId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AgendaActivity.this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AgendaActivity.this, "Error al eliminar el proyecto", Toast.LENGTH_SHORT).show();
                }
            }
        });

        projectList.remove(position);
        agendaAdapter.notifyDataSetChanged();
    }

    private void añadirtarea() {
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
                dialogfecha(editTextDueDate);
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
                    AgendaConstructor newProject = new AgendaConstructor(projectId, projectName, subject, description, dueDate);
                    databaseReference.child(projectId).setValue(newProject);
                    projectList.add(0, newProject); // Añadir nuevo proyecto al inicio de la lista
                    agendaAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(AgendaActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void editartarea(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_addwork, null);
        builder.setView(dialogView);

        final EditText editTextProjectName = dialogView.findViewById(R.id.editTextProjectName);
        final EditText editTextSubject = dialogView.findViewById(R.id.editTextSubject);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        final EditText editTextDueDate = dialogView.findViewById(R.id.editTextDueDate);

        final AgendaConstructor project = projectList.get(position);

        editTextProjectName.setText(project.getNombre());
        editTextSubject.setText(project.getAsignatura());
        editTextDescription.setText(project.getDescripcion());
        editTextDueDate.setText(project.getFecha());

        editTextDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogfecha(editTextDueDate);
            }
        });

        builder.setTitle("Editar tarea")
                .setPositiveButton("Guardar", null)
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
                    project.setNombre(projectName);
                    project.setAsignatura(subject);
                    project.setDescripcion(description);
                    project.setFecha(dueDate);
                    guardatareaFirebase(project);
                    agendaAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(AgendaActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void dialogfecha(final EditText editTextDueDate) {
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

    private void guardatareaFirebase(AgendaConstructor project) {
        databaseReference.child(project.getId()).setValue(project)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AgendaActivity.this, "Proyecto actualizado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AgendaActivity.this, "Error al actualizar el proyecto", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
