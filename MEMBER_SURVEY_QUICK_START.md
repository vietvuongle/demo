# Hướng Dẫn Sử Dụng Hệ Thống Làm Khảo Sát - Thành Viên

## I. Yêu Cầu

### Prerequisites

- Java 11+
- Maven 3.6+
- MySQL 8.0+
- Spring Boot 3.x+

### Cơ Sở Dữ Liệu

Các bảng sau phải được tạo:

- `survey` - Khảo sát
- `survey_member` - Gán khảo sát cho thành viên
- `question_group` - Nhóm câu hỏi
- `survey_group` - Liên kết survey và question_group
- `question` - Câu hỏi
- `question_option` - Lựa chọn cho câu hỏi
- `survey_submission` - Bài nộp khảo sát
- `survey_answer` - Câu trả lời
- `user` - Người dùng
- `role` - Vai trò

---

## II. Thiết Lập Dự Án

### 1. Clone hoặc import dự án

```bash
cd d:\OJT\demo
```

### 2. Cập nhật application.yaml

```yaml
spring:
    datasource:
        url: jdbc:mysql://localhost:3306/survey_db
        username: root
        password: your_password
    jpa:
        hibernate:
            ddl-auto: update
```

### 3. Build dự án

```bash
mvn clean install
```

### 4. Chạy ứng dụng

```bash
mvn spring-boot:run
```

---

## III. Quy Trình Sử Dụng

### A. Đăng Nhập

1. Truy cập: `http://localhost:8080/login`
2. Nhập tên người dùng và mật khẩu
3. Vai trò phải là **MEMBER**

### B. Xem Bảng Điều Khiển

1. Sau khi đăng nhập, tự động redirect đến `/member/dashboard`
2. Hiển thị:
    - Tổng số khảo sát
    - Số khảo sát đã hoàn thành
    - Danh sách khảo sát chưa hoàn thành
    - Danh sách khảo sát đã hoàn thành

### C. Làm Khảo Sát

1. Từ dashboard, nhấn nút **"Làm Khảo Sát"** trên khảo sát cần làm
2. Điền đầy đủ các câu hỏi bắt buộc (có dấu \*)
3. Xem tiến độ hoàn thành trên progress bar
4. Nhấn **"Gửi Khảo Sát"** để hoàn thành

**Lưu ý:**

- Không thể sửa câu trả lời sau khi gửi
- Phải trả lời tất cả câu bắt buộc

### D. Xem Chi Tiết Khảo Sát Đã Hoàn Thành

1. Từ dashboard, nhấn **"Xem Chi Tiết"** trên khảo sát đã hoàn thành
2. Xem lại tất cả câu trả lời
3. Có thể quay lại dashboard để xem khảo sát khác

---

## IV. Loại Câu Hỏi

### 1. Văn Bản Ngắn (SHORT_TEXT)

- Input field cho văn bản ngắn
- Giới hạn: 500 ký tự
- Ví dụ: Tên, Email, Số điện thoại

### 2. Văn Bản Dài (LONG_TEXT)

- Textarea cho văn bản dài
- Giới hạn: 5000 ký tự
- Ví dụ: Nhận xét, Đề xuất

### 3. Chọn Một (SINGLE_CHOICE)

- Radio buttons - chỉ chọn một
- Ví dụ: Có/Không, Đồng ý/Không đồng ý

### 4. Chọn Nhiều (MULTIPLE_CHOICE)

- Checkboxes - chọn nhiều tuỳ ý
- Ví dụ: Các lựa chọn quan tâm

### 5. Danh Sách Thả Xuống (DROPDOWN)

- Select dropdown
- Ví dụ: Lựa chọn từ danh sách

### 6. Đánh Giá (RATING)

- Radio buttons dạng thang điểm
- Ví dụ: 1-5 sao, Rất tệ - Tuyệt vời

---

## V. Các Tính Năng

### ✓ Đã Hoàn Thành

- [x] Dashboard với thống kê
- [x] Danh sách khảo sát chưa/đã hoàn thành
- [x] Form làm khảo sát với 6 loại câu hỏi
- [x] Validation câu bắt buộc
- [x] Progress bar tracking
- [x] Xem chi tiết khảo sát đã hoàn thành
- [x] Responsive design
- [x] Loading spinner khi gửi
- [x] Error handling
- [x] Security & authorization

