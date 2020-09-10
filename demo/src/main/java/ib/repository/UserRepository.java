package ib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ib.project.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    List<User> findByActive(Boolean active);
	User findOne(Long id);
	
	
	
}