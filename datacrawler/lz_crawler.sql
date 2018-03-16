/*
Navicat MariaDB Data Transfer

Source Server         : 本机数据库
Source Server Version : 100213
Source Host           : localhost:3306
Source Database       : lz_crawler

Target Server Type    : MariaDB
Target Server Version : 100213
File Encoding         : 65001

Date: 2018-03-08 09:34:44
*/

## Database
## CREATE DATABASE  `lz_crawler` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for crawler_category
-- ----------------------------
DROP TABLE IF EXISTS `crawler_category`;
CREATE TABLE `crawler_category` (
  `_id` varchar(200) NOT NULL,
  `c_id` varchar(255) DEFAULT '',
  `c_name` varchar(255) NOT NULL COMMENT '分类名称',
  `c_level` int(2) DEFAULT NULL COMMENT '0 顶级\r\n1 二级\r\n2 三级\r\n3 四级\r\n4 五级\r\n',
  `c_islow` int(1) DEFAULT 0 COMMENT '是否最低级级别，1 ： 是  0 ：不是',
  `c_parent` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for crawler_company
-- ----------------------------
DROP TABLE IF EXISTS `crawler_company`;
CREATE TABLE `crawler_company` (
  `_id` varchar(200) NOT NULL,
  `cid` varchar(255) DEFAULT NULL,
  `cName` varchar(255) DEFAULT NULL,
  `cConcat` varchar(255) DEFAULT NULL,
  `cMobile` varchar(255) DEFAULT NULL,
  `cPhone` varchar(255) DEFAULT NULL,
  `cQq` varchar(255) DEFAULT NULL,
  `cAddress` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for crawler_produce
-- ----------------------------
DROP TABLE IF EXISTS `crawler_produce`;
CREATE TABLE `crawler_produce` (
  `_id` varchar(200) NOT NULL COMMENT '数据ID',
  `p_page` int(7) NOT NULL COMMENT '所属Page',
  `pid` varchar(200) DEFAULT '' COMMENT '商品ID',
  `pname` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名',
  `pprice` varchar(100) DEFAULT '' COMMENT '价格',
  `pimgsrc` varchar(255) DEFAULT '' COMMENT '源图片地址',
  `pimglocal` varchar(255) DEFAULT '' COMMENT '本地图片路径',
  `p_category` varchar(255) NOT NULL DEFAULT '' COMMENT '商品所属分类ID',
  `pcompanyId` varchar(255) DEFAULT '' COMMENT '企业ID',
  PRIMARY KEY (`_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for crawler_produce
-- ----------------------------
DROP TABLE IF EXISTS `crawler_typename`;
CREATE TABLE `crawler_typename` (
  `_id` VARCHAR(200) NOT NULL COMMENT '数据ID',
  `tname` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '规格名',
  `t_category` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '规格所属分类ID',
  PRIMARY KEY (`_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for crawler_produce
-- ----------------------------
DROP TABLE IF EXISTS `crawler_typevalue`;
CREATE TABLE `crawler_typevalue` (
  `_id` VARCHAR(200) NOT NULL COMMENT '数据ID',
  `tvalue` VARCHAR(255) DEFAULT '' COMMENT '规格值',
  `t_category` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '规格值所属分类ID',
  `t_typename` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '规格名称ID',
  PRIMARY KEY (`_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;
