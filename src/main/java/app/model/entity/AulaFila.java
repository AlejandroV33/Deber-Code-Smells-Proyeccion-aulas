package app.model.entity;

public class AulaFila {
    private int id;
    private String edificio;
    private String piso;
    private String numero;
    private int capacidad;
    private String estado;
    private int idTipoAula;
    private String tipoAula;

    // Getters y Setters
    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getEdificio() { return edificio; } public void setEdificio(String edificio) { this.edificio = edificio; }
    public String getPiso() { return piso; } public void setPiso(String piso) { this.piso = piso; }
    public String getNumero() { return numero; } public void setNumero(String numero) { this.numero = numero; }
    public int getCapacidad() { return capacidad; } public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public String getEstado() { return estado; } public void setEstado(String estado) { this.estado = estado; }
    public int getIdTipoAula() { return idTipoAula; } public void setIdTipoAula(int idTipoAula) { this.idTipoAula = idTipoAula; }
    public String getTipoAula() { return tipoAula; } public void setTipoAula(String tipoAula) { this.tipoAula = tipoAula; }
}