package com.example.teamwork_alvarezlopez_saul.Calendario;

public class CalendarConstructor {
    private String nombre;
    private String asignatura;
    private String descripcion;
    private String fecha;

    public CalendarConstructor() {
        // Constructor vacío requerido para Firebase
    }

    public CalendarConstructor(String nombre, String asignatura, String descripcion, String fecha) {
        this.nombre = nombre;
        this.asignatura = asignatura;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
