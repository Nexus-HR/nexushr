// src/main/java/com/nexushr/spring/nexushr/repository/UsuarioRepository.java
package com.nexushr.spring.nexushr.repository;

import com.nexushr.spring.nexushr.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCedula(String cedula);
    boolean existsByEmail(String email);
    boolean existsByCedula(String cedula);
}