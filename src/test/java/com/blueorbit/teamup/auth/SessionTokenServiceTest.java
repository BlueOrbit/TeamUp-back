package com.blueorbit.teamup.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTokenServiceTest {

    @Test
    void issueAndVerifyShouldWork() {
        SessionTokenService tokenService = new SessionTokenService(2);
        String token = tokenService.issueToken(1001L);
        assertNotNull(token);
        assertEquals(1001L, tokenService.verifyAndGetUid(token));
    }

    @Test
    void revokeShouldInvalidateToken() {
        SessionTokenService tokenService = new SessionTokenService(2);
        String token = tokenService.issueToken(1002L);
        tokenService.revoke(token);
        assertNull(tokenService.verifyAndGetUid(token));
    }
}
