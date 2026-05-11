package app.model.entity;

public class TipoAula {
    private int id;
    private String nombre;

    public TipoAula() {}

    public TipoAula(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // util para mostrar en combobox
    @Override
    public String toString() {
        return nombre;
    }
}