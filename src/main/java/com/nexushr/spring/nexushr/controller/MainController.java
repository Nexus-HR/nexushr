// src/main/java/com/nexushr/spring/nexushr/controller/MainController.java
package com.nexushr.spring.nexushr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}