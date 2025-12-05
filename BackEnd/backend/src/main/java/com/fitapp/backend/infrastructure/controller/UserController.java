package com.fitapp.backend.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "User Management", description = "Endpoints para gestionar usuarios")
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {


}