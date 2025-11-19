package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email); // ✅ Add this line

    boolean existsByEmail(String email);
}
