package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

@Controller
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping()
    public String index() {
        return "index";
    }

    @GetMapping("/user")
    public String showCurrentUser(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
            } else {
                model.addAttribute("error", "Пользователь не найден");
            }
        } else {
            model.addAttribute("error", "Пользователь не аутентифицирован");
        }
        return "forUser";
    }

}