### ⚠️ Tính Năng Tương Lai

- [ ] Draft saving
- [ ] Time tracking
- [ ] Export PDF
- [ ] Comments
- [ ] File upload
- [ ] Analytics

---

## VI. API Endpoints

### Public

| Method | Endpoint                  | Mô Tả               |
| ------ | ------------------------- | ------------------- |
| GET    | `/member/dashboard`       | Bảng điều khiển     |
| GET    | `/member/survey/{id}`     | Form khảo sát       |
| POST   | `/member/submit/{id}`     | Gửi khảo sát        |
| GET    | `/member/submission/{id}` | Chi tiết submission |

---

## VII. Database Schema (Chính)

### survey

```sql
CREATE TABLE survey (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATETIME,
    end_date DATETIME,
    status VARCHAR(50),
    created_by BIGINT,
    approved_by BIGINT,
    approved_at DATETIME,
    created_at DATETIME,
    FOREIGN KEY (created_by) REFERENCES user(id),
    FOREIGN KEY (approved_by) REFERENCES user(id)
);
```

### survey_submission

```sql
CREATE TABLE survey_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    survey_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    submitted_at DATETIME,
    UNIQUE KEY uq_submission_survey_user (survey_id, user_id),
    FOREIGN KEY (survey_id) REFERENCES survey(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

### survey_answer

```sql
CREATE TABLE survey_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    option_id BIGINT,
    FOREIGN KEY (submission_id) REFERENCES survey_submission(id),
    FOREIGN KEY (question_id) REFERENCES question(id),
    FOREIGN KEY (option_id) REFERENCES question_option(id)
);
```

---

## VIII. Troubleshooting

### Q: Khảo sát không xuất hiện trong dashboard

**A:**

- Kiểm tra status của survey (phải là APPROVED)
- Kiểm tra startDate/endDate
- Kiểm tra user có trong survey_member không

### Q: Không thể gửi form

**A:**

- Kiểm tra console for validation errors
- Đảm bảo tất cả câu bắt buộc được trả lời
- Check browser console (F12) for JavaScript errors

### Q: Lỗi "Khảo sát không tìm thấy"

**A:**

- Xác thực survey ID trong URL
- Kiểm tra survey có tồn tại trong database
- Refresh page và thử lại

### Q: Không thể xem submission detail

**A:**

- Đảm bảo bạn là người tạo submission
- Kiểm tra submission ID
- Kiểm tra database logs

---

## IX. Performance Tips

1. **Tăng Tốc Độ**
    - Sử dụng browser cache
    - Minimize images
    - Enable gzip compression

2. **Database**
    - Add indexes trên survey_id, user_id
    - Use pagination cho large datasets

3. **Caching**
    ```yaml
    spring:
        cache:
            type: redis
    ```

---

## X. Security Best Practices

1. ✓ Authentication bắt buộc
2. ✓ Authorization checks
3. ✓ CSRF protection (Spring Security)
4. ✓ SQL injection prevention (JPA)
5. ✓ XSS protection (Thymeleaf escaping)

**Khuyến nghị:**

- Sử dụng HTTPS trong production
- Validate dữ liệu input
- Implement rate limiting
- Regular security updates

---

## XI. Logs & Monitoring

### Xem Logs

```bash
tail -f logs/app.log
```

### Log Levels

```yaml
logging:
    level:
        com.example.demo: DEBUG
        org.springframework.web: INFO
        org.hibernate: WARN
```

---

## XII. Contact & Support

Để báo cáo lỗi hoặc yêu cầu tính năng, vui lòng:

1. Kiểm tra logs
2. Xem documentation
3. Liên hệ team phát triển

---

## XIII. Changelog

### Version 1.0.0

- ✅ Initial release
- ✅ Dashboard
- ✅ Survey form
- ✅ 6 question types
- ✅ Responsive UI

---

**Last Updated:** January 24, 2026
**Version:** 1.0.0
**Status:** Production Ready
