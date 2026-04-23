package com.fitapp.backend.auth.aplication.port.output;

public interface TokenBlacklistPort {
    
    void blacklist(String token, long ttlSeconds);
    boolean isBlacklisted(String token);
}