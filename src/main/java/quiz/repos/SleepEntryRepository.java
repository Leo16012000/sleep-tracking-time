package quiz.repos;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import quiz.entities.SleepEntryEntity;
import quiz.entities.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface SleepEntryRepository extends BaseRepository<SleepEntryEntity, Integer> {
    Optional<SleepEntryEntity> findById(UUID id);
    @Modifying
    @Query("DELETE FROM SleepEntryEntity s WHERE s.id = :id")
    void deleteById(UUID id);

    @Query("SELECT s FROM SleepEntryEntity s WHERE s.userId = :id")
    List<SleepEntryEntity> findAllByUserId(UUID id);
}
