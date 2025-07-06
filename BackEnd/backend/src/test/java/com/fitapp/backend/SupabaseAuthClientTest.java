package com.fitapp.backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fitapp.backend.domain.exception.SupabaseUserNotFoundException;
import com.fitapp.backend.infrastructure.persistence.adapter.out.supabase.SupabaseAuthClient;

@SpringBootTest
public class SupabaseAuthClientTest {

    @Autowired
    private SupabaseAuthClient supabaseAuthClient;

    @Test
    void shouldVerifyValidUid() {
        assertDoesNotThrow(() -> supabaseAuthClient.verifySupabaseUid("uid_valido"));
    }

    @Test
    void shouldThrowForInvalidUid() {
        assertThrows(SupabaseUserNotFoundException.class, 
            () -> supabaseAuthClient.verifySupabaseUid("uid_invalido"));
    }
}