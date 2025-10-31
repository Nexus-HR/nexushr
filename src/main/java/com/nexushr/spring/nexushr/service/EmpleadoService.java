// src/main/java/com/nexushr/spring/nexushr/service/EmpleadoService.java
package com.nexushr.spring.nexushr.service;

import com.nexushr.spring.nexushr.model.Empleado;
import com.nexushr.spring.nexushr.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public List<Empleado> obtenerTodosEmpleados() {
        return empleadoRepository.findAll();
    }

    public Optional<Empleado> obtenerEmpleadoPorId(@NonNull Long id) {
        return empleadoRepository.findById(id);
    }

    public Empleado guardarEmpleado(Empleado empleado) {
        if (empleadoRepository.existsByEmail(empleado.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        return empleadoRepository.save(empleado);
    }

    public void eliminarEmpleado(@NonNull Long id) {
        empleadoRepository.deleteById(id);
    }

    public List<Empleado> obtenerEmpleadosPorDepartamento(String departamento) {
        return empleadoRepository.findByDepartamento(departamento);
    }
}