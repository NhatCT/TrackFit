# Gutim. - Ứng dụng Quản lý Sức khỏe & Tập Luyện Cá nhân hóa

**Gutim** là một hệ thống web hỗ trợ người dùng theo dõi tình trạng sức khỏe, xây dựng kế hoạch tập luyện cá nhân hóa và nhận các gợi ý bài tập thông minh tích hợp công nghệ AI. 

Dự án được xây dựng theo kiến trúc hệ thống phân tầng (Client - Server) kết hợp với các dịch vụ Microservice xử lý khuyến nghị AI độc lập.

## Công Nghệ Sử Dụng

### 1. Backend (API Server)
- **Framework**: Spring MVC (Spring 6.2.x) & Hibernate 6.x
- **Bảo mật**: Spring Security & JSON Web Token (JWT)
- **Cơ sở dữ liệu**: MySQL 8.4
- **Lưu trữ hình ảnh**: Cloudinary API
- **Thời gian thực**: WebSocket / STOMP

### 2. Frontend (Client SPA)
- **Thư viện**: ReactJS 19.x & React Router Dom
- **Giao diện**: CSS Vanilla, Bootstrap 5 & React Bootstrap
- **Biểu đồ & Hiệu ứng**: Chart.js, AOS (Animate On Scroll)
- **Kết nối API**: Axios

### 3. AI Recommendation Microservice (`ai-reco`)
- **Framework**: FastAPI (Python 3.11)
- **Mô hình nhúng**: Sentence Transformer (`all-MiniLM-L6-v2` / `BAAI/bge-m3`)
- **Tìm kiếm tương đồng**: FAISS (Facebook AI Similarity Search)
- **LLM Chatbot**: OpenAI API (`gpt-4o-mini`)

### 4. DevOps & Triển khai
- Đóng gói và ảo hóa: **Docker** & **Docker Compose**
- Tự động hóa: **GitHub Actions** (CI/CD) & **Docker Hub**

---

## Các Tính Năng Chính

### Dành cho Người dùng (User / Trainee)
- **Quản lý tài khoản**: Đăng ký, đăng nhập bảo mật bằng JWT lưu trữ cookie.
- **Theo dõi chỉ số sức khỏe**: Nhập chiều cao, cân nặng, huyết áp, tự động tính BMI và lưu trữ lịch sử biến đổi thể trạng.
- **Quản lý kế hoạch tập luyện**: Lập kế hoạch tập chi tiết theo từng ngày trong tuần, thêm bài tập, thiết lập thời lượng.
- **Thống kê trực quan**: Biểu đồ cột/tròn theo dõi tổng số phút luyện tập, số buổi tập và tỷ lệ các bài tập trong vòng 30 ngày.
- **Gợi ý bài tập cá nhân hóa (AI Agent)**: Nhận đề xuất danh sách bài tập tối ưu dựa trên chỉ số sức khỏe, mục tiêu luyện tập và lịch sử tập luyện.
- **Trợ lý sức khỏe Chatbot**: Trò chuyện trực tiếp với AI tư vấn về dinh dưỡng, kỹ thuật thực hiện bài tập và chế độ sinh hoạt.

### Dành cho Quản trị viên (Admin)
- **Quản lý bài tập**: Thêm, sửa, xóa danh sách bài tập cùng video hướng dẫn (YouTube).
- **Quản lý kế hoạch mẫu**: Thiết lập kế hoạch tập luyện mẫu 7 ngày cho các mục tiêu phổ biến (giảm cân, tăng cơ, tăng thể lực...).
- **Gửi lời khuyên tự động**: Hệ thống định kỳ chạy Job quét dữ liệu và tạo lời khuyên cho từng người dùng dựa trên AI.

---

## Kiến Trúc Hệ Thống & Luồng AI

### Sơ đồ kiến trúc tổng quan
Hệ thống sử dụng mô hình Client - Server tách biệt hoàn toàn:
1. **React SPA** gửi yêu cầu đến **Spring Backend** thông qua RESTful API.
2. **Spring Backend** xử lý nghiệp vụ, giao tiếp với **MySQL** và hoạt động như một Proxy chuyển tiếp yêu cầu đến **AI Microservice**.
3. **AI Microservice** thực hiện các tính toán nặng (embedding, tìm kiếm vector) và gọi API OpenAI để phản hồi Chatbot.

### Thuật toán gợi ý bài tập (AI-Reco)
1. Dùng mô hình **Sentence Transformer** để tạo vector embedding từ thông tin sức khỏe/mục tiêu của người dùng và các bài tập.
2. Dùng thư viện **FAISS** để tìm kiếm nhanh các bài tập có vector tương đồng nhất.
3. Áp dụng công thức xếp hạng tối ưu điểm số phù hợp tổng thể:
   $$Score = \alpha \times Similarity + \beta \times Novelty + \gamma \times TimeFit$$
   - Trong đó:
     - $Similarity$: Độ tương đồng cosine giữa vector yêu cầu và bài tập.
     - $Novelty$: Điểm mới mẻ tránh lặp lại bài tập cũ quá nhiều.
     - $TimeFit$: Độ khớp giữa thời lượng bài tập và quỹ thời gian mong muốn của người dùng.

---

## Hướng Dẫn Khởi Chạy Dự Án

### Cách 1: Chạy bằng Docker Compose (Khuyên dùng)
Yêu cầu đã cài đặt **Docker Desktop** trên máy.

1. Khởi chạy toàn bộ hệ thống (MySQL, FastAPI, Tomcat Backend, Nginx Frontend):
   ```bash
   docker compose up --build -d
   ```
2. Truy cập ứng dụng tại các địa chỉ:
   - **Frontend React**: [http://localhost:3000](http://localhost:3000)
   - **Backend API**: [http://localhost:8080/TrackFit/](http://localhost:8080/TrackFit/)
   - **AI API**: [http://localhost:8000](http://localhost:8000)
3. Tắt hệ thống:
   ```bash
   docker compose down
   ```

### Cách 2: Chạy độc lập dưới Local (Development)

#### 1. Khởi chạy Database
```bash
docker compose up -d mysql
```
(Nạp cơ sở dữ liệu ban đầu từ file `trackfit.sql`)

#### 2. Khởi chạy AI Service (`ai-reco`)
```bash
cd ai-reco
.venv\Scripts\activate  # Đối với Windows
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

#### 3. Khởi chạy Backend (`TrackFitApp`)
Mở thư mục `TrackFitApp` bằng IDE hỗ trợ Java (như IntelliJ IDEA hoặc NetBeans), cấu hình Local Tomcat (Tomcat 10+) và Deploy dự án dưới dạng artifact `TrackFitApp:war exploded`.
(Lưu ý điều chỉnh cấu hình kết nối DB trong `databases.properties` nếu cần thiết).

#### 4. Khởi chạy Frontend (`trackfitweb`)
```bash
cd trackfitweb
npm install
npm start
```
Giao diện React sẽ chạy tại [http://localhost:3000](http://localhost:3000).
