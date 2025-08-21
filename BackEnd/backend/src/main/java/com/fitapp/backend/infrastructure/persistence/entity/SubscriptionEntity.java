package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.LocalDate;
import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity  
@Data
@Table(name = "subscriptions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "subscription_type", discriminatorType = DiscriminatorType.STRING)
public abstract class SubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;
    
    @OneToOne
    @JsonIgnore 
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public abstract SubscriptionType getType();

    public void setUser(UserEntity user) {
        this.user = user;
        if (user != null && user.getSubscription() != this) {
            user.setSubscription(this);
        }
    }
}