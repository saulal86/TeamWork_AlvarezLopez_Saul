package com.example.teamwork_alvarezlopez_saul.Agenda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.teamwork_alvarezlopez_saul.R;

import java.util.List;

public class AgendaAdapter extends ArrayAdapter<AgendaConstructor> {
    public AgendaAdapter(Context context, List<AgendaConstructor> projects) {
        super(context, 0, projects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AgendaConstructor calendar_constructor = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_agenda_items, parent, false);
        }

        TextView nombretarea = convertView.findViewById(R.id.textViewProjectName);
        TextView asignatura = convertView.findViewById(R.id.textViewSubject);
        TextView descripcion = convertView.findViewById(R.id.textViewDescription);
        TextView fecha = convertView.findViewById(R.id.textViewDueDate);

        nombretarea.setText(calendar_constructor.getNombre());
        asignatura.setText(calendar_constructor.getAsignatura());
        descripcion.setText(calendar_constructor.getDescripcion());
        fecha.setText(calendar_constructor.getFecha());

        return convertView;
    }
}
