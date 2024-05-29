package com.example.teamwork_alvarezlopez_saul.Calendario;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;

import com.example.teamwork_alvarezlopez_saul.R;

public class CalendarActivity extends AppCompatActivity {

    private ArrayList<CalendarConstructor> projectList;
    private CalendarAdapter projectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        projectList = new ArrayList<>();
        projectAdapter = new CalendarAdapter(this, projectList);

        ListView listViewProjects = findViewById(R.id.listViewProjects);
        listViewProjects.setAdapter(projectAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddProjectDialog();
            }
        });
    }

    private void showAddProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_project, null);
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

        builder.setTitle("Agregar Nuevo Proyecto")
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
                    CalendarConstructor newProject = new CalendarConstructor(projectName, subject, description, dueDate);
                    projectList.add(newProject);
                    projectAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    // Manejar campos vacíos
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
