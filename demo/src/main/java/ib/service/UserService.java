package ib.service;

import java.util.List;

import ib.project.entity.User;
import ib.project.entity.UserRequest;


public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll ();
	User save(UserRequest userRequest);
}
