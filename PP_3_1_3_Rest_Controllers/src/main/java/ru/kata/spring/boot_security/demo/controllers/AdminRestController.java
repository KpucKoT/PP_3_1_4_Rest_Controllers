package ru.kata.spring.boot_security.demo.controllers;//package ru.kata.spring.boot_security.demo.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.AdminServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

//@RestController
//@RequestMapping("/admin")
//@PreAuthorize("hasRole('ROLE_ADMIN')")
//public class AdminRestController {
//
//    private final AdminServiceImpl adminServiceImpl;
//
//    @Autowired
//    public AdminRestController(AdminServiceImpl adminService) {
//        this.adminServiceImpl = adminService;
//    }
//
//    @GetMapping("/info")
//    public ResponseEntity<Map<String, String>> getAdminInfo(Principal principal) {
//        User admin = adminServiceImpl.getUserByUsername(principal.getName()).orElseThrow();
//        Map<String, String> response = new HashMap<>();
//        response.put("username", admin.getUsername());
//        response.put("roles", admin.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<User>> showAllUsers() {
//        return ResponseEntity.ok(adminServiceImpl.getUsers());
//    }
//
////    // Получение пользователя по ID
////    @GetMapping("/{id}")
////    public ResponseEntity<User> getUserById(@PathVariable int id) {
////        User user = adminService.getUser(id);
////        if (user == null) {
////            return ResponseEntity.notFound().build();
////        }
////        return ResponseEntity.ok(user);
////    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<User> getUser(@PathVariable int id) {
//        User user = adminServiceImpl.getUser(id);
//
//        if (user == null) {
//            throw new NoSuchUserException("No user with id: " + id);
//        }
//        return ResponseEntity.ok(user);
//    }
//
//
////    @PostMapping("/users")
////    public ResponseEntity<?> createUser(@RequestBody User user) {
////
////        Set<Role> roles = new HashSet<>(Arrays.asList(
////                new Role(1, "ROLE_ADMIN"),
////                new Role(2, "ROLE_USER")
////        ));
////
////        Set<Integer> roleIds = roles.stream()
////                .map(Role::getId) // Извлекаем ID каждой роли
////                .collect(Collectors.toSet()); // Собираем в Set<Integer>
////
////        user.setRoles(roles);
////
////        return ResponseEntity.ok().build();
////    }
//
//    @PostMapping("/new")
//    public ResponseEntity<?> createUser(@RequestBody User user,
//                                        @RequestParam(value = "roles", required = false) Set<Integer> roles) {
//        try {
//            adminServiceImpl.createUser(user, roles);
//            return ResponseEntity.ok().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateUser(@PathVariable("id") int id,
//                                        @RequestBody User user,
//                                        @RequestParam(value = "role", required = false) Integer role) {
//        try {
//            Set<Integer> roles = (role != null) ? Set.of(role) : Set.of(2); // Роль по умолчанию
//            adminServiceImpl.updateUser(id, user, roles);
//            return ResponseEntity.ok().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteUser(@PathVariable("id") int id) {
//        adminServiceImpl.deleteUser(id);
//        return ResponseEntity.ok().build();
//    }
//
//}

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminRestController {

    private final AdminServiceImpl adminServiceImpl;

    @Autowired
    public AdminRestController(AdminServiceImpl adminService) {
        this.adminServiceImpl = adminService;
    }

    // Получить список всех пользователей
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminServiceImpl.getUsers());
    }

    @GetMapping("/info")
    public ResponseEntity<String> getAdminInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String adminUsername = principal.getName();
        Optional<User> optionalAdmin = adminServiceImpl.getUserByUsername(adminUsername);

        return optionalAdmin.map(admin -> {
            String adminRolesString = adminServiceImpl.getUserRolesString(admin.getId());
            return ResponseEntity.ok("Пользователь " + admin.getUsername() + " с ролью: " + adminRolesString);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found"));
    }

    // Получить пользователя по ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        User user = adminServiceImpl.getUser(id);
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

        User user = adminServiceImpl.getUserByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("age", user.getAge());
        response.put("roles", adminServiceImpl.getUserRolesString(user.getId()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestParam Set<Integer> roles) {
        try {
            User createdUser = adminServiceImpl.createUser(user, roles);
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
            adminServiceImpl.updateUser(id, user, roles);
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

            adminServiceImpl.deleteUser(id); // Метод удаления пользователя

            return ResponseEntity.ok().build();  // Если удаление прошло успешно
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя с id: {}", id, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ошибка при удалении пользователя");
        }
    }
}
