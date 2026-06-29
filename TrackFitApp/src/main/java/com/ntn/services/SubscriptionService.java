package com.ntn.services;

import com.ntn.dto.SubscriptionConfirmDTO;
import com.ntn.dto.SubscriptionStatusDTO;
import com.ntn.dto.UserResponseDTO;

public interface SubscriptionService {

    SubscriptionStatusDTO getStatus(String username);

    UserResponseDTO confirmPayment(String username, SubscriptionConfirmDTO dto);
}
