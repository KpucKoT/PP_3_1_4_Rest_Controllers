package ru.kata.spring.boot_security.demo.controllers;//package ru.kata.spring.boot_security.demo.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.AdminService;
import java.security.Principal;
import java.util.*;



@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminRestController {

    private final AdminService adminService;

    @Autowired
    public AdminRestController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Получить список всех пользователей
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @GetMapping("/info")
    public ResponseEntity<String> getAdminInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String adminUsername = principal.getName();
        Optional<User> optionalAdmin = adminService.getUserByUsername(adminUsername);

        return optionalAdmin.map(admin -> {
            String adminRolesString = adminService.getUserRolesString(admin.getId());
            return ResponseEntity.ok("Пользователь " + admin.getUsername() + " с ролью: " + adminRolesString);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found"));
    }

    // Получить пользователя по ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        User user = adminService.getUser(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = adminService.getUserByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("age", user.getAge());
        response.put("roles", adminService.getUserRolesString(user.getId()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestParam Set<Integer> roles) {
        try {
            User createdUser = adminService.createUser(user, roles);
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Обновить пользователя
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") int id,
                                        @RequestBody User user,
                                        @RequestParam Set<Integer> roles) {
        try {
            adminService.updateUser(id, user, roles);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") int id) {
        try {
            // Логируем попытку удалить пользователя
            log.info("Попытка удалить пользователя с id: {}", id);

            adminService.deleteUser(id); // Метод удаления пользователя

            return ResponseEntity.ok().build();  // Если удаление прошло успешно
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя с id: {}", id, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ошибка при удалении пользователя");
        }
    }
}
