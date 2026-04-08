package com.examinai.app.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(String email);

	@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles ORDER BY u.email ASC")
	List<User> findAllWithRolesOrderedByEmail();

	@Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName ORDER BY u.email ASC")
	List<User> findAllWithRoleName(@Param("roleName") String roleName);
}
