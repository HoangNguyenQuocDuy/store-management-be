package com.seveneleven;

import com.seveneleven.entity.User;
import com.seveneleven.entity.UserRole;
import com.seveneleven.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
public class CMSApplication {
    public static void main(String[] args) {
        SpringApplication.run(CMSApplication.class, args);
    }

//    @Bean
//    CommandLineRunner initAdmin(UserRepository repo) {
//        return args -> {
//            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
//
//            User admin = repo.findByEmail("hoangnguyenquocduy0604@gmail.com").orElse(null);
//            if (admin == null) {
//                repo.save(User.builder()
//                        .username("admin")
//                        .email("hoangnguyenquocduy0604@gmail.com")
//                        .fullName("Administrator")
//                        .password(passwordEncoder.encode("admin123"))
//                        .role(UserRole.ADMIN)
//                        .build());
//            }
//            User user = repo.findByEmail("quocduy6114@gmail.com").orElse(null);
//            if (user == null) {
//                repo.save(User.builder()
//                        .username("user")
//                        .email("quocduy6114@gmail.com")
//                        .fullName("User")
//                        .password(passwordEncoder.encode("user123"))
//                        .role(UserRole.USER)
//                        .build());
//            }
//        };
//    }
}
