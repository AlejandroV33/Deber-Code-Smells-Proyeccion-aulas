package app.model.entity;

public class MateriaFila {
    private String codigo;
    private int id;
    private String nombre;
    private int semestre;
    private String departamento;
    private int creditos;
    private int horas;
    private int idTipoAulaReq;
    private String tipoAulaReq; // Nombre del tipo

    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = nombre; }
    public int getSemestre() { return semestre; } public void setSemestre(int semestre) { this.semestre = semestre; }
    public String getDepartamento() { return departamento; } public void setDepartamento(String departamento) { this.departamento = departamento; }
    public int getCreditos() { return creditos; } public void setCreditos(int creditos) { this.creditos = creditos; }
    public int getHoras() { return horas; } public void setHoras(int horas) { this.horas = horas; }
    public int getIdTipoAulaReq() { return idTipoAulaReq; } public void setIdTipoAulaReq(int idTipoAulaReq) { this.idTipoAulaReq = idTipoAulaReq; }
    public String getTipoAulaReq() { return tipoAulaReq; } public void setTipoAulaReq(String tipoAulaReq) { this.tipoAulaReq = tipoAulaReq; }
}