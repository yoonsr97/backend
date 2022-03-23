package com.example.demo.domain.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //Optional<User> findByEmail(String email);
    @Query("select u from User u where u.email = ?1 and u.platform = ?2")
    Optional<User> findByEmailAndPlatform(String email, String platform);

    Optional<User> findByPlatform(String platform);

    @Query("SELECT p FROM User p ORDER BY p.id DESC")
    List<User> findAll();

}
