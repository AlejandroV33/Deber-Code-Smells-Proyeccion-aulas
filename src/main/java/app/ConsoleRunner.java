package app;

import app.model.dao.HorarioDAO;
import app.model.service.ExcelService;
import app.model.service.SimulatedAnnealingService;
import app.util.DatabaseConnection;
import app.model.dao.VistaResultadoDAO;

import java.util.Scanner;

public class ConsoleRunner {

    public static void main(String[] args) {
        DatabaseConnection.init();

        Scanner scanner = new Scanner(System.in);
        SimulatedAnnealingService algoritmo = new SimulatedAnnealingService();
        HorarioDAO horarioDAO = new HorarioDAO();
        ExcelService excelService = new ExcelService();

        while (true) {
            System.out.println("\n=== CONSOLA DE PRUEBAS FIQA ===");
            System.out.println("1. Resetear todas las asignaciones (LIBERAR AULAS)");
            System.out.println("2. Ejecutar Algoritmo de Asignacion");
            System.out.println("3. Generar Reporte Excel");
            System.out.println("4. Salir");
            System.out.print("Opcion: ");

            int op = scanner.nextInt();

            switch (op) {
                case 1:
                    horarioDAO.limpiarAsignaciones();
                    System.out.println("Todas las aulas han sido liberadas.");
                    break;
                case 2:
                    long start = System.currentTimeMillis();
//                    algoritmo.ejecutarMejorDeTres();
                    long end = System.currentTimeMillis();
                    System.out.println("Ejecucion finalizada en " + (end - start) + " ms");
                    break;
                case 3:
                    String path = System.getProperty("user.home") + "/Desktop/Reporte_Aulas_FIQA.xlsx";
                    VistaResultadoDAO vistaDAO = new VistaResultadoDAO();
                    excelService.generarReporte(path, vistaDAO.listarResultados());
                    System.out.println("Reporte guardado en escritorio.");
                    break;
                case 4:
                    System.exit(0);
            }
        }
    }
}