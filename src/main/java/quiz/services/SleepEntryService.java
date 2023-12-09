package quiz.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quiz.dtos.commons.SleepEntryDTO;
import quiz.dtos.commons.UserDTO;
import quiz.entities.SleepEntryEntity;
import quiz.entities.UserEntity;
import quiz.repos.SleepEntryRepository;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
public class SleepEntryService {
    @Autowired
    private SleepEntryRepository sleepEntryRepository;

    public UUID addSleepEntry(SleepEntryDTO sleepEntryDTO) {
        SleepEntryEntity sleepEntry = new SleepEntryEntity();
        Timestamp sleepTime = sleepEntryDTO.getSleepTime();
        Timestamp wakeUpTime = sleepEntryDTO.getWakeUpTime();
        sleepEntry.setUserId(sleepEntryDTO.getUserId());
        sleepEntry.setSleepTime(sleepTime);
        sleepEntry.setWakeUpTime(wakeUpTime);
        long millisecondsDifference = wakeUpTime.getTime() - sleepTime.getTime();
        sleepEntry.setTotalSleepDuration(millisecondsDifference);

        return sleepEntryRepository.save(sleepEntry).getId();
    }

    public UUID update(UUID id, SleepEntryDTO sleepEntryDTO) {
        SleepEntryEntity sleepEntry = sleepEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sleep entry not found with id: " + id));
        Timestamp sleepTime = sleepEntryDTO.getSleepTime();
        Timestamp wakeUpTime = sleepEntryDTO.getWakeUpTime();
        sleepEntry.setSleepTime(sleepEntryDTO.getSleepTime());
        sleepEntry.setWakeUpTime(sleepEntryDTO.getWakeUpTime());
        long millisecondsDifference = wakeUpTime.getTime() - sleepTime.getTime();
        sleepEntry.setTotalSleepDuration(millisecondsDifference);
        return sleepEntryRepository.save(sleepEntry).getId();
    }
}
