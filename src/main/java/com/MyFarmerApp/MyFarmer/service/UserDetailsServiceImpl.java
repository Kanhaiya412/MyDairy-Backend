package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // ✅ Convert enum to string using .name()
        GrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new com.MyFarmerApp.MyFarmer.config.MyFarmerUserDetails(
                user.getUsername(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                Collections.singletonList(authority),
                user.getId()
        );
    }
}
