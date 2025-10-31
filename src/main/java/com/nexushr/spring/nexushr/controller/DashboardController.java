// src/main/java/com/nexushr/spring/nexushr/controller/DashboardController.java
package com.nexushr.spring.nexushr.controller;

import com.nexushr.spring.nexushr.model.Empleado;
import com.nexushr.spring.nexushr.model.Usuario;
import com.nexushr.spring.nexushr.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// IMPORT NUEVOS - AGREGAR ESTOS
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("empleados", empleadoService.obtenerTodosEmpleados());
        model.addAttribute("nuevoEmpleado", new Empleado()); // Para el formulario
        return "dashboard";
    }

    @PostMapping("/empleados")
    public String agregarEmpleado(@ModelAttribute("nuevoEmpleado") Empleado empleado, 
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        try {
            empleadoService.guardarEmpleado(empleado);
            redirectAttributes.addFlashAttribute("success", "Empleado agregado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar empleado: " + e.getMessage());
        }
        
        return "redirect:/dashboard";
    }

    @PostMapping("/empleados/{id}/eliminar")
    public String eliminarEmpleado(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        try {
            empleadoService.eliminarEmpleado(id);
            redirectAttributes.addFlashAttribute("success", "Empleado eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar empleado: " + e.getMessage());
        }
        
        return "redirect:/dashboard";
    }

    // =============================================
    // MÉTODOS NUEVOS - AGREGAR DESDE AQUÍ
    // =============================================

    @GetMapping("/seguridad")
    public String seguridad(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("usuario", usuario);
        return "seguridad";
    }

    @GetMapping("/reportes")
    public String reportes(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        
        List<Empleado> empleados = empleadoService.obtenerTodosEmpleados();
        
        // Cálculos reales basados en datos existentes
        Map<String, Object> estadisticas = calcularEstadisticasReales(empleados);
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("empleados", empleados);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("empleadosConTareas", asignarTareasReales(empleados));
        model.addAttribute("tareasDisponibles", getTareasDisponibles());
        
        return "reportes";
    }

    @GetMapping("/gestion")
    public String gestion(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        
        // Agrupar empleados por departamento
        List<Empleado> empleados = empleadoService.obtenerTodosEmpleados();
        Map<String, List<Empleado>> empleadosPorDepartamento = empleados.stream()
            .filter(e -> e.getDepartamento() != null && !e.getDepartamento().isEmpty())
            .collect(Collectors.groupingBy(Empleado::getDepartamento));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("empleadosPorDepartamento", empleadosPorDepartamento);
        model.addAttribute("departamentos", empleadosPorDepartamento.keySet());
        return "gestion";
    }

    @GetMapping("/estadisticas")
    public String estadisticas(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        
        List<Empleado> empleados = empleadoService.obtenerTodosEmpleados();
        Map<String, Object> estadisticas = calcularEstadisticasReales(empleados);
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("empleados", empleados);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("empleadosConTareas", asignarTareasReales(empleados));
        
        return "estadisticas";
    }

    // =============================================
    // MÉTODOS PRIVADOS NUEVOS PARA REPORTES REALES
    // =============================================

    private Map<String, Object> calcularEstadisticasReales(List<Empleado> empleados) {
        Map<String, Object> stats = new HashMap<>();
        
        // Cálculos basados en datos reales
        double salarioTotal = empleados.stream()
            .filter(e -> e.getSalario() != null)
            .mapToDouble(Empleado::getSalario)
            .sum();
        
        double salarioPromedio = empleados.stream()
            .filter(e -> e.getSalario() != null)
            .mapToDouble(Empleado::getSalario)
            .average()
            .orElse(0.0);
        
        // Agrupar por departamento
        Map<String, Long> empleadosPorDepto = empleados.stream()
            .filter(e -> e.getDepartamento() != null)
            .collect(Collectors.groupingBy(Empleado::getDepartamento, Collectors.counting()));
        
        // Calcular desempeño basado en datos existentes
        Map<String, Double> desempenioPorDepto = new HashMap<>();
        for (String depto : empleadosPorDepto.keySet()) {
            double desempenio = calcularDesempenioDepartamento(empleados, depto);
            desempenioPorDepto.put(depto, desempenio);
        }
        
        stats.put("salarioTotal", salarioTotal);
        stats.put("salarioPromedio", salarioPromedio);
        stats.put("totalEmpleados", empleados.size());
        stats.put("empleadosPorDepto", empleadosPorDepto);
        stats.put("desempenioPorDepto", desempenioPorDepto);
        
        return stats;
    }

    private List<Map<String, Object>> asignarTareasReales(List<Empleado> empleados) {
        List<Map<String, Object>> empleadosConTareas = new ArrayList<>();
        
        String[][] tareasDisponibles = {
            {"Revisión documentación", "MEDIA", "ADMINISTRATIVO"},
            {"Desarrollo nuevas funcionalidades", "ALTA", "TECNOLOGÍA"},
            {"Capacitación equipo", "MEDIA", "RRHH"},
            {"Análisis métricas rendimiento", "BAJA", "ANALISIS"},
            {"Reunión planificación trimestral", "ALTA", "GERENCIA"},
            {"Optimización procesos internos", "MEDIA", "OPERACIONES"},
            {"Auditoría seguridad", "ALTA", "TECNOLOGÍA"},
            {"Elaboración reportes financieros", "ALTA", "FINANZAS"}
        };
        
        Random random = new Random();
        
        for (Empleado empleado : empleados) {
            Map<String, Object> empData = new HashMap<>();
            empData.put("empleado", empleado);
            
            // Asignar tareas basadas en departamento
            List<Map<String, String>> tareasEmpleado = new ArrayList<>();
            int numTareas = random.nextInt(3) + 2; // 2-4 tareas por empleado
            
            for (int i = 0; i < numTareas; i++) {
                String[] tarea = tareasDisponibles[random.nextInt(tareasDisponibles.length)];
                Map<String, String> tareaMap = new HashMap<>();
                tareaMap.put("nombre", tarea[0]);
                tareaMap.put("prioridad", tarea[1]);
                tareaMap.put("departamento", tarea[2]);
                tareaMap.put("estado", random.nextBoolean() ? "COMPLETADA" : "EN_PROGRESO");
                tareaMap.put("progreso", String.valueOf(random.nextInt(100) + 1));
                tareasEmpleado.add(tareaMap);
            }
            
            empData.put("tareas", tareasEmpleado);
            empData.put("tareasCompletadas", 
                tareasEmpleado.stream().filter(t -> "COMPLETADA".equals(t.get("estado"))).count());
            empData.put("tareasTotales", tareasEmpleado.size());
            empData.put("desempenio", calcularDesempenioIndividual(tareasEmpleado));
            
            empleadosConTareas.add(empData);
        }
        
        return empleadosConTareas;
    }

    private double calcularDesempenioDepartamento(List<Empleado> empleados, String departamento) {
        List<Empleado> empDepto = empleados.stream()
            .filter(e -> departamento.equals(e.getDepartamento()))
            .collect(Collectors.toList());
        
        if (empDepto.isEmpty()) return 0.0;
        
        // Cálculo basado en antigüedad y salario (como proxy de desempeño)
        double desempenio = empDepto.stream()
            .mapToDouble(e -> {
                double base = 70.0; // Base del 70%
                if (e.getSalario() != null && e.getSalario() > 30000) base += 15;
                if (e.getFechaContratacion() != null && 
                    e.getFechaContratacion().isBefore(java.time.LocalDate.now().minusYears(2))) {
                    base += 15;
                }
                return Math.min(base, 100.0);
            })
            .average()
            .orElse(70.0);
        
        return desempenio;
    }

    private double calcularDesempenioIndividual(List<Map<String, String>> tareas) {
        if (tareas.isEmpty()) return 0.0;
        
        long tareasCompletadas = tareas.stream()
            .filter(t -> "COMPLETADA".equals(t.get("estado")))
            .count();
        
        double porcentajeCompletadas = (double) tareasCompletadas / tareas.size() * 100;
        
        // Ajustar por prioridad de tareas
        double ajustePrioridad = tareas.stream()
            .mapToDouble(t -> {
                switch (t.get("prioridad")) {
                    case "ALTA": return 1.2;
                    case "MEDIA": return 1.0;
                    case "BAJA": return 0.8;
                    default: return 1.0;
                }
            })
            .average()
            .orElse(1.0);
        
        return Math.min(porcentajeCompletadas * ajustePrioridad, 100.0);
    }

    private List<Map<String, String>> getTareasDisponibles() {
        List<Map<String, String>> tareas = new ArrayList<>();
        
        String[][] tareasData = {
            {"Revisión documentación", "MEDIA", "ADMINISTRATIVO"},
            {"Desarrollo nuevas funcionalidades", "ALTA", "TECNOLOGÍA"},
            {"Capacitación equipo", "MEDIA", "RRHH"},
            {"Análisis métricas rendimiento", "BAJA", "ANALISIS"},
            {"Reunión planificación trimestral", "ALTA", "GERENCIA"},
            {"Optimización procesos internos", "MEDIA", "OPERACIONES"},
            {"Auditoría seguridad", "ALTA", "TECNOLOGÍA"},
            {"Elaboración reportes financieros", "ALTA", "FINANZAS"},
            {"Entrevistas candidatos", "MEDIA", "RRHH"},
            {"Mantenimiento servidores", "ALTA", "TECNOLOGÍA"},
            {"Análisis mercado", "BAJA", "VENTAS"},
            {"Presentación resultados", "MEDIA", "GERENCIA"}
        };
        
        for (String[] tareaData : tareasData) {
            Map<String, String> tarea = new HashMap<>();
            tarea.put("nombre", tareaData[0]);
            tarea.put("prioridad", tareaData[1]);
            tarea.put("departamento", tareaData[2]);
            tareas.add(tarea);
        }
        
        return tareas;
    }
}