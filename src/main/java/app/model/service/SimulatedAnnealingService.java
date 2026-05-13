package app.model.service;

import app.model.dao.AulaDAO;
import app.model.dao.HorarioDAO;
import app.model.dao.HorarioDAO.HorarioDTO;
import app.model.entity.Aula;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimulatedAnnealingService {

    private final HorarioDAO horarioDAO = new HorarioDAO();
    private final AulaDAO aulaDAO = new AulaDAO();

    // parametros annealing ajustados
    private static final double TEMP_INICIAL = 1.0;
    private static final double TEMP_FINAL = 0.001;
    private static final double COOLING_RATE = 0.995;
    private static final int ITERACIONES_POR_TEMP = 1000;
//    private static final double W_DIST = 0.05;
    // Pesos de Energía
    private static final double W_DIST_ESTUDIANTES = 0.08;
    private static final double W_DIST_DOCENTES = 0.3;
    private static final double W_HARD_DOCENTES = 2.0; // Multiplicador para las reglas inquebrantables

    public void ejecutarMejorDeTres(Consumer<String> logger) {
        logger.accept("--- iniciando algoritmo de asignacion (mejor de 3) ---");

        List<HorarioDTO> todosHorarios = horarioDAO.listarParaAlgoritmo();
        List<Aula> todasAulas = aulaDAO.listar().stream()
                .filter(a -> "activo".equalsIgnoreCase(a.getEstado()))
                .toList();

        List<HorarioDTO> especiales = new ArrayList<>();
        List<HorarioDTO> comunes = new ArrayList<>();

        for (HorarioDTO h : todosHorarios) {
            if (esComun(h.getNombreTipoAula())) comunes.add(h);
            else especiales.add(h);
        }

        logger.accept("horarios especiales: " + especiales.size());
        logger.accept("horarios comunes: " + comunes.size());

        // fase 1: determinista
        asignarEspeciales(especiales, todasAulas, logger);

        // fase 2: annealing 3 veces
        logger.accept(">> fase 2: iniciando 3 intentos de simulated annealing...");
        List<Aula> aulasComunes = todasAulas.stream()
                .filter(a -> a.getIdTipoAula() == 1)
                .toList();

        double mejorEnergiaGlobal = Double.MAX_VALUE;
        Map<Integer, Integer> mejorConfiguracion = new HashMap<>();

        for (int intento = 1; intento <= 3; intento++) {
            logger.accept("> ejecutando intento " + intento + "...");

            // limpiar asignaciones comunes en memoria para este intento
            for (HorarioDTO h : comunes) h.setIdAulaAsignada(null);

//            double energiaFinal = ejecutarUnIntentoAnnealing(comunes, aulasComunes, todasAulas);
            double energiaFinal = ejecutarUnIntentoAnnealing(comunes, todosHorarios, aulasComunes, todasAulas);
            logger.accept("  energia obtenida en intento " + intento + ": " + String.format("%.4f", energiaFinal));

            if (energiaFinal < mejorEnergiaGlobal) {
                mejorEnergiaGlobal = energiaFinal;
                // guardar mapa de la mejor configuracion
                for (HorarioDTO h : comunes) {
                    mejorConfiguracion.put(h.getId(), h.getIdAulaAsignada());
                }
            }
        }

        logger.accept(">> seleccionando mejor escenario con energia: " + String.format("%.4f", mejorEnergiaGlobal));

        // aplicar la mejor configuracion a los objetos en memoria
        for (HorarioDTO h : comunes) {
            h.setIdAulaAsignada(mejorConfiguracion.get(h.getId()));
        }

        // guardar todo en bd
        guardarResultados(todosHorarios, todasAulas, logger);
    }

    private boolean esComun(String tipo) {
        return tipo != null && tipo.equalsIgnoreCase("comun");
    }

    private void asignarEspeciales(List<HorarioDTO> horarios, List<Aula> aulas, Consumer<String> logger) {
        logger.accept(">> asignando aulas especiales...");

        for (HorarioDTO h : horarios) {
            Aula mejorAula = null;
            double menorCosto = Double.MAX_VALUE;

            List<Aula> candidatas = aulas.stream()
                    .filter(a -> a.getIdTipoAula() == h.getIdTipoAulaReq())
                    .filter(a -> a.getCapacidadFlexible() >= h.getMatriculados())
                    .toList();

            for (Aula a : candidatas) {
                if (estaOcupada(a.getId(), h, horarios)) continue;

                double costo = OptimizationMetrics.calcularIndiceAjuste(h.getMatriculados(), a.getCapacidad());
                if (costo < menorCosto) {
                    menorCosto = costo;
                    mejorAula = a;
                }
            }

            if (mejorAula != null) {
                h.setIdAulaAsignada(mejorAula.getId());
            } else {
//                logger.accept(String.format("alerta (sin aula especial): dia: %s, hora: %d-%d, paralelo: %s, matriculados: %d, materia: %s, docente: %s, tipo req: %s",
//                        h.getDia(), h.getHoraInicio(), h.getHoraFin(), h.getParalelo(), h.getMatriculados(), h.getMateria(), h.getDocente(), h.getNombreTipoAula()));
                logger.accept(String.format("alerta (sin aula especial) para el horario: | %s | %s | %s | %s | %d-%d | matriculados: %d | tipo de aula requerida: %s",
                        h.getDocente(), h.getMateria(), h.getParalelo(), h.getDia(), h.getHoraInicio(), h.getHoraFin(), h.getMatriculados(), h.getNombreTipoAula()));
            }
        }
    }

    private double ejecutarUnIntentoAnnealing(List<HorarioDTO> comunes, List<HorarioDTO> todosHorarios, List<Aula> aulasComunes, List<Aula> todasAulas) {
        generarSolucionInicial(comunes, aulasComunes);

        double temperatura = TEMP_INICIAL;
        double energiaActual = calcularEnergiaTotal(todosHorarios, todasAulas); // Usamos todos

        Random rand = new Random();

        while (temperatura > TEMP_FINAL) {
            for (int i = 0; i < ITERACIONES_POR_TEMP; i++) {
                int idx = rand.nextInt(comunes.size());
                HorarioDTO h = comunes.get(idx);
                Integer aulaOriginal = h.getIdAulaAsignada();

                Aula nuevaAula = aulasComunes.get(rand.nextInt(aulasComunes.size()));

                if (nuevaAula.getCapacidad() < h.getMatriculados()) continue;

                h.setIdAulaAsignada(nuevaAula.getId());

                if (hayColision(h, comunes)) { // Colisiones se buscan solo en comunes
                    h.setIdAulaAsignada(aulaOriginal);
                    continue;
                }

                double nuevaEnergia = calcularEnergiaTotal(todosHorarios, todasAulas); // Usamos todos
                double delta = nuevaEnergia - energiaActual;

                if (delta < 0) {
                    energiaActual = nuevaEnergia;
                } else {
                    if (Math.exp(-delta / temperatura) > rand.nextDouble()) {
                        energiaActual = nuevaEnergia;
                    } else {
                        h.setIdAulaAsignada(aulaOriginal);
                    }
                }
            }
            temperatura *= COOLING_RATE;
        }
        return energiaActual;
    }

    private void generarSolucionInicial(List<HorarioDTO> horarios, List<Aula> aulas) {
        for (HorarioDTO h : horarios) {
                List<Aula> validas = aulas.stream()
                    .filter(a -> a.getCapacidadFlexible() >= h.getMatriculados())
                    .collect(Collectors.toList());

            Collections.shuffle(validas);

            for (Aula a : validas) {
                h.setIdAulaAsignada(a.getId());
                if (!hayColision(h, horarios)) break;
                h.setIdAulaAsignada(null);
            }
        }
    }

    private double calcularEnergiaTotal(List<HorarioDTO> todosHorarios, List<Aula> aulasMap) {
        double energiaOcupacion = 0;
        double energiaDistanciaEst = 0;
        double energiaDistanciaDoc = 0;
        int asignados = 0;

        Map<Integer, Aula> mapAulas = aulasMap.stream().collect(Collectors.toMap(Aula::getId, a -> a));

        for (HorarioDTO h : todosHorarios) {
            if (h.getIdAulaAsignada() == null) {
                if(esComun(h.getNombreTipoAula())) energiaOcupacion += 10.0;
                continue;
            }

            Aula a = mapAulas.get(h.getIdAulaAsignada());
            energiaOcupacion += OptimizationMetrics.calcularIndiceAjuste(h.getMatriculados(), a.getCapacidad());
            energiaDistanciaEst += calcularDistanciaEstudiantes(h, todosHorarios, mapAulas);
            energiaDistanciaDoc += calcularDistanciaDocentes(h, todosHorarios, mapAulas);
            asignados++;
        }

        // Evaluamos las Reglas Fuertes (Mismo Piso / Anclaje Lab)
        double energiaReglasDuras = calcularPenalizacionesDurasDocente(todosHorarios, mapAulas);

        if (asignados == 0) return 10000;

        return (energiaOcupacion / asignados)
                + (W_DIST_ESTUDIANTES * (energiaDistanciaEst / asignados))
                + (W_DIST_DOCENTES * (energiaDistanciaDoc / asignados))
                + (W_HARD_DOCENTES * energiaReglasDuras); // Castigo severo si rompe las reglas
    }

    // ==========================================
    // NUEVO: EVALUACIÓN DE REGLAS FUERTES
    // ==========================================
    private double calcularPenalizacionesDurasDocente(List<HorarioDTO> todosHorarios, Map<Integer, Aula> mapAulas) {
        double penalizacion = 0;

        // Agrupar horarios por docente (ignorando vacíos)
        Map<String, List<HorarioDTO>> porDocente = todosHorarios.stream()
            .filter(h -> h.getDocente() != null && !h.getDocente().equalsIgnoreCase("Sin profesor") && h.getIdAulaAsignada() != null)
            .collect(Collectors.groupingBy(HorarioDTO::getDocente));

        for (List<HorarioDTO> horDocente : porDocente.values()) {
            List<HorarioDTO> especiales = new ArrayList<>();
            List<HorarioDTO> comunes = new ArrayList<>();

            for(HorarioDTO h : horDocente) {
                if(esComun(h.getNombreTipoAula())) comunes.add(h);
                else especiales.add(h);
            }

            if (comunes.isEmpty()) continue;

                Map<String, List<HorarioDTO>> comunesPorMateria = comunes.stream()
                    .collect(Collectors.groupingBy(HorarioDTO::getMateria));

            Set<String> pisosDeTodasLasMaterias = new HashSet<>();

            // REGLA 1: Todos los paralelos/horas de UNA misma materia deben darse en el mismo edificio y piso
            for (List<HorarioDTO> horariosMateria : comunesPorMateria.values()) {
                Set<String> pisosDeEstaMateria = new HashSet<>();
                for (HorarioDTO h : horariosMateria) {
                    Aula a = mapAulas.get(h.getIdAulaAsignada());
                    pisosDeEstaMateria.add(a.getEdificio() + "|" + a.getPiso());
                }

                // Si la materia se dispersa en más de 1 piso, gran penalización
                if (pisosDeEstaMateria.size() > 1) {
                    penalizacion += 50.0 * (pisosDeEstaMateria.size() - 1);
                }
                pisosDeTodasLasMaterias.addAll(pisosDeEstaMateria);
            }

            // REGLA 2: Anclaje al Laboratorio
            if (!especiales.isEmpty()) {
                Set<String> pisosLaboratorios = new HashSet<>();
                for (HorarioDTO h : especiales) {
                    Aula a = mapAulas.get(h.getIdAulaAsignada());
                    pisosLaboratorios.add(a.getEdificio() + "|" + a.getPiso());
                }

                boolean anclajeExitoso = false;
                for (String pisoLab : pisosLaboratorios) {
                    if (pisosDeTodasLasMaterias.contains(pisoLab)) {
                        anclajeExitoso = true; // Al menos una materia está en el mismo piso que uno de sus labs
                        break;
                    }
                }

                // Si ninguna materia coincide con el piso de sus laboratorios
                if (!anclajeExitoso) {
                    penalizacion += 100.0;
                }
            }
        }
        return penalizacion;
    }

    // ==========================================
    // REGLA SUAVE: DISTANCIA ESTUDIANTES Y DOCENTES
    // ==========================================
    private double calcularDistanciaEstudiantes(HorarioDTO actual, List<HorarioDTO> todos, Map<Integer, Aula> aulas) {
        double penalizacion = 0;
        for (HorarioDTO otro : todos) {
            if (otro == actual || otro.getSemestre() != actual.getSemestre() || otro.getIdAulaAsignada() == null || !otro.getDia().equals(actual.getDia())) continue;
            if (otro.getHoraFin() == actual.getHoraInicio()) {
                Aula a1 = aulas.get(otro.getIdAulaAsignada());
                Aula a2 = aulas.get(actual.getIdAulaAsignada());
                penalizacion += OptimizationMetrics.calcularPenalizacionDistancia(a1.getEdificio(), a2.getEdificio());
            }
        }
        return penalizacion;
    }

    private double calcularDistanciaDocentes(HorarioDTO actual, List<HorarioDTO> todos, Map<Integer, Aula> aulas) {
        if (actual.getDocente() == null || actual.getDocente().equalsIgnoreCase("Sin profesor") || actual.getDocente().isEmpty()) return 0;

        double penalizacion = 0;
        for (HorarioDTO otro : todos) {
            if (otro == actual || otro.getIdAulaAsignada() == null || !otro.getDia().equals(actual.getDia())) continue;

            // Si tiene clases seguidas
            if (otro.getHoraFin() == actual.getHoraInicio() && actual.getDocente().equals(otro.getDocente())) {
                Aula a1 = aulas.get(otro.getIdAulaAsignada());
                Aula a2 = aulas.get(actual.getIdAulaAsignada());

                if (a1.getId() == a2.getId()) {
                    penalizacion += 0; // Ideal: No se mueve
                } else if (a1.getEdificio().equals(a2.getEdificio())) {
                    if (a1.getPiso().equals(a2.getPiso())) {
                        penalizacion += 2; // Mismo piso (leve)
                    } else {
                        penalizacion += 10; // Mismo edificio, distinto piso (medio)
                    }
                } else {
                    penalizacion += 30; // Distinto edificio (grave)
                }
            }
        }
        return penalizacion;
    }

    // Antiguo calcularDistanciaSoft renombrado
    /*private double calcularDistanciaEstudiantes(HorarioDTO actual, List<HorarioDTO> todos, Map<Integer, Aula> aulas) {
        double penalizacion = 0;
        for (HorarioDTO otro : todos) {
            if (otro == actual || otro.semestre != actual.semestre || otro.idAulaAsignada == null || !otro.dia.equals(actual.dia)) continue;

            if (otro.horaFin == actual.horaInicio) {
                Aula a1 = aulas.get(otro.idAulaAsignada);
                Aula a2 = aulas.get(actual.idAulaAsignada);
                penalizacion += OptimizationMetrics.calcularPenalizacionDistancia(a1.getEdificio(), a2.getEdificio());
            }
        }
        return penalizacion;
    }*/

    // NUEVO: Penalización escalonada para el docente
    /*private double calcularDistanciaDocentes(HorarioDTO actual, List<HorarioDTO> todos, Map<Integer, Aula> aulas) {
        // Ignoramos si no tiene docente o si es el genérico
        if (actual.docente == null || actual.docente.equalsIgnoreCase("Sin profesor") || actual.docente.isEmpty()) {
            return 0;
        }

        double penalizacion = 0;
        for (HorarioDTO otro : todos) {
            // Ignorar el mismo registro, los que no tienen aula, o los de otro día
            if (otro == actual || otro.idAulaAsignada == null || !otro.dia.equals(actual.dia)) continue;

            // Si la clase del "otro" termina exactamente cuando empieza la "actual"
            // Y ambas clases son impartidas por el mismo profesor
            if (otro.horaFin == actual.horaInicio && actual.docente.equals(otro.docente)) {
                Aula a1 = aulas.get(otro.idAulaAsignada); // Aula de la clase anterior
                Aula a2 = aulas.get(actual.idAulaAsignada); // Aula de la clase que va a empezar

                if (a1.getId() == a2.getId()) {
                    penalizacion += 0; // Escenario ideal: se queda en la misma aula
                } else if (a1.getEdificio().equals(a2.getEdificio())) {
                    if (a1.getPiso().equals(a2.getPiso())) {
                        penalizacion += 2; // Mismo edificio, mismo piso: Penalización leve
                    } else {
                        penalizacion += 10; // Mismo edificio, distinto piso: Penalización media
                    }
                } else {
                    penalizacion += 30; // Distinto edificio: Penalización grave
                }
            }
        }
        return penalizacion;
    }*/

    /*private double calcularDistanciaSoft(HorarioDTO actual, List<HorarioDTO> todos, Map<Integer, Aula> aulas) {
        double penalizacion = 0;
        for (HorarioDTO otro : todos) {
            if (otro == actual || otro.semestre != actual.semestre || otro.idAulaAsignada == null || !otro.dia.equals(actual.dia)) continue;

            if (otro.horaFin == actual.horaInicio) {
                Aula a1 = aulas.get(otro.idAulaAsignada);
                Aula a2 = aulas.get(actual.idAulaAsignada);
                penalizacion += OptimizationMetrics.calcularPenalizacionDistancia(a1.getEdificio(), a2.getEdificio());
            }
        }
        return penalizacion;
    }*/

    private boolean estaOcupada(int idAula, HorarioDTO actual, List<HorarioDTO> lista) {
        for (HorarioDTO h : lista) {
            if (h == actual) continue;
            if (h.getIdAulaAsignada() != null && h.getIdAulaAsignada() == idAula && h.getDia().equals(actual.getDia())) {
                if (actual.getHoraInicio() < h.getHoraFin() && actual.getHoraFin() > h.getHoraInicio()) return true;
            }
        }
        return false;
    }

    private boolean hayColision(HorarioDTO actual, List<HorarioDTO> lista) {
        if (actual.getIdAulaAsignada() == null) return false;
        return estaOcupada(actual.getIdAulaAsignada(), actual, lista);
    }

    private void guardarResultados(List<HorarioDTO> horarios, List<Aula> aulas, Consumer<String> logger) {
        logger.accept(">> guardando resultados definitivos en base de datos...");
        Map<Integer, Aula> mapAulas = aulas.stream().collect(Collectors.toMap(Aula::getId, a -> a));

        for (HorarioDTO h : horarios) {
            if (h.getIdAulaAsignada() != null) {
                Aula a = mapAulas.get(h.getIdAulaAsignada());
                String prop = h.getMatriculados() + "/" + a.getCapacidad();
                double idxOcup = (double) h.getMatriculados() / a.getCapacidad();
                double idxAjuste = OptimizationMetrics.calcularIndiceAjuste(h.getMatriculados(), a.getCapacidad());

                horarioDAO.actualizarAsignacion(h.getId(), h.getIdAulaAsignada(), prop, idxOcup, idxAjuste);
                horarioDAO.actualizarAsignacion(h.getId(), h.getIdAulaAsignada(), prop, idxOcup, idxAjuste);
            }
        }
        logger.accept(">> guardado completo y exitoso.");
    }
}