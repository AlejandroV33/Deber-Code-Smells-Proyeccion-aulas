package app.model.entity;

public class Paralelo {
    private int id;
    private String nombre;
    private int numEstudiantesMatriculados;
    private int idMateria; // fk
    private int idDocente; // fk
    private String espejo; // 'si', 'no', 'indiferente'

    public Paralelo() {}

    public Paralelo(int id, String nombre, int numEstudiantesMatriculados, int idMateria, int idDocente, String espejo) {
        this.id = id;
        this.nombre = nombre;
        this.numEstudiantesMatriculados = numEstudiantesMatriculados;
        this.idMateria = idMateria;
        this.idDocente = idDocente;
        this.espejo = espejo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getNumEstudiantesMatriculados() { return numEstudiantesMatriculados; }
    public void setNumEstudiantesMatriculados(int numEstudiantesMatriculados) { this.numEstudiantesMatriculados = numEstudiantesMatriculados; }

    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int idMateria) { this.idMateria = idMateria; }

    public int getIdDocente() { return idDocente; }
    public void setIdDocente(int idDocente) { this.idDocente = idDocente; }

    public String getEspejo() { return espejo; }
    public void setEspejo(String espejo) { this.espejo = espejo; }
}