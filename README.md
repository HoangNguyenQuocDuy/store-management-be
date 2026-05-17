# Store Management — Backend

## Mô tả hệ thống

Hệ thống gồm 2 nhóm người dùng:

- **Admin** — quản lý danh mục, sản phẩm, theo dõi và xử lý đơn hàng.
- **User** — xem sản phẩm, đặt hàng.

Backend cung cấp REST API, xác thực bằng JWT. Ảnh sản phẩm được lưu trên **AWS S3** (upload trực tiếp từ browser qua presigned URL). Khi đơn hàng được tạo, hệ thống gửi email thông báo qua **AWS SES**.

#Note: hệ thống chỉ demo dùng SES môi trường Sandbox nên phải xác thực email trước khi nhận mail (//TODO)
---

## Các luồng chính

### 1. Đăng nhập / Đăng ký

```
Client → POST /auth/register  →  Tạo user (hash password BCrypt)  →  201
Client → POST /auth/login     →  Xác thực credentials             →  JWT token
```

Mọi request sau đó cần gửi kèm header: `Authorization: Bearer <token>`  
`JwtAuthFilter` chặn request, xác thực token, nạp thông tin user vào `SecurityContext`.

---

### 2. Quản lý sản phẩm (Admin)

```
Admin → POST   /admin/products        →  Tạo sản phẩm mới
Admin → PUT    /admin/products/{id}   →  Cập nhật thông tin / giá / ảnh
Admin → DELETE /admin/products/{id}   →  Xoá sản phẩm
```

Upload ảnh sản phẩm:

```
Admin → POST /upload/presigned-url  →  Backend tạo presigned URL từ S3
Admin → PUT <presigned-url>         →  Upload ảnh thẳng lên S3 (không qua backend)
```

---

### 3. Đặt hàng (User)

```
1. User xem danh sách sản phẩm
   GET /products  →  Trả về danh sách (có filter theo danh mục, tìm kiếm, phân trang)

2. User chọn sản phẩm, thêm vào giỏ hàng (xử lý ở frontend)

3. User xác nhận đặt hàng
   POST /orders
   Body: { items: [{ productId, quantity }] }

4. Backend xử lý:
   a. Kiểm tra sản phẩm tồn tại
   b. Tính tổng tiền
   c. Lưu Order + OrderItem vào database
   d. Gửi email xác nhận đơn hàng qua AWS SES (async)
   e. Trả về thông tin đơn hàng vừa tạo

5. User xem lại đơn hàng của mình
   GET /orders  →  Danh sách đơn hàng của user đang đăng nhập
```

---

### 4. Xử lý đơn hàng (Admin)

```
Admin → GET /admin/orders               →  Xem tất cả đơn hàng (mọi user)
Admin → PUT /admin/orders/{id}/status   →  Cập nhật trạng thái: PENDING → CONFIRMED / CANCELLED -> Gửi mail cho User
```

---

## Công nghệ sử dụng

| Công nghệ | Phiên bản | Mô tả |
|---|---|---|
| Java | 22 | Ngôn ngữ lập trình chính |
| Spring Boot | 3.3.5 | Framework backend |
| Spring Security | 6.x | Xác thực & phân quyền theo role |
| JWT (JJWT) | 0.12.3 | Stateless authentication |
| Spring Data JPA | — | ORM / truy vấn database |
| Hibernate | — | JPA implementation |
| PostgreSQL | — | Hệ quản trị CSDL quan hệ |
| Maven | 3.x | Build & dependency management |
| Lombok | — | Giảm boilerplate code |
| AWS S3 SDK | 2.25.60 | Lưu trữ ảnh sản phẩm |
| AWS SES v2 | 2.25.60 | Gửi email thông báo đơn hàng |
| Thymeleaf | — | Template engine cho email |

---

## Cấu trúc dự án

