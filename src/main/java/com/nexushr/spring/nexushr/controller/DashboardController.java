// src/main/java/com/nexushr/spring/nexushr/controller/DashboardController.java
package com.nexushr.spring.nexushr.controller;

import com.nexushr.spring.nexushr.model.Empleado;
import com.nexushr.spring.nexushr.model.Usuario;
import com.nexushr.spring.nexushr.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("nuevoEmpleado", new Empleado());
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
    public String eliminarEmpleado(@PathVariable @NonNull Long id,  // CORRECCIÓN AQUÍ
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

    // ... el resto del código se mantiene igual ...
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

    private Map<String, Object> calcularEstadisticasReales(List<Empleado> empleados) {
        Map<String, Object> stats = new HashMap<>();
        
        if (empleados == null || empleados.isEmpty()) {
            stats.put("salarioTotal", 0.0);
            stats.put("salarioPromedio", 0.0);
            stats.put("totalEmpleados", 0);
            stats.put("empleadosPorDepto", new HashMap<>());
            stats.put("desempenioPorDepto", new HashMap<>());
            return stats;
        }
        
        double salarioTotal = empleados.stream()
            .filter(e -> e.getSalario() != null)
            .mapToDouble(Empleado::getSalario)
            .sum();
        
        double salarioPromedio = empleados.stream()
            .filter(e -> e.getSalario() != null)
            .mapToDouble(Empleado::getSalario)
            .average()
            .orElse(0.0);
        
        Map<String, Long> empleadosPorDepto = empleados.stream()
            .filter(e -> e.getDepartamento() != null && !e.getDepartamento().isEmpty())
            .collect(Collectors.groupingBy(Empleado::getDepartamento, Collectors.counting()));
        
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
        
        if (empleados == null || empleados.isEmpty()) {
            return empleadosConTareas;
        }
        
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
            
            List<Map<String, String>> tareasEmpleado = new ArrayList<>();
            int numTareas = random.nextInt(3) + 2;
            
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
            .filter(e -> departamento != null && departamento.equals(e.getDepartamento()))
            .collect(Collectors.toList());
        
        if (empDepto.isEmpty()) return 0.0;
        
        double desempenio = empDepto.stream()
            .mapToDouble(e -> {
                double base = 70.0;
                if (e.getSalario() != null && e.getSalario() > 30000) base += 15;
                if (e.getFechaContratacion() != null) {
                    base += 15;
                }
                return Math.min(base, 100.0);
            })
            .average()
            .orElse(70.0);
        
        return desempenio;
    }

    private double calcularDesempenioIndividual(List<Map<String, String>> tareas) {
        if (tareas == null || tareas.isEmpty()) return 0.0;
        
        long tareasCompletadas = tareas.stream()
            .filter(t -> "COMPLETADA".equals(t.get("estado")))
            .count();
        
        double porcentajeCompletadas = (double) tareasCompletadas / tareas.size() * 100;
        
        return Math.min(porcentajeCompletadas, 100.0);
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