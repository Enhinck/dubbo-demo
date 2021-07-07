-- 创建数据库
grant all privileges on *.* to 'root'@'%' identified by 'mysql' with grant option;
flush privileges;

set character set utf8mb4;
CREATE DATABASE  `demo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;