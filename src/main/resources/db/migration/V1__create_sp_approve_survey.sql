-- Create stored procedure sp_approve_survey
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
        SELECT DISTINCT usr.id AS user_id
        FROM survey_assign_rule sar
        JOIN roles r ON r.code = sar.rule_value
        JOIN user_role ur ON ur.role_id = r.id
        JOIN users usr ON usr.id = ur.user_id AND usr.status = 'ACTIVE'
        WHERE sar.survey_id = p_survey_id
          AND sar.rule_type = 'ROLE'
    ) AS u;

    -- 6) Commit nếu mọi bước đều OK
    COMMIT;

END$$

DELIMITER ;
