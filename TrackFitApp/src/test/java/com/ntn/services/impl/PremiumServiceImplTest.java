package com.ntn.services.impl;

import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import com.ntn.exceptions.PremiumRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PremiumServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private PremiumServiceImpl premiumService;

    private User premiumUser;
    private User expiredUser;
    private User freeUser;

    @BeforeEach
    void setUp() {
        premiumUser = new User();
        premiumUser.setUserId(1);
        premiumUser.setUsername("premiumuser");
        premiumUser.setIsPremium(true);
        premiumUser.setPremiumExpiresAt(LocalDateTime.now().plusDays(30));

        expiredUser = new User();
        expiredUser.setUserId(2);
        expiredUser.setUsername("expireduser");
        expiredUser.setIsPremium(true);
        expiredUser.setPremiumExpiresAt(LocalDateTime.now().minusDays(1));

        freeUser = new User();
        freeUser.setUserId(3);
        freeUser.setUsername("freeuser");
        freeUser.setIsPremium(false);
    }

    @Test
    void isPremiumActive_activePremium_returnsTrue() {
        assertTrue(premiumService.isPremiumActive(premiumUser));
    }

    @Test
    void isPremiumActive_expiredPremium_returnsFalse() {
        assertFalse(premiumService.isPremiumActive(expiredUser));
    }

    @Test
    void isPremiumActive_freeUser_returnsFalse() {
        assertFalse(premiumService.isPremiumActive(freeUser));
    }

    @Test
    void isPremiumActive_nullUser_returnsFalse() {
        assertFalse(premiumService.isPremiumActive((User) null));
    }

    @Test
    void isPremiumActive_premiumWithNullExpiry_returnsTrue() {
        User u = new User();
        u.setIsPremium(true);
        u.setPremiumExpiresAt(null);
        assertTrue(premiumService.isPremiumActive(u));
    }

    @Test
    void isPremiumActive_nullIsPremiumFlag_returnsFalse() {
        User u = new User();
        u.setIsPremium(null);
        assertFalse(premiumService.isPremiumActive(u));
    }

    @Test
    void isPremiumActive_byUsername_delegatesToUserLookup() {
        when(userRepo.getUserByUsername("premiumuser")).thenReturn(premiumUser);
        assertTrue(premiumService.isPremiumActive("premiumuser"));
        verify(userRepo).getUserByUsername("premiumuser");
    }

    @Test
    void isPremiumActive_byUsername_userNotFound_returnsFalse() {
        when(userRepo.getUserByUsername("unknown")).thenReturn(null);
        assertFalse(premiumService.isPremiumActive("unknown"));
    }

    @Test
    void requirePremium_activePremium_noException() {
        when(userRepo.getUserByUsername("premiumuser")).thenReturn(premiumUser);
        assertDoesNotThrow(() -> premiumService.requirePremium("premiumuser"));
    }

    @Test
    void requirePremium_freeUser_throwsException() {
        when(userRepo.getUserByUsername("freeuser")).thenReturn(freeUser);
        assertThrows(PremiumRequiredException.class, () -> premiumService.requirePremium("freeuser"));
    }
}
