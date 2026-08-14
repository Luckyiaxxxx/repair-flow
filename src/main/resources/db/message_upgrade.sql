-- 消息通知体系（C组）升级脚本
-- 功能：站内消息中心（工单状态变更通知+评价提醒）、公告已读/未读红点
-- 注意：Windows 下需用 --default-character-set=utf8mb4 应用，否则中文乱码：
--   mysql --default-character-set=utf8mb4 -uroot -p123456 repair_db < message_upgrade.sql

-- 1. 站内消息表
CREATE TABLE IF NOT EXISTS sys_message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receiver_id INT NOT NULL COMMENT '接收人ID',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1-派单通知 2-接单通知 3-完工通知 4-评价提醒 5-系统通知',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content VARCHAR(500) NOT NULL COMMENT '内容',
    order_id INT DEFAULT NULL COMMENT '关联工单ID',
    is_read TINYINT DEFAULT 0 COMMENT '0-未读 1-已读',
    read_at DATETIME DEFAULT NULL COMMENT '阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_receiver (receiver_id, is_read),
    INDEX idx_order_type (order_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- 2. 公告已读记录表
CREATE TABLE IF NOT EXISTS announcement_read (
    id INT AUTO_INCREMENT PRIMARY KEY,
    announcement_id INT NOT NULL COMMENT '公告ID',
    user_id INT NOT NULL COMMENT '用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY uk_ann_user (announcement_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录表';