package com.example.teamwork_alvarezlopez_saul.Calendario;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.teamwork_alvarezlopez_saul.R;

import java.util.List;

public class CalendarAdapter extends ArrayAdapter<CalendarConstructor> {
    public CalendarAdapter(Context context, List<CalendarConstructor> projects) {
        super(context, 0, projects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CalendarConstructor calendar_constructor = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_calendar_items, parent, false);
        }

        TextView textViewProjectName = convertView.findViewById(R.id.textViewProjectName);
        TextView textViewSubject = convertView.findViewById(R.id.textViewSubject);
        TextView textViewDescription = convertView.findViewById(R.id.textViewDescription);
        TextView textViewDueDate = convertView.findViewById(R.id.textViewDueDate);


        textViewProjectName.setText(calendar_constructor.getNombre());
        textViewSubject.setText(calendar_constructor.getAsignatura());
        textViewDescription.setText(calendar_constructor.getDescripcion());
        textViewDueDate.setText(calendar_constructor.getFecha());


        return convertView;
    }
}
