package quiz.auth.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import quiz.auth.dtos.requests.SignInRequest;
import quiz.auth.dtos.requests.SignUpRequest;
import quiz.auth.dtos.responses.JwtAuthenticationResponse;
import quiz.entities.UserEntity;
import quiz.auth.enums.Role;
import quiz.repos.UserRepository;
import quiz.auth.services.IAuthenticationService;
import quiz.auth.services.IJwtService;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
    private final UserRepository accountCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService iJwtService;
    private final AuthenticationManager authenticationManager;
    @Override
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        var user = UserEntity.builder().firstName(request.getFirstName()).lastName(request.getLastName())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER).build();
        accountCredentialRepository.save(user);
        var jwt = iJwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }

    @Override
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = accountCredentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        var jwt = iJwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }
}
