package ru.kata.spring.boot_security.demo.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminServiceImpl implements AdminService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // Метод для получения строки ролей пользователя
    @Override
    @Transactional
    public String getUserRolesString(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        Hibernate.initialize(user.getRoles());

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            return user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(", "));
        }
        return "Нет ролей";
    }

    @Override
    @Transactional
    public Optional<User> getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с именем " + username + " не найден"));

        Hibernate.initialize(user.getRoles());

        return Optional.of(user);
    }

    @Override
    @Transactional
    public User getUser(int userId) {
        log.debug("Получение пользователя с ID: {}", userId);
        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElseThrow(() -> new RuntimeException("Пользователь не найден1"));
        log.debug("Пользователь найден: {}", user);
        Hibernate.initialize(user.getRoles());
        log.debug("Роли пользователя инициализированы");
        return user;
    }

    @Override
    @Transactional
    public List<User> getUsers() {
        return userRepository.findAllWithRoles();
    }

    @Override
    @Transactional
    public User createUser(User user, Set<Integer> roleNames) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Этот пользователь " + user.getUsername() + " уже существует");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new RuntimeException("Имя пользователя не может быть пустым");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Пароль не может быть пустым");
        }

        user.setUsername(user.getUsername());
        user.setAge(user.getAge());

        Set<Role> roles = roleNames.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(int id, User userDetails, Set<Integer> roleNames) {
        log.debug("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        log.debug("User found: {}", user);
        user.setUsername(userDetails.getUsername());
        user.setAge(userDetails.getAge());

        Set<Role> roles = roleNames.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findById(2)
                    .orElseThrow(() -> new RuntimeException("Default role 'ROLE_USER' not found in the database."));
            roles.add(defaultRole);
            log.debug("Added default role: ROLE_USER");
        }

        user.setRoles(roles);
        user.setPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
        userRepository.save(user);
        log.debug("User updated successfully with roles: {}", roles);
    }

    @Override
    @Transactional
    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
        log.debug("Пользователь с ID={} успешно удален", userId);
    }

    @Override
    @Transactional
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
