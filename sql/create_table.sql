# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员小新</a>
#

-- 创建库
create database if not exists xin_Chat;

-- 切换库
use xin_Chat;

-- 用户表
create table if not exists user
(
    id           varchar(12)                           not null comment 'id' primary key,
    email        varchar(50)                            not null comment '邮箱',
    userPassword varchar(32)                           not null comment '密码',
    userName     varchar(50)                           null comment '用户昵称',
    joinType     tinyint(1)                             null comment '0:直接加入，1：同意后添加好友',
    Sex          tinyint(1)                             null comment '1：男,0:女',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    lastLoginTime datetime                              not null comment '最后登录时间',
    areaName      varchar(50)                           null comment '地区',
    areaCode      varchar(50)                           null comment '地区编码',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    lastOffTime  bigint(13)                             null comment '最后下线时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_Email (email)
) comment '用户信息表' collate = utf8mb4_unicode_ci;


/*
 Navicat Premium Data Transfer

 Source Server         : 腾讯云
 Source Server Type    : MySQL
 Source Schema         : xin_chat

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 09/06/2024 11:35:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for UserBeauty
-- ----------------------------
DROP TABLE IF EXISTS UserBeauty;
CREATE TABLE `userBeauty`  (
                               `id` int NOT NULL AUTO_INCREMENT,
                               `userId` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
                               `status` tinyint(1) NULL DEFAULT NULL COMMENT '0：未使用 1：已使用',
                               `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户邮箱',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `idx_key_userId`(`userId` ASC) USING BTREE,
                               UNIQUE INDEX `idx_key_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '靓号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of UserBeauty
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;




