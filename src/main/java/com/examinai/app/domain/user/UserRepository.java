package com.examinai.app.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(String email);

	@EntityGraph(attributePaths = { "roles" })
	@Query("SELECT u FROM User u WHERE u.id = :id")
	Optional<User> findWithRolesById(@Param("id") UUID id);

	@EntityGraph(attributePaths = { "roles" })
	@Query("SELECT u FROM User u ORDER BY u.email ASC")
	List<User> findAllWithRolesOrderedByEmail();

	@EntityGraph(attributePaths = { "roles" })
	@Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName ORDER BY u.email ASC")
	List<User> findAllWithRoleName(@Param("roleName") String roleName);
}
