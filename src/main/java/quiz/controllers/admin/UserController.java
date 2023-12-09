package quiz.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quiz.constants.Paths;
import quiz.dtos.commons.UserDTO;
import quiz.dtos.responses.ApiResponse;
import quiz.repos.UserRepository;
import quiz.entities.UserEntity;
import quiz.services.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Paths.APP + Paths.ADMIN + Paths.USERS)
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiResponse<UserEntity>>  findById(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse<>(true,null,userRepository.findById(id).orElseThrow()));
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<ApiResponse<List<UserEntity>>>  findAll() {
        return ResponseEntity.ok(new ApiResponse<>(true,null,userRepository.findAll()));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<ApiResponse<UUID>>  updateById(@PathVariable UUID id, @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return ResponseEntity.ok(new ApiResponse<>(true,null, id));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ApiResponse<List<UserEntity>>>  deleteById(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true,null, null));
    }
}

