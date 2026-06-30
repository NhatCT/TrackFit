package com.ntn.pojo;

import com.ntn.pojo.PaymentOrder.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentOrderTest {

    @Test
    void canTransitionTo_pendingToSubmitted() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(PaymentStatus.PENDING);
        assertTrue(order.canTransitionTo(PaymentStatus.SUBMITTED));
    }

    @Test
    void canTransitionTo_pendingToCancelled() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(PaymentStatus.PENDING);
        assertTrue(order.canTransitionTo(PaymentStatus.CANCELLED));
    }

    @Test
    void canTransitionTo_pendingToVerified_notAllowed() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(PaymentStatus.PENDING);
        assertFalse(order.canTransitionTo(PaymentStatus.VERIFIED));
    }

    @Test
    void canTransitionTo_submittedToVerified() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(PaymentStatus.SUBMITTED);
        assertTrue(order.canTransitionTo(PaymentStatus.VERIFIED));
    }

    @Test
    void canTransitionTo_submittedToRejected() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(PaymentStatus.SUBMITTED);
        assertTrue(order.canTransitionTo(PaymentStatus.REJECTED));
    }

    @Test
    void canTransitionTo_verifiedToActivated() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(PaymentStatus.VERIFIED);
        assertTrue(order.canTransitionTo(PaymentStatus.ACTIVATED));
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"ACTIVATED", "CANCELLED", "REJECTED"})
    void canTransitionTo_terminalStates_noTransitionsAllowed(PaymentStatus terminalStatus) {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(terminalStatus);
        for (PaymentStatus target : PaymentStatus.values()) {
            assertFalse(order.canTransitionTo(target),
                    terminalStatus + " should not transition to " + target);
        }
    }

    @Test
    void canTransitionTo_nullStatus_returnsFalse() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(null);
        assertFalse(order.canTransitionTo(PaymentStatus.SUBMITTED));
    }

    @Test
    void canTransitionTo_nullTarget_returnsFalse() {
        PaymentOrder order = new PaymentOrder();
        order.setStatus(PaymentStatus.PENDING);
        assertFalse(order.canTransitionTo(null));
    }

    @Test
    void prePersist_setsDefaults() {
        PaymentOrder order = new PaymentOrder();
        order.prePersist();

        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getExpiredAt());
        assertEquals(PaymentStatus.PENDING, order.getStatus());
        assertTrue(order.getExpiredAt().isAfter(order.getCreatedAt()));
    }

    @Test
    void prePersist_doesNotOverrideExisting() {
        PaymentOrder order = new PaymentOrder();
        LocalDateTime custom = LocalDateTime.of(2025, 1, 1, 12, 0);
        order.setCreatedAt(custom);
        order.setExpiredAt(custom.plusHours(48));
        order.setStatus(PaymentStatus.SUBMITTED);

        order.prePersist();

        assertEquals(custom, order.getCreatedAt());
        assertEquals(custom.plusHours(48), order.getExpiredAt());
        assertEquals(PaymentStatus.SUBMITTED, order.getStatus());
    }
}
