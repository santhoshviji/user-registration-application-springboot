package com.Springcrud.Registration.repository;

import com.Springcrud.Registration.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.gender) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.country) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(u.dateOfBirth AS string) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "EXISTS (SELECT 1 FROM u.skills skill WHERE LOWER(skill) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<User> searchUsersAcrossFields(@Param("keyword") String keyword, Pageable pageable);
}