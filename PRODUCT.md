# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Ba nhóm chính, cùng lúc:

1. **Người tự tập gym / tự theo dõi sức khỏe tại nhà** — không cần PT, tự log dữ liệu và xem gợi ý AI theo cường độ thực tế.
2. **Học sinh / sinh viên Việt Nam mới bắt đầu tập** — cần hướng dẫn cơ bản, giao diện thân thiện, không phức tạp.
3. **Người muốn giảm cân / kiểm soát sức khỏe cá nhân** — chú trọng BMI, cân nặng, huyết áp theo thời gian và lời khuyên AI phù hợp thể trạng.

Tất cả đều là người Việt Nam, dùng app trên điện thoại hoặc máy tính, chủ yếu sau giờ tập hoặc buổi tối để log và xem lại tiến trình.

## Product Purpose

Gutim giúp người dùng theo dõi sức khỏe và xây dựng kế hoạch tập luyện cá nhân hóa với AI — tất cả trong một app. Thành công là khi người dùng duy trì được chuỗi ngày tập, nhận gợi ý bài tập phù hợp thể trạng, và thấy biểu đồ sức khỏe của mình cải thiện theo tuần.

## Positioning

AI cá nhân hóa theo thể trạng người Việt: dùng ngưỡng BMI châu Á (≥23 thừa cân, ≥25 béo phì) thay vì ngưỡng WHO phương Tây — điểm khác biệt mà MyFitnessPal hay Garmin không có. Gợi ý bài tập tự động điều chỉnh theo BMI hiện tại và lịch sử sức khỏe thực tế của từng người.

## Operating Context

- Người dùng log sau buổi tập hoặc sau bữa ăn (mobile hoặc desktop).
- Dashboard Home là màn hình chính sau đăng nhập — cần đọc nhanh: BMI, chuỗi ngày, gợi ý hôm nay.
- Chatbot "Gutim Coach" tư vấn real-time về dinh dưỡng và kỹ thuật tập.
- Admin quản lý kho bài tập và kế hoạch mẫu từ giao diện riêng.

## Capabilities and Constraints

- **Frontend:** React 19 + Bootstrap 5 + React Router v7 (CRA), deploy Vercel.
- **Backend:** Spring MVC 6 / Hibernate 6 / JWT, deploy Render (Docker WAR on Tomcat 11).
- **AI:** FastAPI microservice (Sentence Transformer + FAISS + LLM), deploy HuggingFace Spaces.
- **DB:** Aiven MySQL; **Cache:** Upstash Redis; **Media:** Cloudinary.
- **Design system:** CSS custom property tokens (`tokens.css`) — iOS-inspired palette (pink, green, cyan, amber, purple).
- **WebSocket:** real-time notifications và chat qua SockJS/STOMP.
- Không có native mobile app; mobile web phải hoạt động tốt trên ≥360px.
- Flyway migration enabled trên production; Kafka disabled.

## Brand Commitments

- **Tiếng Việt** là ngôn ngữ giao diện chính — copy, label, thông báo đều bằng tiếng Việt.
- Tên sản phẩm: **Gutim** (AI coach character) và **TrackFit** (system/repo name) — cả hai đang tồn tại song song; "Gutim" là tên người dùng thấy.
- Không có ràng buộc cứng về palette hay tên domain — có thể điều chỉnh theo từng đợt thiết kế.

## Evidence on Hand

- Dashboard Apple Health-style đã implement (ActivityRings, MetricCard, HealthTrendChart, BloodPressureChart).
- BMI scoring với ngưỡng châu Á đã implement trong `RecommendationServiceImpl.java` và `HealthUtils.java`.
- Đang có 1 PR mở (`feat/security-hardening-ui-tokens-ci`) với toàn bộ thay đổi design token + dashboard.
- Chưa có user research hay testimonial thực tế — không fabricate.

## Product Principles

1. **Thể trạng người Việt trước** — mọi chỉ số, gợi ý, ngưỡng đánh giá phải phù hợp người châu Á, không áp chuẩn phương Tây.
2. **Đọc nhanh, hành động ngay** — dashboard phải trả lời "hôm nay tôi cần làm gì?" trong vòng 3 giây nhìn, không cần scroll.
3. **AI là trợ lý, không phải gánh nặng** — gợi ý phải tự nhiên, ngắn gọn, không áp lực; người dùng có thể bỏ qua mà không cảm thấy tệ.
4. **Nhất quán hơn là hoàn hảo** — design system token beats hardcoded hex, mobile-first beats desktop-only tweak.
5. **Một app, không phải ba** — tracking + kế hoạch + AI chatbot trong cùng một luồng, không yêu cầu người dùng nhảy giữa nhiều tool.

## Accessibility & Inclusion

- Ưu tiên contrast đủ theo WCAG AA trên cả light và dark theme.
- Touch target ≥44px cho mobile.
- Không có yêu cầu a11y đặc biệt nào được xác nhận ngoài các tiêu chuẩn cơ bản trên.
