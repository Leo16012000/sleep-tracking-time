package quiz.dtos.commons;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class SleepEntryDTO {
    private UUID userId;
    private Timestamp sleepTime;
    private Timestamp wakeUpTime;
}
