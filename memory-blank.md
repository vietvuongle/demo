Yêu cầu cho hệ thống:

1. Đăng nhập hệ thống

🎯 Mục tiêu

Xây dựng một trang web cho phép user đăng nhập vào hệ thống.

👥 Đối tượng sử dụng

• Các member trong BU PSI

• Hệ thống cho phép CRUD user

• 2 loại quyền:

o USER (MEMBER)

o ADMIN

🔧 Công nghệ & công việc

• Tạo Database

• Dựng project Spring Boot (Web), Java 8

• Quy trình: Study → Estimate → Development

2. Mục đích của hệ thống

• Người quản lý có thể tạo các survey cho các member trong BU thực hiện.

• Người quản lý có thể xem các survey đã tạo và báo cáo thống kê kết quả survey.

• Member có thể thực hiện khảo sát trên hệ thống.

3. Chi tiết yêu cầu chức năng

3.1 Chức năng Quản lý Survey (ADMIN)

• Xem danh sách các survey đã tạo.

• Tạo mới và cập nhật survey.

• Xem báo cáo thống kê kết quả survey của member, bao gồm thông tin chi tiết từng

survey.

3.2 Chức năng Survey (MEMBER)

• Member đăng nhập vào hệ thống và thực hiện survey được assign.

• Chỉ những member được phân công mới thực hiện được survey.

• Survey chỉ active trong thời gian đánh giá.

4. Chi tiết chức năng tạo Survey

Một survey bao gồm:

🔖 Thông tin Survey

• Tiêu đề

• Thời gian thực hiện

• Nội dung khảo sát

🗂 Cấu trúc nội dung Survey

Nội dung được chia thành:

• Group(là group member. Ví dụ như group DEV, group PM, group BA...)

• Question

• Answer (Yes/No), text, option

- Survey có thể gán member, đồng thời Group trong Survey có thể gán cho tất cả hoặc 1 nhóm member riêng. Vd survey khảo sát chất lượng 2026 dành cho all member, trong đó có 2 group question là quest_base (all member) và quest_leader(chỉ dành cho user role leader), ví dụ như thế. Thì survey đó sẽ hiển thị cho all member nhưng với user role leader mới hiển thị các câu hỏi trong quest_leader.

🔁 Tái sử dụng group

• Một survey có thể có nhiều group.

• Group có thể được tái sử dụng ở nhiều survey khác nhau.

👥 Phân công member

• Mỗi survey gán danh sách member cần thực hiện.

• Một số group có thể gắn với các member khác nhau.

✔️ Quy trình phê duyệt

Sau khi survey được tạo:

1. PM review nội dung

2. PM confirm OK → Survey mới được gửi cho member

3. Chức năng thực hiện Survey

• Chỉ member nằm trong danh sách mới được phép làm survey.

• Survey chỉ khả dụng (active) trong khoảng thời gian đánh giá.

6. Report / Báo cáo

Yêu cầu:

• Xây dựng website cho BU quản lý survey.

• Bao gồm:

o Member login & thực hiện khảo sát

o Admin quản lý survey và xem báo cáo

- PM xem và APPROVE servey

• Công việc:

o Phân tích yêu cầu (Study + Q\&A)

o Thiết kế DB

