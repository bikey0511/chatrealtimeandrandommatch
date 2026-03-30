# ChatOnlineDemo

Ứng dụng chat realtime (Spring Boot): đăng ký/đăng nhập JWT, chat 1-1, ghép ngẫu nhiên, báo cáo, thông báo, quản trị (ban user, duyệt tin nhắn bị gắn cờ).

## Yêu cầu môi trường

- **JDK 17**
- **Maven 3.8+**
- **MySQL 8** (chạy local, cổng mặc định `3306`)

## Cài đặt nhanh

### 1. Tạo database (tùy chọn)

Ứng dụng dùng URL có `createDatabaseIfNotExist=true`, nên MySQL có thể tự tạo schema `chatonlinedemo` khi kết nối lần đầu. Bạn vẫn cần **MySQL đang chạy** và tài khoản có quyền tạo DB.

### 2. Cấu hình kết nối

Sửa `src/main/resources/application.properties` cho khớp máy bạn:

| Thuộc tính | Mô tả |
|------------|--------|
| `spring.datasource.url` | JDBC URL (mặc định `localhost:3306`, database `chatonlinedemo`) |
| `spring.datasource.username` | User MySQL (mặc định `root`) |
| `spring.datasource.password` | Mật khẩu MySQL (mặc định trống) |
| `jwt.secret` | **Đổi trên môi trường production** — chuỗi bí mật ký JWT |

JPA `ddl-auto=update`: Hibernate sẽ cập nhật schema khi chạy (phù hợp dev/demo).

### 3. Chạy ứng dụng

Từ thư mục gốc project:

```bash
.\mvnw.cmd spring-boot:run

```

Hoặc build rồi chạy JAR:

```bash
mvn -q -DskipTests package
java -jar target/chatonlinedemo-1.0.0.jar
```

Mặc định server lắng nghe **cổng 8080**.

### 4. Tài khoản mẫu (tự tạo khi khởi động)

`DataInitializer` tạo user nếu chưa có:

| Vai trò | Username | Password |
|---------|----------|----------|
| Admin   | `admin`  | `admin123` |
| User    | `user1`, `user2`, `user3` | `password123` |

## Sử dụng qua trình duyệt

Sau khi server chạy, mở:

| Đường dẫn | Mô tả |
|-----------|--------|
| http://localhost:8080/ | Chuyển tới đăng nhập (`signin.html`) |
| http://localhost:8080/signin.html | Đăng nhập |
| http://localhost:8080/signup.html | Đăng ký |
| http://localhost:8080/chat.html | Màn chat (cần token sau khi đăng nhập — theo logic trong file) |
| http://localhost:8080/admin hoặc `/admin.html` | Trang admin (đăng nhập bằng `admin`) |

## API & tài liệu

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON:** http://localhost:8080/api-docs  

Các nhóm API chính (cần header `Authorization: Bearer <access_token>` trừ `/api/auth/**`):

- `/api/auth` — đăng ký, đăng nhập, refresh, logout  
- `/api/users` — hồ sơ, online, chặn user  
- `/api/conversations`, `/api/messages` — REST chat (chi tiết xem Swagger)  
- `/api/match` — tìm match, hủy, lịch sử  
- `/api/reports`, `/api/notifications` — báo cáo & thông báo  
- `/api/admin/**` — chỉ user có role **ADMIN**  

## WebSocket (realtime)

- Endpoint STOMP: **`/ws`** (hỗ trợ SockJS và WebSocket thuần).  
- Prefix gửi message từ client: **`/app`** (ví dụ map tới `/app/chat.sendMessage` trong code).  
- Broker: `/topic`, `/queue`; user destinations: prefix **`/user`**.

Khi tích hợp client mới, bật CORS đã cấu hình sẵn cho origin dev; production nên thu hẹp origin.

## Upload file

`file.upload.dir=./uploads` — thư mục lưu file tương đối so với process chạy app. Static **`/uploads/**`** được phép truy cập không cần JWT (xem `SecurityConfig`).

## Gỡ lỗi thường gặp

- **Không kết nối được MySQL:** kiểm tra service MySQL, username/password, port 3306, firewall.  
- **Port 8080 bận:** đổi `server.port` trong `application.properties`.  
- **401/403 khi gọi API:** kiểm tra token hết hạn, hoặc user không đủ quyền (admin API).  

## Cấu trúc thư mục (tóm tắt)

- `src/main/java/...` — mã nguồn Spring Boot  
- `src/main/resources/static/` — `signin.html`, `signup.html`, `chat.html`, `admin.html`  
- `inapp-1.0.0/` — gói frontend Vite mẫu (tách biệt, không bắt buộc để chạy backend)

---

*Demo học tập — đổi `jwt.secret` và tắt `show-sql` khi triển khai thật.*
