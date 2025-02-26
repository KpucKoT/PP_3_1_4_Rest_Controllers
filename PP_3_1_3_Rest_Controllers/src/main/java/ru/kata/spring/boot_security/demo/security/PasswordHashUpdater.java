//package ru.kata.spring.boot_security.demo.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import ru.kata.spring.boot_security.demo.model.User;
//import ru.kata.spring.boot_security.demo.repository.UserRepository;
//
//import java.util.List;
//
//@Component
//public class PasswordHashUpdater implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Autowired
//    public PasswordHashUpdater(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        List<User> users = userRepository.findAll();
//
//        for (User user : users) {
//            String rawPassword = user.getPassword(); // Получаем исходный пароль
//            String encodedPassword = passwordEncoder.encode(rawPassword); // Хешируем пароль
//            user.setPassword(encodedPassword); // Обновляем пароль пользователя
//            userRepository.save(user); // Сохраняем изменения в БД
//        }
//
//        System.out.println("Все пароли захэшированы BCryptEncoder");
//    }
//}
