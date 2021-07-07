INSERT INTO nacos.his_config_info
(id, nid, data_id, group_id, app_name, content, md5, gmt_create, gmt_modified, src_user, src_ip, op_type, tenant_id)
VALUES(0, 1, 'seataServer.properties', 'SEATA_GROUP', '', 'store.mode=db
store.db.dbType=mysql
store.db.driverClassName=com.mysql.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true&rewriteBatchedStatements=true
store.db.user=root
store.db.password=mysql
# db模式数据库初始连接数	默认1
store.db.minConn=1
# db模式数据库最大连接数	默认20
store.db.maxConn=20
# db模式获取连接时最大等待时间	默认5000，单位毫秒
store.db.maxWait=5000
# db模式全局事务表名	默认global_table
store.db.globalTable=global_table
# db模式分支事务表名	默认branch_table
store.db.branchTable=branch_table
# db模式全局锁表名	默认lock_table
store.db.lockTable=lock_table
# db模式查询全局事务一次的最大条数	默认100
store.db.queryLimit=100', '83d7b8d1254a6decba2b3114457cd5b2', '2021-07-06 11:38:48', '2021-07-06 19:38:48', NULL, '0:0:0:0:0:0:0:1', 'I', '');

INSERT INTO nacos.config_info
(id, data_id, group_id, content, md5, gmt_create, gmt_modified, src_user, src_ip, app_name, tenant_id, c_desc, c_use, effect, `type`, c_schema)
VALUES(1, 'seataServer.properties', 'SEATA_GROUP', 'store.mode=db
store.db.dbType=mysql
store.db.driverClassName=com.mysql.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true&rewriteBatchedStatements=true
store.db.user=root
store.db.password=mysql
# db模式数据库初始连接数	默认1
store.db.minConn=1
# db模式数据库最大连接数	默认20
store.db.maxConn=20
# db模式获取连接时最大等待时间	默认5000，单位毫秒
store.db.maxWait=5000
# db模式全局事务表名	默认global_table
store.db.globalTable=global_table
# db模式分支事务表名	默认branch_table
store.db.branchTable=branch_table
# db模式全局锁表名	默认lock_table
store.db.lockTable=lock_table
# db模式查询全局事务一次的最大条数	默认100
store.db.queryLimit=100', '83d7b8d1254a6decba2b3114457cd5b2', '2021-07-06 19:38:48', '2021-07-06 19:38:48', NULL, '0:0:0:0:0:0:0:1', '', '', NULL, NULL, NULL, 'properties', NULL);


use demo;

CREATE TABLE `test_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `test_store` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- for AT mode you must to init this sql for you business database. the seata server not need it.
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8 COMMENT ='AT transaction mode undo table';