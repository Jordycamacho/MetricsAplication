package com.fitapp.backend.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.application.ports.output.SubscriptionPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.converter.SubscriptionConverter;
import com.fitapp.backend.infrastructure.persistence.converter.UserConverter;
import com.fitapp.backend.infrastructure.persistence.entity.SubscriptionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository implements UserPersistencePort {

    private final SpringDataUserRepository jpaRepository;
    private final SubscriptionPersistencePort subscriptionRepository;

    @Override
    public Optional<UserModel> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::mapToDomainWithSubscription);
    }

    @Override
    public Page<UserModel> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(this::mapToDomainWithSubscription);
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        Optional<UserEntity> userEntity = jpaRepository.findByEmail(email);

        // Debug
        if (userEntity.isPresent()) {
            System.out.println("User found: " + userEntity.get().getEmail());
            System.out.println("User subscription: " + userEntity.get().getSubscription());
        }

        return userEntity.map(this::mapToDomainWithSubscription);
    }

    @Override
    @Transactional 
    public UserModel save(UserModel userModel) {
        UserEntity entity = UserConverter.toEntity(userModel);
        UserEntity savedUser = jpaRepository.save(entity);

        if (userModel.getSubscription() != null) {
            
            SubscriptionModel subscription = userModel.getSubscription();
            subscription.setUserId(savedUser.getId());

            SubscriptionModel savedSubscription = subscriptionRepository.save(subscription);

            userModel.setSubscription(savedSubscription);

            SubscriptionEntity subscriptionEntity = SubscriptionConverter.toEntity(savedSubscription, savedUser);
            savedUser.setSubscription(subscriptionEntity);
            jpaRepository.save(savedUser);
        }

        return mapToDomainWithSubscription(savedUser);
    }

    private UserModel mapToDomainWithSubscription(UserEntity userEntity) {
        UserModel userModel = UserConverter.toDomain(userEntity);

        if (userEntity.getSubscription() == null) {
            Optional<SubscriptionModel> subscription = subscriptionRepository.findByUserId(userEntity.getId());
            subscription.ifPresent(userModel::setSubscription);
        }

        return userModel;
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }
}
