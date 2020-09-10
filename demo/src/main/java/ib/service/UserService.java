package ib.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ib.dto.UserDTO;
import ib.project.entity.Authority;
import ib.project.entity.User;
import ib.repository.UserRepository;
@Service
public class UserService implements UserServiceInterface {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthorityService authService;

	@Override
	public User findByEmail(String email) throws UsernameNotFoundException {
		User u = userRepository.findByEmail(email);
		return u;
	}

	public User findById(Long id) throws AccessDeniedException {
		User u = userRepository.findById(id).orElseGet(null);
		return u;
	}

	public List<User> findAll() throws AccessDeniedException {
		List<User> result = userRepository.findAll();
		return result;
	}

	
	@Override
	public User save(UserDTO userDTO) {
		User u = new User();
		u.setEmail(userDTO.getEmail());
		u.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		u.setCertificate(userDTO.getPath());
		u.setActive(false);
		List<Authority> auth = authService.findByname("ROLE_USER");
		u.setAuthorities(auth);
		u = this.userRepository.save(u);
		return u;
	}
	
	@Override
	public List<User> findByActive(Boolean active) {
		return userRepository.findByActive(active);
	}

	@Override
	public User activateUser(String email) {
		User user = userRepository.findByEmail(email);
		user.setActive(true);
		userRepository.save(user);
		return user;
		
	}
}
