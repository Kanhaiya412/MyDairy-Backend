package com.MyFarmerApp.MyFarmer.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom UserDetails to store the userId in the SecurityContext.
 * This allows us to retrieve the ID without hitting the database in every request.
 */
@Getter
public class MyFarmerUserDetails extends User {

    private final Long userId;

    public MyFarmerUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long userId) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public MyFarmerUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
                              boolean credentialsNonExpired, boolean accountNonLocked,
                              Collection<? extends GrantedAuthority> authorities, Long userId) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }
}
