package app.model.entity;

public class Docente {
    private int id;
    private String nombre;
    private String cualquierPizarra; // 'si' o 'no'

    public Docente() {}

    public Docente(int id, String nombre, String cualquierPizarra) {
        this.id = id;
        this.nombre = nombre;
        this.cualquierPizarra = cualquierPizarra;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCualquierPizarra() { return cualquierPizarra; }
    public void setCualquierPizarra(String cualquierPizarra) { this.cualquierPizarra = cualquierPizarra; }

    @Override
    public String toString() {
        return nombre;
    }
}