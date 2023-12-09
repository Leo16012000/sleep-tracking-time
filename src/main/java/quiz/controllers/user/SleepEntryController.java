package quiz.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quiz.constants.Paths;
import quiz.dtos.commons.SleepEntryDTO;
import quiz.dtos.commons.UserDTO;
import quiz.dtos.responses.ApiResponse;
import quiz.entities.SleepEntryEntity;
import quiz.entities.UserEntity;
import quiz.repos.SleepEntryRepository;
import quiz.repos.UserRepository;
import quiz.services.SleepEntryService;
import quiz.services.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Paths.APP + Paths.SLEEP_ENTRIES)
public class SleepEntryController {
    @Autowired
    private SleepEntryRepository sleepEntryRepository;
    @Autowired
    private SleepEntryService sleepEntryService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiResponse<SleepEntryEntity>>  findById(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse<>(true,null,sleepEntryRepository.findById(id).orElseThrow()));
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<ApiResponse<List<SleepEntryEntity>>>  findAll() {
        return ResponseEntity.ok(new ApiResponse<>(true,null,sleepEntryRepository.findAll()));
    }

    @RequestMapping(value = "/get-by-user", method = RequestMethod.POST)
    public ResponseEntity<ApiResponse<List<SleepEntryEntity>>>  findAllByUserId(@RequestBody SleepEntryDTO sleepEntryDTO) {
        return ResponseEntity.ok(new ApiResponse<>(true,null,sleepEntryRepository.findAllByUserId(sleepEntryDTO.getUserId())));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<ApiResponse<UUID>> addSleepEntry(@RequestBody SleepEntryDTO sleepEntryDTO) {
        return ResponseEntity.ok(new ApiResponse<>(true,null,sleepEntryService.addSleepEntry(sleepEntryDTO)));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<ApiResponse<UUID>>  updateById(@PathVariable UUID id, @RequestBody SleepEntryDTO sleepEntryDTO) {
        sleepEntryService.update(id, sleepEntryDTO);
        return ResponseEntity.ok(new ApiResponse<>(true,null, id));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ApiResponse<List<UserEntity>>>  deleteById(@PathVariable UUID id) {
        sleepEntryRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true,null, null));
    }
}

