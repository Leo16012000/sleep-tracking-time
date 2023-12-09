package quiz.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quiz.dtos.commons.UserDTO;
import quiz.entities.UserEntity;
import quiz.repos.UserRepository;

import java.util.Date;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserEntity updateUser(UUID userId, UserDTO userDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

//        user.setEmail(userDTO.getEmail());
        user.setIsDeleted(userDTO.getIsDeleted());
        user.setAvatar(userDTO.getAvatar());
        user.setPhoneNumber(userDTO.getPhoneNumber());

        return userRepository.save(user);
    }

    public UserEntity deleteUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setIsDeleted(new Date());

        return userRepository.save(user);
    }
}
