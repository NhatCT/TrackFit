package com.ntn.services.impl;

import com.ntn.dto.PaymentOrderDTO;
import com.ntn.dto.SubscriptionStatusDTO;
import com.ntn.dto.UserResponseDTO;
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

    // ========================
    //  User-facing operations
    // ========================

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

    /**
     * Tạo đơn hàng mới — IDEMPOTENT.
     *
     * Idempotency Key = SHA-256(userId + planKey + ngày hôm nay)
     * → Nếu cùng user, cùng gói, cùng ngày → trả order cũ thay vì tạo mới.
     * → Chống double-click, retry, race condition từ client.
     */
    @Override
    public PaymentOrderDTO createOrder(String username, String planKey) {
        if (!PLAN_PRICES.containsKey(planKey)) {
            throw new IllegalArgumentException("Gói không hợp lệ. Chọn 'monthly' hoặc 'yearly'.");
        }

        User user = mustUser(username);

        // Kiểm tra đã là PRO chưa
        if (premiumService.isPremiumActive(user)) {
            throw new IllegalStateException("Tài khoản đã là PRO. Không cần tạo đơn hàng mới.");
        }

        // Generate idempotency key
        String idempotencyKey = generateIdempotencyKey(user.getUserId(), planKey);

        // Kiểm tra duplicate — nếu đã có order cùng key → trả order cũ
        PaymentOrder existing = orderRepo.findByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            // Nếu order cũ đã CANCELLED/REJECTED → cho tạo mới (key khác ngày)
            if (existing.getStatus() == PaymentStatus.CANCELLED
                    || existing.getStatus() == PaymentStatus.REJECTED) {
                // Key collision nhưng order đã terminal → tạo key mới với timestamp
                idempotencyKey = generateIdempotencyKey(user.getUserId(), planKey + "_" + System.currentTimeMillis());
            } else {
                // Order đang active (PENDING/SUBMITTED/VERIFIED) → trả lại
                return PaymentOrderDTO.fromEntity(existing);
            }
        }

        // Kiểm tra có order PENDING/SUBMITTED khác không (tránh spam)
        List<PaymentOrder> activeOrders = orderRepo.findByUserAndStatusIn(
                user.getUserId(), List.of("PENDING", "SUBMITTED"));
        if (!activeOrders.isEmpty()) {
            // Trả order đang active đầu tiên
            return PaymentOrderDTO.fromEntity(activeOrders.get(0));
        }

        // Tạo order mới
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

    /**
     * User xác nhận đã chuyển khoản: PENDING → SUBMITTED.
     * Kiểm tra ownership + state transition hợp lệ.
     */
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

        // Kiểm tra hết hạn
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

    // ========================
    //  Admin-facing operations
    // ========================

    /**
     * Admin xác minh thanh toán — CORE CONCURRENCY HANDLER.
     *
     * Sử dụng Pessimistic Lock (SELECT FOR UPDATE):
     * - Admin A gọi verify(order=1) → lock row → xử lý → commit → unlock
     * - Admin B gọi verify(order=1) cùng lúc → block chờ → khi đọc lại → status đã VERIFIED
     * → Chỉ 1 admin verify thành công, admin thứ 2 nhận lỗi "đã xử lý".
     */
    @Override
    public PaymentOrderDTO verifyOrder(String adminUsername, int orderId, boolean approved, String note) {
        // === PESSIMISTIC LOCK ===
        PaymentOrder order = orderRepo.findByIdForUpdate(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại.");
        }

        // Sau khi acquire lock, kiểm tra lại status (có thể admin khác đã verify)
        if (order.getStatus() != PaymentStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Đơn hàng đã được xử lý bởi admin khác. Trạng thái: " + order.getStatus());
        }

        if (approved) {
            // === VERIFY + ACTIVATE ===
            order.setStatus(PaymentStatus.VERIFIED);
            order.setVerifiedAt(LocalDateTime.now());
            order.setVerifiedBy(adminUsername);
            order.setAdminNote(note);

            // Kích hoạt PRO cho user
            User user = order.getUser();
            int days = "yearly".equalsIgnoreCase(order.getPlanKey()) ? 365 : 30;

            LocalDateTime base = user.getPremiumExpiresAt();
            if (base != null && base.isAfter(LocalDateTime.now())) {
                base = base.plusDays(days); // Cộng dồn nếu đang PRO
            } else {
                base = LocalDateTime.now().plusDays(days);
            }

            user.setIsPremium(true);
            user.setPremiumExpiresAt(base);
            user.setUpdatedAt(LocalDateTime.now());
            userRepo.updateUser(user);

            order.setStatus(PaymentStatus.ACTIVATED);
            orderRepo.update(order);

            // Publish Kafka event → WebSocket push tới user
            webSocketEventService.publishSubscriptionActivated(
                    user.getUsername(),
                    Map.of(
                            "planKey", order.getPlanKey(),
                            "premiumExpiresAt", base.toString(),
                            "orderId", order.getOrderId()
                    )
            );
        } else {
            // === REJECT ===
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

    /**
     * Scheduled Job — tự động huỷ order quá 24h chưa thanh toán.
     * Chạy mỗi giờ.
     */
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

    // ========================
    //  Helpers
    // ========================

    private User mustUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng: " + username);
        return u;
    }

    /**
     * SHA-256 idempotency key = hash(userId + planKey + today).
     * Đảm bảo cùng user + cùng gói + cùng ngày → cùng key → không tạo duplicate.
     */
    private String generateIdempotencyKey(int userId, String planKey) {
        try {
            String raw = userId + "_" + planKey + "_" + LocalDate.now();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            // Fallback nếu SHA-256 không khả dụng (cực kỳ hiếm)
            return userId + "_" + planKey + "_" + LocalDate.now();
        }
    }
}
