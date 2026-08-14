-- 工单流程增强（A组）升级脚本
-- 功能：业主取消/催单、客服转派/驳回、超时管理、留言板、预约上门、维修工转单/协助

-- 1. repair_order 表新增字段
ALTER TABLE repair_order
    ADD COLUMN urge_count INT DEFAULT 0 COMMENT '催单次数' AFTER is_timeout,
    ADD COLUMN last_urge_at DATETIME DEFAULT NULL COMMENT '最近催单时间' AFTER urge_count,
    ADD COLUMN close_reason VARCHAR(255) DEFAULT NULL COMMENT '关闭原因（取消/驳回/超时）' AFTER last_urge_at,
    ADD COLUMN closed_by INT DEFAULT NULL COMMENT '关闭人ID（业主取消=业主ID，客服驳回=客服ID，超时自动关闭=NULL）' AFTER close_reason,
    ADD COLUMN closed_at DATETIME DEFAULT NULL COMMENT '关闭时间' AFTER closed_by,
    ADD COLUMN preferred_time_start DATETIME DEFAULT NULL COMMENT '期望上门开始时间' AFTER closed_at,
    ADD COLUMN preferred_time_end DATETIME DEFAULT NULL COMMENT '期望上门结束时间' AFTER preferred_time_start,
    ADD COLUMN confirmed_time DATETIME DEFAULT NULL COMMENT '维修工确认上门时间' AFTER preferred_time_end,
    ADD COLUMN helper_id INT DEFAULT NULL COMMENT '协助维修工ID（申请协助同意后加派）' AFTER confirmed_time;

-- 2. 工单留言表
CREATE TABLE IF NOT EXISTS order_message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL COMMENT '报修单ID',
    sender_id INT NOT NULL COMMENT '发送人ID',
    sender_role TINYINT NOT NULL COMMENT '发送人角色：1-业主 2-客服 3-维修工',
    content VARCHAR(500) NOT NULL COMMENT '留言内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单留言表';

-- 3. 转派记录表
CREATE TABLE IF NOT EXISTS order_reassign_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL COMMENT '报修单ID',
    old_worker_id INT DEFAULT NULL COMMENT '原维修工ID',
    new_worker_id INT NOT NULL COMMENT '新维修工ID',
    dispatcher_id INT NOT NULL COMMENT '操作客服ID',
    source TINYINT DEFAULT 1 COMMENT '来源：1-客服转派 2-转单同意 3-协助加派',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转派记录表';

-- 4. 转单/协助申请表
CREATE TABLE IF NOT EXISTS order_transfer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL COMMENT '报修单ID',
    from_worker_id INT NOT NULL COMMENT '发起维修工ID',
    type TINYINT DEFAULT 1 COMMENT '类型：1-转单 2-申请协助',
    reason VARCHAR(255) NOT NULL COMMENT '申请原因',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-已同意 2-已拒绝',
    dispatcher_id INT DEFAULT NULL COMMENT '处理客服ID',
    to_worker_id INT DEFAULT NULL COMMENT '同意时指定的维修工ID',
    handle_note VARCHAR(255) DEFAULT NULL COMMENT '处理备注（拒绝原因）',
    handled_at DATETIME DEFAULT NULL COMMENT '处理时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    INDEX idx_order_id (order_id),
    INDEX idx_from_worker (from_worker_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转单/协助申请表';
