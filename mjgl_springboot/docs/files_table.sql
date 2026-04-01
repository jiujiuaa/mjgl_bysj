-- 通用业务文件表（模具、订单等多业务通过 biz_type + biz_id 关联）
-- 若当前表无数据或可重建，可直接执行下方 CREATE 替换；若有数据需保留，用下方「迁移方案」

-- ========== 方案一：新建表（无历史数据时使用） ==========
DROP TABLE IF EXISTS `files`;
CREATE TABLE `files` (
  `id` char(32) NOT NULL COMMENT '文件唯一ID',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型：mold=模具, order=订单 等',
  `biz_id` char(32) NOT NULL COMMENT '业务主键ID（如模具id、订单id）',
  `file_type` varchar(20) NOT NULL COMMENT '类型：photo/drawing/bom/manual/repair_photo',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_name` varchar(255) DEFAULT NULL COMMENT 'MinIO 对象名（用于删除）',
  `file_path` varchar(500) NOT NULL COMMENT '存储路径或访问 URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `upload_user_id` char(32) DEFAULT NULL COMMENT '上传人ID，逻辑关联 users.id',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `description` text COMMENT '文件说明',
  `image_status` varchar(32) DEFAULT NULL COMMENT '图片所属业务状态：如 REPORT/FINISH 等',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`, `biz_id`) COMMENT '按业务查文件列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用业务文件表（模具/订单等）';


-- ========== 方案二：在现有表上迁移（保留 mold 历史数据） ==========
-- 1. 增加新字段
-- ALTER TABLE `files`
--   ADD COLUMN `biz_type` varchar(32) NULL COMMENT '业务类型：mold=模具, order=订单 等' AFTER `id`,
--   ADD COLUMN `biz_id` char(32) NULL COMMENT '业务主键ID' AFTER `biz_type`,
--   ADD COLUMN `file_name` varchar(255) NULL COMMENT 'MinIO 对象名（用于删除）' AFTER `original_name`;
--
-- 2. 历史数据回填（原 mold_id 转为 biz_type + biz_id）
-- UPDATE `files` SET `biz_type` = 'mold', `biz_id` = `mold_id` WHERE `mold_id` IS NOT NULL;
--
-- 3. 改为非空并加索引
-- ALTER TABLE `files`
--   MODIFY COLUMN `biz_type` varchar(32) NOT NULL COMMENT '业务类型：mold=模具, order=订单 等',
--   MODIFY COLUMN `biz_id` char(32) NOT NULL COMMENT '业务主键ID',
--   ADD KEY `idx_biz` (`biz_type`, `biz_id`);
--
-- 4. 删除旧字段
-- ALTER TABLE `files` DROP COLUMN `mold_id`;
