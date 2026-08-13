-- 物料消耗记录表
CREATE TABLE IF NOT EXISTS material_consumption (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL COMMENT '报修单ID',
    material_id INT NOT NULL COMMENT '物料ID',
    quantity INT NOT NULL COMMENT '消耗数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料消耗记录';