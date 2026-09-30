# db-schema/ — DB init & seed

Mọi file `*.sql` trong thư mục này chạy MỘT LẦN ở lần start đầu tiên của container `db`
(chuẩn postgres `/docker-entrypoint-initdb.d`, thứ tự alphabet — đặt tên `01_...`, `02_...`).

- Schema ứng dụng do Flyway trong backend tự tạo (`V1__schema.sql`, `baseline-on-migrate`) —
  thư mục này chủ yếu dành cho **seed data** (hoặc full dump schema+data; Flyway sẽ baseline).
- Muốn chạy lại init từ đầu: `docker compose down -v` (xoá volume `dbdata`) rồi `up --build`.
