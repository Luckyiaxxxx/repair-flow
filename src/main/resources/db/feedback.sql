-- 投诉建议表
CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT NOT NULL COMMENT '业主ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type VARCHAR(50) DEFAULT NULL COMMENT '类型：complaint-投诉 suggestion-建议',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-已处理',
    reply TEXT DEFAULT NULL COMMENT '回复内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉建议表';