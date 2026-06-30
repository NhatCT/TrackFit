package com.ntn.services.impl;

import com.ntn.dto.PaymentOrderDTO;
import com.ntn.dto.SubscriptionStatusDTO;
import com.ntn.pojo.PaymentOrder;
import com.ntn.pojo.PaymentOrder.PaymentStatus;
import com.ntn.pojo.User;
import com.ntn.repositories.PaymentOrderRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.ChatQuotaService;
import com.ntn.services.PremiumService;
import com.ntn.services.SubscriptionService;
import com.ntn.services.UserService;
import com.ntn.services.WebSocketEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private PaymentOrderRepository orderRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private PremiumService premiumService;
    @Autowired
    private ChatQuotaService chatQuotaService;
    @Autowired
    private WebSocketEventService webSocketEventService;

    private static final Map<String, Integer> PLAN_PRICES = Map.of(
            "monthly", 99000,
            "yearly", 499000
    );

    @Override
    public SubscriptionStatusDTO getStatus(String username) {
        User u = mustUser(username);
        boolean premium = premiumService.isPremiumActive(u);
        SubscriptionStatusDTO dto = new SubscriptionStatusDTO();
        dto.setPremium(premium);
        dto.setPremiumExpiresAt(u.getPremiumExpiresAt());
        dto.setChatDailyLimit(chatQuotaService.getDailyLimit());
        dto.setChatRemaining(chatQuotaService.getRemaining(u.getUserId(), premium));
        return dto;
    }

    @Override
    public PaymentOrderDTO createOrder(String username, String planKey) {
        if (!PLAN_PRICES.containsKey(planKey)) {
            throw new IllegalArgumentException("Gói không hợp lệ. Chọn 'monthly' hoặc 'yearly'.");
        }

        User user = mustUser(username);

        if (premiumService.isPremiumActive(user)) {
            throw new IllegalStateException("Tài khoản đã là PRO. Không cần tạo đơn hàng mới.");
        }

        String idempotencyKey = generateIdempotencyKey(user.getUserId(), planKey);

        PaymentOrder existing = orderRepo.findByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            if (existing.getStatus() == PaymentStatus.CANCELLED
                    || existing.getStatus() == PaymentStatus.REJECTED) {
                idempotencyKey = generateIdempotencyKey(user.getUserId(), planKey + "_" + System.currentTimeMillis());
            } else {
                return PaymentOrderDTO.fromEntity(existing);
            }
        }

        List<PaymentOrder> activeOrders = orderRepo.findByUserAndStatusIn(
                user.getUserId(), List.of("PENDING", "SUBMITTED"));
        if (!activeOrders.isEmpty()) {
            return PaymentOrderDTO.fromEntity(activeOrders.get(0));
        }

        PaymentOrder order = new PaymentOrder();
        order.setUser(user);
        order.setPlanKey(planKey);
        order.setAmount(PLAN_PRICES.get(planKey));
        order.setStatus(PaymentStatus.PENDING);
        order.setIdempotencyKey(idempotencyKey);
        order.setTransferRef("GUTIM_PRO_" + username);

        orderRepo.save(order);
        return PaymentOrderDTO.fromEntity(order);
    }

    @Override
    public PaymentOrderDTO submitOrder(String username, int orderId) {
        User user = mustUser(username);
        PaymentOrder order = orderRepo.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại.");
        }
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("Bạn không có quyền truy cập đơn hàng này.");
        }
        if (!order.canTransitionTo(PaymentStatus.SUBMITTED)) {
            throw new IllegalStateException(
                    "Không thể chuyển trạng thái. Trạng thái hiện tại: " + order.getStatus());
        }

        if (order.getExpiredAt() != null && order.getExpiredAt().isBefore(LocalDateTime.now())) {
            order.setStatus(PaymentStatus.CANCELLED);
            orderRepo.update(order);
            throw new IllegalStateException("Đơn hàng đã hết hạn. Vui lòng tạo đơn hàng mới.");
        }

        order.setStatus(PaymentStatus.SUBMITTED);
        order.setSubmittedAt(LocalDateTime.now());
        orderRepo.update(order);

        return PaymentOrderDTO.fromEntity(order);
    }

    @Override
    public PaymentOrderDTO getCurrentOrder(String username) {
        User user = mustUser(username);
        List<PaymentOrder> active = orderRepo.findByUserAndStatusIn(
                user.getUserId(), List.of("PENDING", "SUBMITTED"));
        if (active.isEmpty()) return null;
        return PaymentOrderDTO.fromEntity(active.get(0));
    }

    @Override
    public PaymentOrderDTO verifyOrder(String adminUsername, int orderId, boolean approved, String note) {
        PaymentOrder order = orderRepo.findByIdForUpdate(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại.");
        }

        if (order.getStatus() != PaymentStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Đơn hàng đã được xử lý bởi admin khác. Trạng thái: " + order.getStatus());
        }

        if (approved) {
            order.setStatus(PaymentStatus.VERIFIED);
            order.setVerifiedAt(LocalDateTime.now());
            order.setVerifiedBy(adminUsername);
            order.setAdminNote(note);

            User user = order.getUser();
            int days = "yearly".equalsIgnoreCase(order.getPlanKey()) ? 365 : 30;

            LocalDateTime base = user.getPremiumExpiresAt();
            if (base != null && base.isAfter(LocalDateTime.now())) {
                base = base.plusDays(days);
            } else {
                base = LocalDateTime.now().plusDays(days);
            }

            user.setIsPremium(true);
            user.setPremiumExpiresAt(base);
            user.setUpdatedAt(LocalDateTime.now());
            userRepo.updateUser(user);

            order.setStatus(PaymentStatus.ACTIVATED);
            orderRepo.update(order);

            webSocketEventService.publishSubscriptionActivated(
                    user.getUsername(),
                    Map.of(
                            "planKey", order.getPlanKey(),
                            "premiumExpiresAt", base.toString(),
                            "orderId", order.getOrderId()
                    )
            );
        } else {
            order.setStatus(PaymentStatus.REJECTED);
            order.setVerifiedAt(LocalDateTime.now());
            order.setVerifiedBy(adminUsername);
            order.setAdminNote(note != null ? note : "Admin từ chối thanh toán.");
            orderRepo.update(order);
        }

        return PaymentOrderDTO.fromEntity(order);
    }

    @Override
    public List<PaymentOrderDTO> listOrders(String status, int page, int pageSize) {
        return orderRepo.findAllByStatus(status, page, pageSize)
                .stream()
                .map(PaymentOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentOrderDTO getOrder(int orderId) {
        return PaymentOrderDTO.fromEntity(orderRepo.findById(orderId));
    }

    @Override
    public long countOrders(String status) {
        return orderRepo.countByStatus(status);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void cancelExpiredOrders() {
        List<PaymentOrder> expired = orderRepo.findExpiredOrders();
        for (PaymentOrder order : expired) {
            order.setStatus(PaymentStatus.CANCELLED);
            order.setAdminNote("Tự động huỷ — hết hạn 24h.");
            orderRepo.update(order);
        }
        if (!expired.isEmpty()) {
            System.out.println("[Scheduler] Auto-cancelled " + expired.size() + " expired payment orders.");
        }
    }

    private User mustUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng: " + username);
        return u;
    }

    private String generateIdempotencyKey(int userId, String planKey) {
        try {
            String raw = userId + "_" + planKey + "_" + LocalDate.now();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return userId + "_" + planKey + "_" + LocalDate.now();
        }
    }
}
