package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AdminService {

    String getUserRolesString(int userId);
    Optional<User> getUserByUsername(String username);
    User getUser(int userId);
    List<User> getUsers();
    User createUser(User user, Set<Integer> roleNames);
    void updateUser(int id, User userDetails, Set<Integer> roleNames);
    void deleteUser(int userId);
    List<Role> getAllRoles();
}