```
src/main/java/com/seveneleven/
├── config/
│   ├── AsyncConfig.java          # Cấu hình @Async thread pool
│   ├── AWSConfig.java            # Cấu hình AWS S3 / SES client
│   ├── CorsConfig.java           # Cấu hình CORS cho frontend
│   └── SecurityConfig.java       # Spring Security + JWT filter chain
│
├── controller/
│   ├── AuthController.java       # POST /auth/login, /auth/register
│   ├── CategoryController.java   # CRUD danh mục
│   ├── ProductController.java    # Xem sản phẩm (User)
│   ├── OrderController.java      # Tạo & xem đơn hàng (User)
│   └── UploadController.java     # Lấy presigned URL upload S3
│
├── dto/
│   ├── auth/                     # LoginRequest, RegisterRequest, AuthResponse
│   ├── category/                 # CategoryDto, CategoryRequest
│   ├── order/                    # OrderRequest, OrderResponse, OrderItemDto
│   └── product/                  # ProductRequest, ProductResponse
│
├── entity/
│   ├── User.java                 # Người dùng hệ thống
│   ├── UserRole.java             # Enum: ADMIN | USER
│   ├── Category.java             # Danh mục sản phẩm
│   ├── Product.java              # Sản phẩm
│   ├── Order.java                # Đơn hàng
│   ├── OrderItem.java            # Chi tiết từng sản phẩm trong đơn
│   └── OrderStatus.java          # Enum: PENDING | CONFIRMED | CANCELLED
│
├── exception/
│   ├── GlobalExceptionHandler.java   # Xử lý lỗi toàn cục (@RestControllerAdvice)
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
│
├── repository/
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── spec/                     # JPA Specifications — filter động (tìm kiếm, lọc)
│
├── security/
│   ├── JwtUtil.java              # Tạo, ký, và xác thực JWT
│   ├── JwtAuthFilter.java        # Filter thêm authentication vào SecurityContext
│   └── UserDetailsServiceImpl.java
│
└── service/
    ├── interfaces/               # Service interfaces (AuthService, ProductService, ...)
    └── impl/                     # Implementations
```

```
src/main/resources/
├── application.yml               # Cấu hình chính (db, jwt, aws, server)
```

---

## Database

**PostgreSQL** được host trên **Supabase**.

| Thông số | Giá trị |
|---|---|
| Host | `aws-1-ap-northeast-2.pooler.supabase.com` |
| Port | `6543` (connection pooler) |
| Database | `postgres` |
| Username | `postgres.xukzxkdnwfypjacbzexs` |
| Password | Biến môi trường `DB_PASSWORD` |
| Driver | `org.postgresql.Driver` |
| DDL | `ddl-auto: update` (tự tạo/cập nhật schema) |

---

## API Endpoints

Base URL: `http://localhost:8080/api/v1`

### Auth

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/auth/login` | Đăng nhập, trả về JWT | Public |
| POST | `/auth/register` | Đăng ký tài khoản mới | Public |

### Products (User)

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/products` | Danh sách sản phẩm (có filter/phân trang) | Public |
| GET | `/products/{id}` | Chi tiết sản phẩm | Public |

### Products (Admin)

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/admin/products` | Tạo sản phẩm mới | Admin |
| PUT | `/admin/products/{id}` | Cập nhật sản phẩm | Admin |
| DELETE | `/admin/products/{id}` | Xoá sản phẩm | Admin |

### Categories

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/categories` | Danh sách danh mục | Public |
| POST | `/admin/categories` | Tạo danh mục | Admin |

### Orders (User)

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/orders` | Tạo đơn hàng mới | User |
| GET | `/orders` | Xem đơn hàng của tôi | User |

### Orders (Admin)

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/admin/orders` | Xem tất cả đơn hàng | Admin |
| PUT | `/admin/orders/{id}/status` | Cập nhật trạng thái đơn | Admin |

### Upload

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/upload/presigned-url` | Lấy presigned URL upload S3 | Auth |

---
## Biến môi trường

| Biến | Mô tả | Bắt buộc                     |
|---|---|------------------------------|
| `DB_PASSWORD` | Mật khẩu PostgreSQL Supabase | Có                           |
| `JWT_SECRET` | Secret key ký JWT | Không (có default)           |
| `AWS_ENABLED` | Bật tích hợp AWS (S3, SES) | Không (default: `true`)      |
| `AWS_ACCESS_KEY` | AWS Access Key ID | Khi `AWS_ENABLED=true`       |
| `AWS_SECRET_KEY` | AWS Secret Access Key | Khi `AWS_ENABLED=true`       |
| `AWS_REGION` | AWS Region | Không (default: `us-east-1`) |
| `AWS_BUCKET` | S3 Bucket name | Khi `AWS_ENABLED=true`       |

---

## Tài khoản mặc định (Seed Data)

| Role  | username | Password   |
|-------|----------|------------|
| Admin | `admin`  | `admin123` |
| User   | `user`   | `user123`  |