o Xây dựng website bằng Spring Boot

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- USERS
CREATE TABLE users (
id BIGINT PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
full_name VARCHAR(100),
email VARCHAR(255),
status VARCHAR(20), -- ACTIVE/INACTIVE
created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ROLES
CREATE TABLE roles (
id BIGINT PRIMARY KEY,
code VARCHAR(50) NOT NULL UNIQUE, -- ADMIN, MEMBER, LEADER, PM
name VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- USER_ROLE (many-to-many)
CREATE TABLE user_role (
user_id BIGINT NOT NULL,
role_id BIGINT NOT NULL,
PRIMARY KEY (user_id, role_id),
CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SURVEY
CREATE TABLE survey (
id BIGINT PRIMARY KEY,
title VARCHAR(255) NOT NULL,
description TEXT,
start_date DATETIME,
end_date DATETIME,
status VARCHAR(20), -- DRAFT, PENDING, APPROVED, CLOSED
created_by BIGINT,
approved_by BIGINT,
approved_at DATETIME,
created_at DATETIME,
CONSTRAINT fk_s_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
CONSTRAINT fk_s_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SURVEY ASSIGN RULE
CREATE TABLE survey_assign_rule (
id BIGINT PRIMARY KEY,
survey_id BIGINT NOT NULL,
rule_type VARCHAR(50) NOT NULL, -- ALL, ROLE, DEPARTMENT, GROUP
rule_value VARCHAR(100), -- MEMBER, LEADER, DEV, BA...
CONSTRAINT fk_sar_survey FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- QUESTION GROUP (catalog)
CREATE TABLE question_group (
id BIGINT PRIMARY KEY,
code VARCHAR(100) NOT NULL UNIQUE, -- quest_base, quest_leader
name VARCHAR(255),
description TEXT,
target_role VARCHAR(50) -- NULL=ALL, LEADER, PM...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SURVEY_GROUP (map survey <-> question_group)
CREATE TABLE survey_group (
survey_id BIGINT NOT NULL,
group_id BIGINT NOT NULL,
PRIMARY KEY (survey_id, group_id),
CONSTRAINT fk_sg_survey FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE,
CONSTRAINT fk_sg_group FOREIGN KEY (group_id) REFERENCES question_group(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- QUESTION
CREATE TABLE question (
id BIGINT PRIMARY KEY,
group_id BIGINT NOT NULL,
content TEXT NOT NULL,
type VARCHAR(20), -- YES_NO, TEXT, OPTION
required TINYINT(1) DEFAULT 0,
order_index INT,
CONSTRAINT fk_q_group FOREIGN KEY (group_id) REFERENCES question_group(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- QUESTION OPTION
CREATE TABLE question_option (
id BIGINT PRIMARY KEY,
question_id BIGINT NOT NULL,
content VARCHAR(255) NOT NULL,
CONSTRAINT fk_qo_question FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SURVEY_MEMBER (materialized assignees)
CREATE TABLE survey_member (
survey_id BIGINT NOT NULL,
user_id BIGINT NOT NULL,
assigned_at DATETIME,
PRIMARY KEY (survey_id, user_id),
CONSTRAINT fk_sm_survey FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE,
CONSTRAINT fk_sm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SURVEY_SUBMISSION
CREATE TABLE survey_submission (
id BIGINT PRIMARY KEY,
survey_id BIGINT NOT NULL,
user_id BIGINT NOT NULL,
submitted_at DATETIME,
CONSTRAINT uq_submission UNIQUE (survey_id, user_id),
CONSTRAINT fk_ss_survey FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE,
CONSTRAINT fk_ss_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SURVEY_ANSWER
CREATE TABLE survey_answer (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
submission_id BIGINT NOT NULL,
question_id BIGINT NOT NULL,
answer_text TEXT,
option_id BIGINT,
CONSTRAINT fk_sa_submission FOREIGN KEY (submission_id) REFERENCES survey_submission(id) ON DELETE CASCADE,
CONSTRAINT fk_sa_question FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
CONSTRAINT fk_sa_option FOREIGN KEY (option_id) REFERENCES question_option(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- ROLES
INSERT INTO `roles` (`id`,`code`,`name`) VALUES (1,'ADMIN','Quản trị');
INSERT INTO `roles` (`id`,`code`,`name`) VALUES (2,'MEMBER','Thành viên');
INSERT INTO `roles` (`id`,`code`,`name`) VALUES (3,'LEADER','Trưởng nhóm');
INSERT INTO `roles` (`id`,`code`,`name`) VALUES (4,'PM','Quản lý dự án');

-- USERS
INSERT INTO `users` (`id`,`username`,`password`,`full_name`,`email`,`status`,`created_at`) VALUES
(1,'admin','$2a$10$hash_admin','Admin System','admin@example.com','ACTIVE','2025-12-01 09:00:00'),
(2,'pm1','$2a$10$hash_pm1','Phan Minh (PM)','pm1@example.com','ACTIVE','2025-12-01 09:10:00'),
(3,'leader1','$2a$10$hash_lead1','Lê Duy (Leader)','leader1@example.com','ACTIVE','2025-12-05 10:00:00'),
(4,'member1','$2a$10$hash_mem1','Nguyễn An','member1@example.com','ACTIVE','2025-12-05 10:05:00'),
(5,'member2','$2a$10$hash_mem2','Trần Bình','member2@example.com','ACTIVE','2025-12-05 10:06:00'),
(6,'member3','$2a$10$hash_mem3','Phạm Chi','member3@example.com','ACTIVE','2025-12-05 10:07:00'),
(7,'leader2','$2a$10$hash_lead2','Võ Dũng (Leader)','leader2@example.com','ACTIVE','2025-12-06 09:00:00'),
(8,'member4','$2a$10$hash_mem4','Đỗ Em','member4@example.com','ACTIVE','2025-05-01 09:00:00'),
(9,'member5','$2a$10$hash_mem5','Huỳnh Phát','member5@example.com','ACTIVE','2025-05-01 09:05:00'),
(10,'qa_user','$2a$10$hash_qa','QA User','qa@example.com','INACTIVE','2025-05-01 09:10:00');

-- USER_ROLE
INSERT INTO `user_role` (`user_id`,`role_id`) VALUES
(1,1),(2,4),(3,3),(3,2),(4,2),(5,2),(6,2),(7,3),(7,2),(8,2),(9,2);

-- SURVEY
INSERT INTO `survey` (`id`,`title`,`description`,`start_date`,`end_date`,`status`,`created_by`,`approved_by`,`approved_at`,`created_at`) VALUES
(1,'Khảo sát hài lòng Q1/2026','Survey toàn công ty về môi trường làm việc','2026-01-01 00:00:00','2026-02-15 23:59:59','APPROVED',1,2,'2025-12-28 09:00:00','2025-12-20 08:00:00'),
(2,'Khảo sát dành cho Leader 2026','Dành riêng cho các Leader','2026-01-10 00:00:00','2026-02-10 23:59:59','APPROVED',1,2,'2026-01-05 10:00:00','2025-12-25 08:00:00'),
(3,'Khảo sát nội bộ 2025','Đợt khảo sát 2025 đã kết thúc','2025-06-01 00:00:00','2025-06-30 23:59:59','CLOSED',1,2,'2025-05-25 10:00:00','2025-05-20 08:00:00');

-- SURVEY_ASSIGN_RULE
INSERT INTO `survey_assign_rule` (`id`,`survey_id`,`rule_type`,`rule_value`) VALUES
(1,1,'ROLE','MEMBER'),
(2,1,'ROLE','LEADER'),
(3,2,'ROLE','LEADER'),
(4,3,'ALL',NULL);

-- QUESTION_GROUP
INSERT INTO `question_group` (`id`,`code`,`name`,`description`,`target_role`) VALUES
(1,'quest_base','Bộ câu hỏi chung','Áp dụng cho tất cả',NULL),
(2,'quest_leader','Bộ câu hỏi Leader','Chỉ cho Leader','LEADER');

-- SURVEY_GROUP
INSERT INTO `survey_group` (`survey_id`,`group_id`) VALUES
(1,1),(1,2),(2,2),(3,1);

-- QUESTION
INSERT INTO `question` (`id`,`group_id`,`content`,`type`,`required`,`order_index`) VALUES
(1,1,'Bạn có hài lòng với môi trường làm việc?','YES_NO',1,1),
(2,1,'Điều gì bạn muốn cải thiện?','TEXT',0,2),
(3,1,'Bạn đánh giá phúc lợi công ty?','OPTION',1,3),
(4,2,'Bạn có thường xuyên feedback cho team?','YES_NO',1,1),
(5,2,'Khó khăn lớn nhất khi quản lý?','TEXT',0,2),
(6,2,'Bạn hài lòng với hiệu suất team?','OPTION',1,3);

-- QUESTION_OPTION
INSERT INTO `question_option` (`id`,`question_id`,`content`) VALUES
(1,3,'Tốt'),(2,3,'Bình thường'),(3,3,'Cần cải thiện'),
(4,6,'Cao'),(5,6,'Trung bình'),(6,6,'Thấp');

-- SURVEY_MEMBER (materialized)
INSERT INTO `survey_member` (`survey_id`,`user_id`,`assigned_at`) VALUES
(1,3,'2025-12-29 09:00:00'),
(1,4,'2025-12-29 09:00:00'),
(1,5,'2025-12-29 09:00:00'),
(1,6,'2025-12-29 09:00:00'),
(1,7,'2025-12-29 09:00:00'),
(1,8,'2025-12-29 09:00:00'),
(1,9,'2025-12-29 09:00:00'),
(2,3,'2026-01-06 09:00:00'),
(2,7,'2026-01-06 09:00:00'),
(3,4,'2025-05-26 09:00:00'),
(3,8,'2025-05-26 09:00:00');

-- SURVEY_SUBMISSION (mỗi user chỉ submit 1 lần/survey)
INSERT INTO `survey_submission` (`id`,`survey_id`,`user_id`,`submitted_at`) VALUES
(1,1,4,'2026-01-15 09:30:00'),
(2,1,5,'2026-01-16 10:00:00'),
(3,1,3,'2026-01-17 11:00:00'),
(4,1,7,'2026-01-18 14:45:00'),
(5,1,6,'2026-01-18 15:10:00'),
(6,2,3,'2026-01-18 16:00:00'),
(7,2,7,'2026-01-19 09:00:00'),
(8,3,4,'2025-06-10 08:30:00'),
(9,3,8,'2025-06-15 17:45:00');

-- SURVEY_ANSWER (YES/NO tính theo (submission_id + question_id) % 2, OPTION quay vòng theo submission_id)
-- Survey 1 - member1 (submission 1, chỉ quest_base)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(1,1,'YES',NULL),
(1,2,'Tôi muốn cải thiện quy trình review.',NULL),
(1,3,NULL,2);

-- Survey 1 - member2 (submission 2, chỉ quest_base)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(2,1,'NO',NULL),
(2,2,'Tôi muốn cải thiện quy trình review.',NULL),
(2,3,NULL,3);

-- Survey 1 - leader1 (submission 3, quest_base + quest_leader)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(3,1,'YES',NULL),
(3,2,'Tôi muốn cải thiện quy trình review.',NULL),
(3,3,NULL,1),
(3,4,'NO',NULL),
(3,5,'Thiếu thời gian mentoring cho member mới.',NULL),
(3,6,NULL,4);

-- Survey 1 - leader2 (submission 4, quest_base + quest_leader)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(4,1,'NO',NULL),
(4,2,'Tôi muốn cải thiện quy trình review.',NULL),
(4,3,NULL,2),
(4,4,'YES',NULL),
(4,5,'Thiếu thời gian mentoring cho member mới.',NULL),
(4,6,NULL,5);

-- Survey 1 - member3 (submission 5, chỉ quest_base)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(5,1,'YES',NULL),
(5,2,'Tôi muốn cải thiện quy trình review.',NULL),
(5,3,NULL,3);
-- Survey 2 - leader1 (submission 6, quest_leader)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(6,4,'YES',NULL),
(6,5,'Thiếu thời gian mentoring cho member mới.',NULL),
(6,6,NULL,4);

-- Survey 2 - leader2 (submission 7, quest_leader)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(7,4,'NO',NULL),
(7,5,'Thiếu thời gian mentoring cho member mới.',NULL),
(7,6,NULL,5);

-- Survey 3 - member1 (submission 8, quest_base)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(8,1,'NO',NULL),
(8,2,'Tôi muốn cải thiện quy trình review.',NULL),
(8,3,NULL,3);

-- Survey 3 - member4 (submission 9, quest_base)
INSERT INTO `survey_answer` (`submission_id`,`question_id`,`answer_text`,`option_id`) VALUES
(9,1,'YES',NULL),
(9,2,'Tôi muốn cải thiện quy trình review.',NULL),
(9,3,NULL,1);

SET FOREIGN_KEY_CHECKS=1;

---

-- SP: phê duyệt survey và materialize survey_member tại thời điểm APPROVED
-- Yêu cầu MySQL 8.x, engine InnoDB, schema đúng như đã định nghĩa trước đó.

---

DELIMITER $$

CREATE PROCEDURE sp_approve_survey (
IN p_survey_id BIGINT,
IN p_pm_id BIGINT
)
BEGIN
DECLARE v_status VARCHAR(20);
DECLARE v_cnt INT;

    -- Bắt đầu transaction (tự đóng gói trong SP)
    START TRANSACTION;

    -- 1) Khóa bản ghi survey để chống race condition và kiểm tra trạng thái
    SELECT status INTO v_status
    FROM survey
    WHERE id = p_survey_id
    FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Survey không tồn tại';
    END IF;

    IF v_status <> 'PENDING' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Survey phải ở trạng thái PENDING mới được approve';
    END IF;

    -- 2) Validate điều kiện nghiệp vụ tối thiểu trước khi approve
    SELECT COUNT(*) INTO v_cnt FROM survey_group WHERE survey_id = p_survey_id;
    IF v_cnt = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Survey phải có ít nhất 1 question_group';
    END IF;

    SELECT COUNT(*) INTO v_cnt
    FROM question q
    JOIN survey_group sg ON sg.group_id = q.group_id
    WHERE sg.survey_id = p_survey_id;
    IF v_cnt = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Survey phải có ít nhất 1 question';
    END IF;

    SELECT COUNT(*) INTO v_cnt FROM survey_assign_rule WHERE survey_id = p_survey_id;
    IF v_cnt = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Survey phải có ít nhất 1 assign rule';
    END IF;

    -- 3) Cập nhật trạng thái sang APPROVED + ghi dấu thời điểm/người duyệt
    UPDATE survey
    SET status = 'APPROVED',
        approved_at = NOW(),
        approved_by = p_pm_id
    WHERE id = p_survey_id;

    -- 4) (An toàn) Xóa dữ liệu materialized cũ nếu có
    DELETE FROM survey_member WHERE survey_id = p_survey_id;

    -- 5) Materialize user thỏa rule vào survey_member (chỉ lấy user ACTIVE)
    --    Hỗ trợ 2 rule_type: ALL, ROLE. Có thể mở rộng thêm DEPARTMENT/GROUP sau.
    INSERT INTO survey_member (survey_id, user_id, assigned_at)
    SELECT p_survey_id, u.user_id, NOW()
    FROM (
        -- 5.1) ALL -> toàn bộ user ACTIVE
        SELECT DISTINCT usr.id AS user_id
        FROM survey_assign_rule sar
        JOIN users usr ON usr.status = 'ACTIVE'
        WHERE sar.survey_id = p_survey_id
          AND sar.rule_type = 'ALL'

        UNION

-- 5.2) ROLE -> user có role tương ứng (ACTIVE)
SELECT DISTINCT usr.id AS user_idsp_approve_survey
FROM survey_assign_rule sar
JOIN roles r ON r.code = sar.rule_value
JOIN user_role ur ON ur.role_id = r.id
JOIN users usr ON usr.id = ur.user_id AND usr.status = 'ACTIVE'
WHERE sar.survey_id = p_survey_id
AND sar.rule_type = 'ROLE'

        -- (Mở rộng) UNION các rule_type khác nếu có:
        -- UNION
        -- SELECT DISTINCT usr.id
        -- FROM survey_assign_rule sar
        -- JOIN user_department ud ON ...
        -- WHERE sar.rule_type = 'DEPARTMENT' AND sar.rule_value = ud.dept_code
    ) AS u;

    -- 6) Commit nếu mọi bước đều OK
    COMMIT;

END$$

DELIMITER ;
SELECT \* FROM survey_member WHERE survey_id = 1 ORDER BY user_id;users

-- User load danh sách survey thấy được

SELECT s.\*
FROM survey s
JOIN survey_member sm
ON sm.survey_id = s.id
WHERE
sm.user_id = :currentUserId
AND s.status = 'APPROVED'
AND NOW() BETWEEN s.start_date AND s.end_date
AND NOT EXISTS (
SELECT 1
FROM survey_submission ss
WHERE ss.survey_id = s.id
AND ss.user_id = :currentUserId
)
ORDER BY s.start_date DESC;
