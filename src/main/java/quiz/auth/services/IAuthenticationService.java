package quiz.auth.services;

import quiz.auth.dtos.requests.SignInRequest;
import quiz.auth.dtos.requests.SignUpRequest;
import quiz.auth.dtos.responses.JwtAuthenticationResponse;

public interface IAuthenticationService {
    JwtAuthenticationResponse signUp(SignUpRequest request);

    JwtAuthenticationResponse signIn(SignInRequest request);
}
