package quiz.auth.services;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface IAuthService {
    UserDetailsService userDetailsService();
}
