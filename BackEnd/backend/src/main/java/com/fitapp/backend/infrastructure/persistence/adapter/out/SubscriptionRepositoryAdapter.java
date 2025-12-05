package com.fitapp.backend.infrastructure.persistence.adapter.out;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fitapp.backend.application.ports.output.SubscriptionPersistencePort;
import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.infrastructure.persistence.converter.SubscriptionConverter;
import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataSubscriptionRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionPersistencePort {

    private final SpringDataSubscriptionRepository jpaRepository;
    private final SpringDataUserRepository userRepository; 

    @Override
    public Optional<SubscriptionModel> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .map(SubscriptionConverter::toDomain);
    }

    @Override
    public SubscriptionModel save(SubscriptionModel subscriptionModel) {
   
        UserEntity user = userRepository.findById(subscriptionModel.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        SubscriptionEntity entity = SubscriptionConverter.toEntity(subscriptionModel, user);
        SubscriptionEntity  savedEntity = jpaRepository.save(entity);
              
        return SubscriptionConverter.toDomain(savedEntity);
    }
}