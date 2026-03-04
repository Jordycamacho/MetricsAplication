package com.fitapp.backend.application.ports.output;

public interface TokenBlacklistPort {
    
    void blacklist(String token, long ttlSeconds);
    boolean isBlacklisted(String token);
}