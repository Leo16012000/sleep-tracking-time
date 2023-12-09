package quiz.auth.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import quiz.auth.dtos.requests.SignInRequest;
import quiz.auth.dtos.requests.SignUpRequest;
import quiz.auth.dtos.responses.JwtAuthenticationResponse;
import quiz.entities.UserEntity;
import quiz.repos.UserRepository;
import quiz.dtos.responses.ApiResponse;
import quiz.auth.services.IAuthenticationService;
import quiz.constants.Paths;

import java.util.Optional;

@RestController
@RequestMapping(Paths.APP+ Paths.AUTH)
@RequiredArgsConstructor
public class AuthenticationController {
    private UserRepository repo;
    private final IAuthenticationService authenticationService;
    @PostMapping(Paths.SIGN_UP)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> signUp(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,null,authenticationService.signUp(request)));
    }

    @PostMapping(Paths.SIGN_IN)
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> signIn(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true,null,authenticationService.signIn(request)));
    }

    @RequestMapping(value = "current-user", method = RequestMethod.GET)
    public ResponseEntity<ApiResponse<UserEntity>>  getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<UserEntity> accountCredential =  repo.findByEmail(username);
        return ResponseEntity.ok(new ApiResponse<>(true,null,accountCredential.orElse(null)));
    }
}
