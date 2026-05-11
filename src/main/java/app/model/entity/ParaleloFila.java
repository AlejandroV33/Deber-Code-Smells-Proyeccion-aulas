package app.model.entity;

public class ParaleloFila {
    private int id;
    private int idMateria;
    private String materia;
    private Integer idDocente;
    private String docente;
    private String nombre;
    private int numEstudiantes;

    public ParaleloFila() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int idMateria) { this.idMateria = idMateria; }
    public String getMateria() { return materia; }
    public void setMateria(String materia) { this.materia = materia; }
    public Integer getIdDocente() { return idDocente; }
    public void setIdDocente(Integer idDocente) { this.idDocente = idDocente; }
    public String getDocente() { return docente; }
    public void setDocente(String docente) { this.docente = docente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getNumEstudiantes() { return numEstudiantes; }
    public void setNumEstudiantes(int numEstudiantes) { this.numEstudiantes = numEstudiantes; }
}