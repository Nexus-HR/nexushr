// src/main/java/com/nexushr/spring/nexushr/model/Usuario.java
package com.nexushr.spring.nexushr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;
    
    @Column(nullable = false)
    private Integer edad;
    
    @Column(nullable = false, unique = true)
    private String cedula;
    
    private LocalDateTime fechaRegistro;
    
    @Enumerated(EnumType.STRING)
    private Rol rol;
    
    public enum Rol {
        ADMIN, USUARIO
    }

    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
        this.rol = Rol.USUARIO;
    }

    public Usuario(String email, String password, String nombre, String apellido, Integer edad, String cedula) {
        this();
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.cedula = cedula;
    }

    // Getters y Setters (AGREGA ESTOS)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}