package app.model.entity;

/**
 * Parameter Object que agrupa los atributos relacionados con la ubicación
 * y características físicas de un aula.
 * 
 * Patrón: Introduce Parameter Object
 * Objetivo: Reducir Long Parameter List en el constructor de Aula
 */
public class UbicacionAula {
    private String edificio;
    private String piso;
    private String numero;
    private int capacidad;

    public UbicacionAula() {}

    public UbicacionAula(String edificio, String piso, String numero, int capacidad) {
        this.edificio = edificio;
        this.piso = piso;
        this.numero = numero;
        this.capacidad = capacidad;
    }

    // Getters y Setters
    public String getEdificio() {
        return edificio;
    }

    public void setEdificio(String edificio) {
        this.edificio = edificio;
    }

    public String getPiso() {
        return piso;
    }

    public void setPiso(String piso) {
        this.piso = piso;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    @Override
    public String toString() {
        return edificio + "/" + piso + "/" + numero + " (" + capacidad + ")";
    }
}
