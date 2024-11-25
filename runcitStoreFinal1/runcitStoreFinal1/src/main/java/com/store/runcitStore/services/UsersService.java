package com.store.runcitStore.services;

import com.store.runcitStore.models.UsersModel;
import com.store.runcitStore.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsersModel registerUser(String login, String password, String email) {
        if (login == null || password == null) {
            return null;
        } else {
            if (usersRepository.findFirstByLogin(login).isPresent()) {
                System.out.println("Duplicate Login");
                return null;
            }
            UsersModel usersModel = new UsersModel();
            usersModel.setLogin(login);
            usersModel.setPassword(passwordEncoder.encode(password));
            usersModel.setEmail(email);
            return usersRepository.save(usersModel);
        }
    }

    public UsersModel authenticate(String login, String rawPassword) {
        System.out.println("Authenticating user: " + login);
        Optional<UsersModel> userOptional = usersRepository.findFirstByLogin(login);
        if (userOptional.isPresent()) {
            UsersModel user = userOptional.get();
            System.out.println("User found: " + user.getLogin());
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                System.out.println("Password match successful");
                return user;
            } else {
                System.out.println("Password mismatch");
            }
        } else {
            System.out.println("User not found");
        }
        return null;
    }

}
