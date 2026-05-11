package app.model.entity;

public class ParaleloDetalle {
    private int id;
    private String materia;
    private String paralelo;
    private String docente;

    public ParaleloDetalle(int id, String materia, String paralelo, String docente) {
        this.id = id;
        this.materia = materia;
        this.paralelo = paralelo;
        this.docente = docente;
    }

    public int getId() { return id; }
    public String getMateria() { return materia; }
    public String getParalelo() { return paralelo; }
    public String getDocente() { return docente; }

    @Override
    public String toString() {
        return materia + " - " + paralelo + " (" + (docente != null ? docente : "Sin Docente") + ")";
    }
}