package com.techvedika.harmonycvi.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.UserLogin;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {

//    UserLogin findByEmail(String email);

    // Alternatively, you can use the @Query annotations if you want to leverage the named queries defined above:
//    @Query("SELECT ul FROM UserLogin ul WHERE ul.email = :email AND ul.password = :password")
//    UserLogin getUserByEmailAndPassword(@Param("email") String email, @Param("password") String password);
}