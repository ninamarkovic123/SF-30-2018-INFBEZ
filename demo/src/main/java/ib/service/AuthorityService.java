package ib.service;

import java.util.List;

import ib.project.entity.Authority;

public interface AuthorityService {
	List<Authority> findById(Long id);
	List<Authority> findByname(String name);
}
