package quiz.dtos.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserDTO {
    private String email;
    private Date isDeleted;
    private String avatar;
    private String phoneNumber;
}
