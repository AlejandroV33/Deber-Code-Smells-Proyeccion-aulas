package app.model.entity;

public class Aula {
    private int id;
    private UbicacionAula ubicacion;
    private String estado;
    private String disponibilidad;
    private int idTipoAula;

    // Campos privados mantenidos para compatibilidad con getters/setters individuales
    private String edificio;
    private String piso;
    private String numero;
    private int capacidad;

    public Aula() {}

    /**
     * Constructor refactorizado que usa el patrón Introduce Parameter Object.
     * Reduce el número de parámetros de 8 a 5.
     * 
     * @param id identificador único del aula
     * @param ubicacion objeto que agrupa edificio, piso, número y capacidad
     * @param estado estado actual del aula
     * @param disponibilidad disponibilidad del aula
     * @param idTipoAula identificador del tipo de aula
     */
    public Aula(int id, UbicacionAula ubicacion, String estado, String disponibilidad, int idTipoAula) {
        this.id = id;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.disponibilidad = disponibilidad;
        this.idTipoAula = idTipoAula;
        
        // Mantener sincronizados los campos individuales para compatibilidad
        if (ubicacion != null) {
            this.edificio = ubicacion.getEdificio();
            this.piso = ubicacion.getPiso();
            this.numero = ubicacion.getNumero();
            this.capacidad = ubicacion.getCapacidad();
        }
    }

    /**
     * Constructor antiguo (deprecado) mantenido solo para compatibilidad temporal.
     * Se recomienda usar el nuevo constructor con UbicacionAula.
     * 
     * @deprecated Use Aula(int, UbicacionAula, String, String, int) instead
     */
    @Deprecated(since = "2.0", forRemoval = false)
    public Aula(int id, String edificio, String piso, String numero, int capacidad, String estado, String disponibilidad, int idTipoAula) {
        this.id = id;
        this.edificio = edificio;
        this.piso = piso;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
        this.disponibilidad = disponibilidad;
        this.idTipoAula = idTipoAula;
        // Crear UbicacionAula a partir de los parámetros individuales
        this.ubicacion = new UbicacionAula(edificio, piso, numero, capacidad);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEdificio() { return edificio; }
    public void setEdificio(String edificio) { 
        this.edificio = edificio;
        if (ubicacion != null) ubicacion.setEdificio(edificio);
    }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { 
        this.piso = piso;
        if (ubicacion != null) ubicacion.setPiso(piso);
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { 
        this.numero = numero;
        if (ubicacion != null) ubicacion.setNumero(numero);
    }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { 
        this.capacidad = capacidad;
        if (ubicacion != null) ubicacion.setCapacidad(capacidad);
    }

    //se pueden mover sillas y aumentar la capacidad
    public int getCapacidadFlexible() {
        return (capacidad + 3);
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String disponibilidad) { this.disponibilidad = disponibilidad; }

    public int getIdTipoAula() { return idTipoAula; }
    public void setIdTipoAula(int idTipoAula) { this.idTipoAula = idTipoAula; }

    /**
     * Obtiene el objeto Parameter Object que agrupa ubicación y capacidad.
     * @return UbicacionAula o null si no ha sido inicializado
     */
    public UbicacionAula getUbicacion() { 
        return ubicacion; 
    }

    /**
     * Establece el objeto Parameter Object que agrupa ubicación y capacidad.
     * Sincroniza automáticamente los campos individuales.
     * @param ubicacion el nuevo objeto de ubicación
     */
    public void setUbicacion(UbicacionAula ubicacion) { 
        this.ubicacion = ubicacion;
        if (ubicacion != null) {
            this.edificio = ubicacion.getEdificio();
            this.piso = ubicacion.getPiso();
            this.numero = ubicacion.getNumero();
            this.capacidad = ubicacion.getCapacidad();
        }
    }

    @Override
    public String toString() {
        /*// Si es el objeto fantasma (ID 0), mostramos el texto especial
        if (id == 0) return "--- SIN AULA ---";*/

        // Formato visual normal: E17/P2/E004 (40)
        // Usa ubicacion si está disponible, sino usa los campos individuales
        if (ubicacion != null) {
            return ubicacion.toString();
        }
        return edificio + "/" + piso + "/" + numero + " (" + capacidad + ")";
    }
}