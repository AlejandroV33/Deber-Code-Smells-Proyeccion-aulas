package app.model.entity;

public class Horario {
    private int id;
    private String dia;
    private int horaInicio;
    private int horaFin;
    private int idParalelo;
    private Integer idAula;

    // Nuevos campos de métricas
    private String proporcionOcupacion;
    private double indiceOcupacion;
    private double indiceAjusteOcupacion;

    public Horario() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }

    public int getHoraInicio() { return horaInicio; }
    public void setHoraInicio(int horaInicio) { this.horaInicio = horaInicio; }

    public int getHoraFin() { return horaFin; }
    public void setHoraFin(int horaFin) { this.horaFin = horaFin; }

    public int getIdParalelo() { return idParalelo; }
    public void setIdParalelo(int idParalelo) { this.idParalelo = idParalelo; }

    public Integer getIdAula() { return idAula; }
    public void setIdAula(Integer idAula) { this.idAula = idAula; }

    public String getProporcionOcupacion() { return proporcionOcupacion; }
    public void setProporcionOcupacion(String proporcionOcupacion) { this.proporcionOcupacion = proporcionOcupacion; }

    public double getIndiceOcupacion() { return indiceOcupacion; }
    public void setIndiceOcupacion(double indiceOcupacion) { this.indiceOcupacion = indiceOcupacion; }

    public double getIndiceAjusteOcupacion() { return indiceAjusteOcupacion; }
    public void setIndiceAjusteOcupacion(double indiceAjusteOcupacion) { this.indiceAjusteOcupacion = indiceAjusteOcupacion; }
}