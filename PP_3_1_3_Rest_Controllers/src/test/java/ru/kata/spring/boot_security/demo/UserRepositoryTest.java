package ru.kata.spring.boot_security.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindById() {
        int userId = 19; // Замените на реальный ID
        Optional<User> userOptional = userRepository.findById(userId);

        assertTrue(userOptional.isPresent(), "Пользователь не найден");
        assertNotNull(userOptional.get().getRoles(), "Роли пользователя не загружены");
    }
}