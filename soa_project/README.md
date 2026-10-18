# Product SOA Services

Dự án quản lý sản phẩm theo kiến trúc SOA. Frontend chạy trong container Nginx
riêng; API Gateway và mỗi chức năng backend cũng chạy trong container riêng và
giao tiếp qua HTTP/JSON. Các API sản phẩm được bảo vệ bằng JWT.

## Thành phần và cổng

| Service | Cổng | Chức năng |
| --- | ---: | --- |
| Frontend | 3000 | Giao diện đăng nhập và quản lý sản phẩm |
| API Gateway | 8080 | Điểm vào API; Frontend proxy request qua Gateway |
| Auth | 8081 | Xác thực tài khoản, cấp JWT |
| Product Create | 8082 | Tạo sản phẩm |
| Product Delete | 8083 | Xóa sản phẩm |
| Product Update | 8084 | Cập nhật sản phẩm |
| Product List | 8085 | Liệt kê sản phẩm |
| Product Detail | 8086 | Xem chi tiết sản phẩm |
| Product Data | 8087 | API nội bộ và cơ sở dữ liệu H2 |

Mở giao diện tại `http://localhost:3000`. API Gateway có thể được gọi trực tiếp
tại `http://localhost:8080` khi cần kiểm thử. Các cổng backend được map vào
loopback để tiện kiểm tra cục bộ nhưng không mở trực tiếp ra mạng bên ngoài.
Trong mạng Docker, các service gọi nhau bằng tên service.

## Yêu cầu

- Docker Desktop (Windows/macOS) hoặc Docker Engine cùng Docker Compose v2.
- Java 21 chỉ cần nếu muốn chạy hoặc kiểm thử bằng Maven bên ngoài Docker.

## Chạy toàn bộ bằng Docker Compose

Khởi động Docker Desktop, mở PowerShell tại thư mục gốc dự án và chạy một lệnh
để build image rồi khởi chạy toàn bộ service:

```powershell
docker compose up --build -d
```

Mở giao diện tại <http://localhost:3000>. Đăng nhập bằng
`admin` / `change-me-local`.

Compose tự khởi chạy Frontend, Gateway, các service nghiệp vụ và Product Data
service. Product Data dùng database H2 dạng file trong Docker volume `product-data`;
không cần cài hoặc khởi động database riêng. Dữ liệu vẫn còn sau khi dừng/xóa
container bằng `docker compose down`.

Giá trị mặc định JWT và tài khoản chỉ dành cho chạy thử trên máy cá nhân. Trước
khi triển khai thật, tạo `.env` dựa trên [.env.example](./.env.example), thay
`JWT_SECRET` bằng secret ngẫu nhiên tối thiểu 32 byte, và đổi `AUTH_USERNAME` /
`AUTH_PASSWORD`. File `.env` đã được Git ignore.

Các lệnh quản lý thường dùng:

```powershell
docker compose ps
docker compose logs -f
docker compose logs -f frontend api-gateway auth-service
docker compose down
```

`docker compose down` dừng và xóa container nhưng giữ lại dữ liệu sản phẩm
trong named volume `product-data`. **Không dùng `docker compose down -v` trừ khi
muốn xóa vĩnh viễn volume và toàn bộ dữ liệu sản phẩm.**

### Nếu trình duyệt báo `ERR_CONNECTION_REFUSED`

- Đảm bảo Docker Desktop đã khởi động và Docker Engine đang chạy.
- Kiểm tra trạng thái bằng `docker compose ps`. Nếu container chưa chạy, thực
  hiện `docker compose up --build -d` rồi kiểm tra lại.
- Mở đúng địa chỉ giao diện: <http://localhost:3000>.
- Cổng `8080` là API Gateway, không phải giao diện; trang gốc `/` ở Gateway
  không dùng để đăng nhập.
- Xem lỗi khởi động bằng `docker compose logs -f frontend api-gateway`.

## Bật/tắt từng service và cổng riêng

Mỗi thành phần là container độc lập. Dùng tên service trong `docker-compose.yml`
để tắt hoặc bật riêng; thao tác này không dừng các container khác:

```powershell
docker compose stop frontend
docker compose start frontend

docker compose stop product-create-service
docker compose start product-create-service
```

Thay tên service bằng `api-gateway`, `auth-service`, `product-delete-service`,
`product-update-service`, `product-list-service`, `product-detail-service` hoặc
`product-data-service` để điều khiển từng container tương ứng. Khi dừng một
service, container khác vẫn chạy; chức năng phụ thuộc vào service đã dừng sẽ
tạm thời không dùng được. Ví dụ, dừng Product Data làm các API CRUD không thể
đọc hoặc ghi dữ liệu.

Để chạy lại chỉ một service đã dừng: `docker compose start <service>`. Nếu đã
xóa container bằng lệnh khác, dùng `docker compose up -d <service>` để tạo lại.
Xem container và trạng thái cổng bằng `docker compose ps`.

## Luồng JWT

1. Người dùng đăng nhập trên trang web; trình duyệt gửi thông tin đăng nhập
   đến `POST /auth` qua Nginx Frontend và API Gateway.
2. Auth xác thực thông tin và trả JWT về trình duyệt; giao diện lưu token trong
   `localStorage` của trình duyệt.
3. Với từng thao tác sản phẩm, trình duyệt đọc token và gửi
   `Authorization: Bearer <token>`. Nginx chuyển tiếp header này tới Gateway.
4. Gateway chuyển tiếp token đến service chức năng; service xác minh JWT rồi
   gửi token đến Product Data, nơi token cũng được xác minh trước khi truy cập DB.
5. Token thiếu, không hợp lệ hoặc hết hạn bị từ chối với HTTP `401`; giao diện
   đưa người dùng về trang đăng nhập.

## API sản phẩm

Frontend gọi các API qua Nginx tại `http://localhost:3000`; có thể gọi trực tiếp
qua Gateway tại `http://localhost:8080`. Tất cả API dưới đây cần JWT hợp lệ:

| Method | Đường dẫn | Service |
| --- | --- | --- |
| `POST` | `/products` | Product Create |
| `DELETE` | `/products/{id}` | Product Delete |
| `PUT` | `/products/{id}` | Product Update |
| `GET` | `/products` | Product List |
| `GET` | `/products/{id}` | Product Detail |

## Kiểm thử bằng Maven

Cài Java 21 rồi chạy toàn bộ test Maven từ thư mục gốc bằng PowerShell:

```powershell
.\mvnw.cmd clean test
```
