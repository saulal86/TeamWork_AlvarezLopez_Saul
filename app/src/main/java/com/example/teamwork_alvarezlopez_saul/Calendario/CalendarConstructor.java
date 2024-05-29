package com.example.teamwork_alvarezlopez_saul.Calendario;

public class CalendarConstructor {
    private String nombre;
    private String asignatura;
    private String descripcion;
    private String fecha;

    public CalendarConstructor(String nombre, String asignatura, String descripcion, String fecha) {
        this.nombre = nombre;
        this.asignatura = asignatura;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }
}
