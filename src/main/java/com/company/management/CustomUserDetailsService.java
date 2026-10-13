
package com.company.management;

import com.company.management.entity.User;
import com.company.management.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override

    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
            System.out.println("LOGIN SERVICE CALLED - username = " + username);

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found: " + username));

    System.out.println("User found: " + user.getUsername()
            + ", role: " + user.getRole());

    return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole())
            .disabled(!user.isActive())
            .build();
}
        
    }

