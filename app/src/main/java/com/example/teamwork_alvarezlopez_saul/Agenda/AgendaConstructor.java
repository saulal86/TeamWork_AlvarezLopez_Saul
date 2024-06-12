package com.example.teamwork_alvarezlopez_saul.Agenda;

public class AgendaConstructor {
    private String id;
    private String nombre;
    private String asignatura;
    private String descripcion;
    private String fecha;

    public AgendaConstructor() {
    }

    public AgendaConstructor(String id, String nombre, String asignatura, String descripcion, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.asignatura = asignatura;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
