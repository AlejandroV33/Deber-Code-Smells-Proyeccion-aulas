package app.model.entity;

public class HorarioFila {
    private int idParalelo;
    private int idMateria;
    private String materia;
    private String paralelo;
    private int matriculados;

    private Integer idAula; // puede ser null
    private String aulaDesc; // ej: E17 - P1 - 101
    private String tipoAulaReq;
    private int capacidadAula;
    private String docente;

    // guardamos los objetos completos de horario para poder editarlos (saber su id real)
    private Horario horarioLunes;
    private Horario horarioMartes;
    private Horario horarioMiercoles;
    private Horario horarioJueves;
    private Horario horarioViernes;
    private Horario horarioSabado;

    // constructores, getters y setters
    public HorarioFila() {}

    public int getIdParalelo() { return idParalelo; }
    public void setIdParalelo(int idParalelo) { this.idParalelo = idParalelo; }

    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int idMateria) { this.idMateria = idMateria; }

    public String getMateria() { return materia; }
    public void setMateria(String materia) { this.materia = materia; }

    public String getParalelo() { return paralelo; }
    public void setParalelo(String paralelo) { this.paralelo = paralelo; }

    public int getMatriculados() { return matriculados; }
    public void setMatriculados(int matriculados) { this.matriculados = matriculados; }

    public Integer getIdAula() { return idAula; }
    public void setIdAula(Integer idAula) { this.idAula = idAula; }

    public String getAulaDesc() { return aulaDesc; }
    public void setAulaDesc(String aulaDesc) { this.aulaDesc = aulaDesc; }

    public String getTipoAulaReq() { return tipoAulaReq; }
    public void setTipoAulaReq(String tipoAulaReq) { this.tipoAulaReq = tipoAulaReq; }

    public int getCapacidadAula() { return capacidadAula; }
    public void setCapacidadAula(int capacidadAula) { this.capacidadAula = capacidadAula; }

    public Horario getHorarioLunes() { return horarioLunes; }
    public void setHorarioLunes(Horario horarioLunes) { this.horarioLunes = horarioLunes; }

    public Horario getHorarioMartes() { return horarioMartes; }
    public void setHorarioMartes(Horario horarioMartes) { this.horarioMartes = horarioMartes; }

    public Horario getHorarioMiercoles() { return horarioMiercoles; }
    public void setHorarioMiercoles(Horario horarioMiercoles) { this.horarioMiercoles = horarioMiercoles; }

    public Horario getHorarioJueves() { return horarioJueves; }
    public void setHorarioJueves(Horario horarioJueves) { this.horarioJueves = horarioJueves; }

    public Horario getHorarioViernes() { return horarioViernes; }
    public void setHorarioViernes(Horario horarioViernes) { this.horarioViernes = horarioViernes; }

    public Horario getHorarioSabado() { return horarioSabado; }
    public void setHorarioSabado(Horario horarioSabado) { this.horarioSabado = horarioSabado; }

    public String getDocente() { return docente; }
    public void setDocente(String docente) { this.docente = docente; }

    // metodos helper para mostrar el texto en la tabla (ej. "07-09")
    public String getLunesText() { return horarioLunes != null ? horarioLunes.getHoraInicio() + "-" + horarioLunes.getHoraFin() : ""; }
    public String getMartesText() { return horarioMartes != null ? horarioMartes.getHoraInicio() + "-" + horarioMartes.getHoraFin() : ""; }
    public String getMiercolesText() { return horarioMiercoles != null ? horarioMiercoles.getHoraInicio() + "-" + horarioMiercoles.getHoraFin() : ""; }
    public String getJuevesText() { return horarioJueves != null ? horarioJueves.getHoraInicio() + "-" + horarioJueves.getHoraFin() : ""; }
    public String getViernesText() { return horarioViernes != null ? horarioViernes.getHoraInicio() + "-" + horarioViernes.getHoraFin() : ""; }
    public String getSabadoText() { return horarioSabado != null ? horarioSabado.getHoraInicio() + "-" + horarioSabado.getHoraFin() : ""; }
}