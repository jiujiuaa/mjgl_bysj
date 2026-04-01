-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: db_mjgl
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `abnormal_records`
--

DROP TABLE IF EXISTS `abnormal_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `abnormal_records` (
  `id` char(32) NOT NULL COMMENT '异常记录唯一ID，32位UUID',
  `mold_id` char(32) NOT NULL COMMENT '关联模具ID，逻辑关联 molds.id',
  `abnormal_type` tinyint unsigned NOT NULL COMMENT '异常类型：1=温度异常, 2=润滑异常, 3=振动异常, 4=外观损伤, 5=尺寸超差, 6=其他',
  `measured_value` varchar(100) DEFAULT NULL COMMENT '实测值（如：120℃, 液位30%, 裂纹长5mm）',
  `threshold_value` varchar(100) DEFAULT NULL COMMENT '阈值/标准值（如：≤100℃, ≥50%）',
  `description` text COMMENT '异常详细描述（人工填写补充信息）',
  `occurred_at` datetime NOT NULL COMMENT '异常发生/发现时间（由用户输入或系统自动记录）',
  `source_type` tinyint unsigned NOT NULL COMMENT '数据来源：1=人工巡检, 2=传感器自动上报, 3=维修过程发现, 4=试模发现',
  `operator_id` char(32) DEFAULT NULL COMMENT '操作人ID（人工录入时必填，逻辑关联 users.id）',
  `device_id` varchar(50) DEFAULT NULL COMMENT '设备/传感器ID（自动上报时填写）',
  `linked_repair_id` char(32) DEFAULT NULL COMMENT '关联的维修记录ID（若此异常已转为维修）',
  `linked_log_id` char(32) DEFAULT NULL COMMENT '关联的日志ID（如 temperature_logs.id）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具异常事件原始记录表（支持人工+自动）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `alerts`
--

DROP TABLE IF EXISTS `alerts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alerts` (
  `id` char(32) NOT NULL COMMENT '报警记录唯一ID，32位UUID',
  `mold_id` char(32) NOT NULL COMMENT '关联模具ID，逻辑关联 molds.id',
  `alert_type` tinyint unsigned NOT NULL COMMENT '报警类型：1=故障频发, 2=保养超期, 3=温度异常, 4=维修成本过高',
  `trigger_condition` text COMMENT '触发条件描述，如：近30天故障≥3次',
  `severity` tinyint unsigned DEFAULT '2' COMMENT '严重等级：1=低, 2=中, 3=高',
  `message` text NOT NULL COMMENT '提示消息',
  `status` tinyint unsigned DEFAULT '1' COMMENT '状态：1=活跃, 2=已解决, 3=已忽略',
  `resolved_by` char(32) DEFAULT NULL COMMENT '处理人ID，逻辑关联 users.id',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报警生成时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具异常报警与智能预警记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `authorities`
--

DROP TABLE IF EXISTS `authorities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `authorities` (
  `role` varchar(50) NOT NULL COMMENT '角色名（如 ADMIN, INSPECTOR）',
  `authority` varchar(50) NOT NULL COMMENT '权限标识（建议以 ROLE_ 或 自定义前缀开头）',
  UNIQUE KEY `uk_role_authority` (`role`,`authority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限表（一个角色可拥有多个权限）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `files`
--

DROP TABLE IF EXISTS `files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `files` (
  `id` char(32) NOT NULL COMMENT '文件唯一ID',
  `mold_id` char(32) NOT NULL COMMENT '模具ID，逻辑关联 molds.id',
  `file_type` varchar(20) NOT NULL COMMENT '类型：photo/drawing/bom/manual/repair_photo',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '存储路径，如 /uploads/mold/xxx.jpg',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `upload_user_id` char(32) DEFAULT NULL COMMENT '上传人ID，逻辑关联 users.id',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `description` text COMMENT '文件说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具相关文件与图片存储表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `health_reports`
--

DROP TABLE IF EXISTS `health_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_reports` (
  `id` char(32) NOT NULL COMMENT '健康报告唯一ID，32位UUID',
  `mold_id` char(32) NOT NULL COMMENT '关联模具ID，逻辑关联 molds.id',
  `report_title` varchar(100) DEFAULT '模具健康评估报告' COMMENT '报告标题',
  `report_period_start` date NOT NULL COMMENT '统计周期开始日期',
  `report_period_end` date NOT NULL COMMENT '统计周期结束日期',
  `total_usage_count` int DEFAULT NULL COMMENT '周期内使用次数',
  `total_production_time` decimal(10,2) DEFAULT NULL COMMENT '周期内生产时长（小时）',
  `fault_count` int DEFAULT NULL COMMENT '周期内故障次数',
  `repair_cost_total` decimal(12,2) DEFAULT NULL COMMENT '周期内维修总成本（元）',
  `maintenance_completed_count` int DEFAULT NULL COMMENT '已完成保养次数',
  `maintenance_planned_count` int DEFAULT NULL COMMENT '计划保养次数',
  `maintenance_rate` decimal(5,2) DEFAULT NULL COMMENT '保养完成率（%）',
  `health_score` int DEFAULT '0' COMMENT '综合健康评分（0～100，越高越好）',
  `status` tinyint unsigned DEFAULT '1' COMMENT '报告状态：1=草稿, 2=已生成, 3=已导出',
  `pdf_file_path` varchar(500) DEFAULT NULL COMMENT '生成的PDF文件存储路径',
  `generated_by` char(32) DEFAULT NULL COMMENT '生成人ID，逻辑关联 users.id',
  `generated_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具健康评估报告主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lubrication_logs`
--

DROP TABLE IF EXISTS `lubrication_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lubrication_logs` (
  `id` char(32) NOT NULL COMMENT '润滑日志ID',
  `mold_id` char(32) NOT NULL COMMENT '模具ID，逻辑关联 molds.id',
  `oil_level_percent` decimal(5,2) DEFAULT NULL COMMENT '润滑油液位百分比（0～100）',
  `pressure_kpa` decimal(6,2) DEFAULT NULL COMMENT '压力（kPa）',
  `operator_id` char(32) NOT NULL COMMENT '操作人ID，逻辑关联 users.id',
  `operation_time` datetime NOT NULL COMMENT '实际检测时间（由用户输入）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '系统记录创建时间',
  `description` text COMMENT '备注说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人工填写的模具润滑状态检测日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `maintenance_logs`
--

DROP TABLE IF EXISTS `maintenance_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maintenance_logs` (
  `id` char(32) NOT NULL COMMENT '保养记录ID',
  `mold_id` char(32) NOT NULL COMMENT '模具ID，逻辑关联 molds.id',
  `plan_id` char(32) DEFAULT NULL COMMENT '关联计划ID（可选）',
  `maintainer_id` char(32) NOT NULL COMMENT '保养人ID，逻辑关联 users.id',
  `maintenance_type` varchar(50) DEFAULT NULL COMMENT '保养类型',
  `details` text COMMENT '详细内容',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `cost` decimal(12,2) DEFAULT NULL COMMENT '费用（元）',
  `file_ids` text COMMENT '关联文件ID列表（JSON数组）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具保养操作记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `maintenance_reminders`
--

DROP TABLE IF EXISTS `maintenance_reminders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maintenance_reminders` (
  `id` char(32) NOT NULL COMMENT '保养提醒记录唯一ID，32位UUID',
  `mold_id` char(32) NOT NULL COMMENT '关联模具ID，逻辑关联 molds.id',
  `plan_name` varchar(100) DEFAULT NULL COMMENT '保养计划名称，如：月度点检、年度大修',
  `reminder_type` tinyint unsigned NOT NULL COMMENT '提醒类型：1=按时间周期, 2=按使用次数',
  `interval_value` int NOT NULL COMMENT '间隔值，如30（天）或500（模次）',
  `next_due_date` date DEFAULT NULL COMMENT '下次应保养日期（当reminder_type=1时有效）',
  `next_due_cycles` int DEFAULT NULL COMMENT '下次应保养模次（当reminder_type=2时有效）',
  `status` tinyint unsigned DEFAULT '1' COMMENT '状态：1=待处理, 2=已发送, 3=已完成, 4=已忽略',
  `sent_at` datetime DEFAULT NULL COMMENT '实际推送提醒的时间',
  `operator_id` char(32) DEFAULT NULL COMMENT '处理人ID，逻辑关联 users.id',
  `notes` text COMMENT '备注说明',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具保养智能提醒记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mold_qrcodes`
--

DROP TABLE IF EXISTS `mold_qrcodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mold_qrcodes` (
  `id` char(32) NOT NULL COMMENT '二维码唯一ID（本身也是二维码内容）',
  `mold_id` char(32) NOT NULL COMMENT '关联模具ID，逻辑关联 molds.id',
  `qrcode_type` tinyint unsigned DEFAULT '1' COMMENT '二维码类型：1=模具详情, 2=快速报修, 3=异常上报',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否有效：1=有效, 0=已停用（如模具报废或二维码损坏）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_mold_id` (`mold_id`),
  KEY `idx_id_active` (`id`,`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具二维码定义表（支持多类型、可停用）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mold_specs`
--

DROP TABLE IF EXISTS `mold_specs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mold_specs` (
  `id` char(32) NOT NULL COMMENT '参数记录ID',
  `mold_id` char(32) NOT NULL COMMENT '模具ID，逻辑关联 molds.id',
  `dimensions` varchar(200) DEFAULT NULL COMMENT '外形尺寸（长×宽×高×重）',
  `material` varchar(100) DEFAULT NULL COMMENT '材质',
  `cavity_count` int DEFAULT NULL COMMENT '腔数',
  `design_life_cycles` int DEFAULT NULL COMMENT '设计寿命（次）',
  `design_life_years` int DEFAULT NULL COMMENT '设计寿命（年）',
  `key_dimensions` text COMMENT '关键尺寸与公差',
  `linked_documents` text COMMENT '关联文档路径（JSON或逗号分隔）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `mold_id` (`mold_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具技术参数表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `molds`
--

DROP TABLE IF EXISTS `molds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `molds` (
  `id` char(32) NOT NULL COMMENT '模具唯一标识，32位UUID',
  `mold_code` varchar(50) NOT NULL COMMENT '模具编号（业务唯一）',
  `name` varchar(100) NOT NULL COMMENT '模具名称/型号',
  `category` varchar(50) DEFAULT NULL COMMENT '类别：注塑模、冲压模等',
  `product_project` varchar(100) DEFAULT NULL COMMENT '所属产品或项目',
  `location` varchar(100) DEFAULT NULL COMMENT '存放位置（仓库-货架-库位）',
  `manufacturer` varchar(100) DEFAULT NULL COMMENT '制造商',
  `supplier` varchar(100) DEFAULT NULL COMMENT '供应商',
  `purchase_date` date DEFAULT NULL COMMENT '采购/入库日期',
  `current_status` tinyint unsigned DEFAULT '1' COMMENT '当前状态：1=在库, 2=使用中, 3=维修中, 4=外借, 5=待报废',
  `total_usage_count` int DEFAULT '0' COMMENT '累计使用次数',
  `total_production_time` decimal(10,2) DEFAULT '0.00' COMMENT '累计生产时长（小时）',
  `last_maintenance_date` date DEFAULT NULL COMMENT '最后保养日期',
  `purchase_cost` decimal(12,2) DEFAULT NULL COMMENT '采购成本（元）',
  `total_repair_cost` decimal(12,2) DEFAULT '0.00' COMMENT '累计维修成本（元）',
  `total_maintenance_cost` decimal(12,2) DEFAULT '0.00' COMMENT '累计保养成本（元）',
  `created_by` char(32) DEFAULT NULL COMMENT '创建人ID，逻辑关联 users.id',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `mold_code` (`mold_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具基本信息主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `repair_records`
--

DROP TABLE IF EXISTS `repair_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repair_records` (
  `id` char(32) NOT NULL COMMENT '维修记录ID',
  `mold_id` char(32) NOT NULL COMMENT '模具ID，逻辑关联 molds.id',
  `reporter_id` char(32) DEFAULT NULL COMMENT '送修人ID，逻辑关联 users.id（可为空）',
  `maintainer_id` char(32) DEFAULT NULL COMMENT '维修人ID，逻辑关联 users.id',
  `verifier_id` char(32) DEFAULT NULL COMMENT '验证人ID，逻辑关联 users.id',
  `repair_reason` text COMMENT '故障原因',
  `repair_description` text COMMENT '维修内容',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint unsigned DEFAULT '1' COMMENT '维修状态：1=待处理, 2=维修中, 3=已修复, 4=已验收',
  `cost` decimal(12,2) DEFAULT NULL COMMENT '维修费用（元）',
  `notes` text COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具维修全流程记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `temperature_logs`
--

DROP TABLE IF EXISTS `temperature_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `temperature_logs` (
  `id` char(32) NOT NULL COMMENT '温度日志ID',
  `mold_id` char(32) NOT NULL COMMENT '模具ID，逻辑关联 molds.id',
  `sensor_location` varchar(50) DEFAULT NULL COMMENT '测温位置',
  `temperature` decimal(5,2) NOT NULL COMMENT '温度值（℃）',
  `operator_id` char(32) NOT NULL COMMENT '操作人ID，逻辑关联 users.id',
  `operation_time` datetime NOT NULL COMMENT '实际检测时间（由用户输入）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '系统记录创建时间',
  `description` text COMMENT '备注说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人工填写的模具温度检测日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trial_molding`
--

DROP TABLE IF EXISTS `trial_molding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trial_molding` (
  `id` char(32) NOT NULL COMMENT '试模记录ID',
  `mold_id` char(32) NOT NULL COMMENT '模具ID，逻辑关联 molds.id',
  `operator_id` char(32) NOT NULL COMMENT '操作人ID，逻辑关联 users.id',
  `trial_date` datetime NOT NULL COMMENT '试模时间',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称',
  `trial_results` text COMMENT '试模结果',
  `issues_found` text COMMENT '发现问题',
  `comments` text COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模具试模过程记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` char(32) NOT NULL COMMENT '用户业务ID（32位UUID）',
  `username` varchar(50) NOT NULL COMMENT '登录账号（唯一，如工号/手机号）',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（1=是, 0=否）',
  `real_name` varchar(50) NOT NULL COMMENT '姓名',
  `age` int unsigned DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `role` varchar(50) NOT NULL DEFAULT 'USER' COMMENT '用户角色（如 ADMIN, INSPECTOR）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表（32位ID主键，兼容Spring Security）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'db_mjgl'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-03 16:25:02
