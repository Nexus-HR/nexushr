// src/main/java/com/nexushr/spring/nexushr/repository/EmpleadoRepository.java
package com.nexushr.spring.nexushr.repository;

import com.nexushr.spring.nexushr.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findByDepartamento(String departamento);
    List<Empleado> findByEstado(Empleado.EstadoEmpleado estado);
    boolean existsByEmail(String email);
}