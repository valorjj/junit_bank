package com.example.banksample.repository;

import com.example.banksample.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /*
    SELECT * from user where username = ?
    * */
    Optional<User> findByUsername(String username);

}
