package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.TrainerDto;
import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidOperationException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.util.UserInitializationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserInitializationUtil userInitializationUtil;
    private final TrainerMapper trainerMapper;
    private final UserService userService;

    public TrainerDto createTrainer(TrainerDto trainerDto) {
        if (trainerDto == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        TrainerEntity trainer = trainerMapper.toEntity(trainerDto);
        TrainerEntity created = userInitializationUtil.createUser(trainer, trainerRepository::save, "Trainer");
        return trainerMapper.toDto(created);
    }

    public TrainerDto updateTrainer(TrainerDto trainerDto) {
        if (trainerDto == null || trainerDto.getId() == null) {
            throw new IllegalArgumentException("Trainer or id cannot be null");
        }

        trainerRepository.findById(trainerDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with id: " + trainerDto.getId()));

        TrainerEntity trainer = trainerMapper.toEntity(trainerDto);
        TrainerEntity updated = trainerRepository.save(trainer);
        log.info("Trainer profile updated successfully: {}", trainerDto.getId());
        return trainerMapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public TrainerDto getTrainer(Long id) {
        return trainerMapper.toDto(getActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public List<TrainerDto> getAllTrainers() {
        return trainerRepository.findAll()
                .filter(TrainerEntity::isActive)
                .map(trainerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainerDto getTrainerByUsername(String username) {
        TrainerEntity trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: username=" + username);
        }
        return trainerMapper.toDto(trainer);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        userService.changePassword(username, oldPassword, newPassword);
    }

    public void toggleActivation(String username) {
        userService.toggleActivation(username);
    }

    @Transactional(readOnly = true)
    public TrainerDto getActiveTrainerForSpecialization(Long id, String typeName) {
        TrainerEntity trainer = getActiveEntity(id);
        if (!trainer.matchesSpecialization(typeName)) {
            throw new InvalidOperationException(
                    "Trainer specialization does not match training type: " + typeName);
        }
        return trainerMapper.toDto(trainer);
    }

    @Transactional(readOnly = true)
    public TrainerDto findActiveBySpecialization(String typeName) {
        TrainerEntity trainer = trainerRepository.findActiveBySpecialization(typeName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active trainer found for type: " + typeName));
        return trainerMapper.toDto(trainer);
    }

    private TrainerEntity getActiveEntity(Long id) {
        TrainerEntity trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with id: " + id));
        if (!trainer.isActive()) {
            throw new InvalidOperationException("Trainer is inactive: id=" + id);
        }
        return trainer;
    }
}
