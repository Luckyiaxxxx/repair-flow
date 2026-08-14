-- 基础数据与组织管理（B组）升级脚本
-- 功能：报修类别字典、楼栋/单元基础数据、用户管理（禁用/启用/重置密码）、维修工档案、数据字典统一管理
-- 注意：Windows 下需用 --default-character-set=utf8mb4 应用，否则中文乱码：
--   mysql --default-character-set=utf8mb4 -uroot -p123456 repair_db < base_data_upgrade.sql

-- 1. 报修类别字典表
CREATE TABLE IF NOT EXISTS repair_category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '类别名称',
    sort_order INT DEFAULT 0 COMMENT '排序号（越小越靠前）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修类别字典表';

-- 2. 楼栋基础数据表
CREATE TABLE IF NOT EXISTS building (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '楼栋名称',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋基础数据表';

-- 3. 单元基础数据表
CREATE TABLE IF NOT EXISTS unit_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    building_id INT NOT NULL COMMENT '所属楼栋ID',
    name VARCHAR(50) NOT NULL COMMENT '单元名称',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_building_name (building_id, name),
    INDEX idx_building_id (building_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单元基础数据表';

-- 4. 数据字典表（统一管理）
CREATE TABLE IF NOT EXISTS sys_dict (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(50) NOT NULL COMMENT '显示名称',
    dict_value VARCHAR(50) NOT NULL COMMENT '字典值',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_type_value (dict_type, dict_value),
    INDEX idx_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 5. user 表新增维修工档案字段
ALTER TABLE user
    ADD COLUMN on_duty TINYINT DEFAULT 1 COMMENT '在岗状态：1-在岗 0-休息' AFTER max_workload,
    ADD COLUMN service_area VARCHAR(255) DEFAULT NULL COMMENT '服务区域（楼栋名称，逗号分隔）' AFTER on_duty;

-- 6. 种子数据：报修类别
INSERT INTO repair_category (name, sort_order, status) VALUES
('水电维修', 1, 1), ('管道疏通', 2, 1), ('门窗维修', 3, 1),
('电器维修', 4, 1), ('墙面地面', 5, 1), ('其他', 99, 1);

-- 7. 种子数据：楼栋/单元
INSERT INTO building (name, sort_order, status) VALUES ('1栋', 1, 1), ('2栋', 2, 1), ('3栋', 3, 1);
INSERT INTO unit_info (building_id, name, sort_order, status) VALUES
(1, '1单元', 1, 1), (1, '2单元', 2, 1), (1, '3单元', 3, 1),
(2, '1单元', 1, 1), (2, '2单元', 2, 1), (3, '1单元', 1, 1);

-- 8. 种子数据：系统字典
INSERT INTO sys_dict (dict_type, dict_label, dict_value, sort_order, status) VALUES
('emergency_level', '普通', '1', 1, 1),
('emergency_level', '紧急', '2', 2, 1),
('emergency_level', '特急', '3', 3, 1),
('order_status', '待派单', '1', 1, 1),
('order_status', '已派单', '2', 2, 1),
('order_status', '维修中', '3', 3, 1),
('order_status', '已完工', '4', 4, 1),
('order_status', '已评价', '5', 5, 1),
('order_status', '已关闭', '6', 6, 1),
('order_status', '超时关闭', '7', 7, 1);