package com.fitapp.backend.application.service;

import com.fitapp.backend.application.ports.input.SportUseCase;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.SportModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportUseCase {

    private final SportPersistencePort sportPersistencePort;
    private final UserPersistencePort userPersistencePort;

    @Override
    public List<SportModel> getAllSports() {
        return sportPersistencePort.findAll();
    }

    @Override
    public List<SportModel> getPredefinedSports() {
        return sportPersistencePort.findByIsPredefinedTrue();
    }

    @Override
    public List<SportModel> getUserSports(String userEmail) {
        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return sportPersistencePort.findByCreatedBy(user.getId());
    }

    @Override
    public SportModel createCustomSport(SportModel sportModel, String userEmail) {
        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sportModel.setIsPredefined(false);
        sportModel.setCreatedBy(user.getId());
        return sportPersistencePort.save(sportModel);
    }

    @Override
    public void deleteCustomSport(Long sportId, String userEmail) {
        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var sport = sportPersistencePort.findById(sportId)
                .orElseThrow(() -> new RuntimeException("Sport not found"));

        if (sport.getIsPredefined()) {
            throw new RuntimeException("Cannot delete predefined sports");
        }

        if (!sport.getCreatedBy().equals(user.getId())) {
            throw new RuntimeException("Cannot delete other users' sports");
        }

        sportPersistencePort.delete(sportId);
    }
}