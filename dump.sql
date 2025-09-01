-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: trackfitdb
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Current Database: `trackfitdb`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `trackfitdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `trackfitdb`;

--
-- Table structure for table `exercises`
--

DROP TABLE IF EXISTS `exercises`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exercises` (
  `exercises_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `target_goal` varchar(50) DEFAULT NULL,
  `muscle_group` varchar(50) DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`exercises_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exercises`
--

LOCK TABLES `exercises` WRITE;
/*!40000 ALTER TABLE `exercises` DISABLE KEYS */;
INSERT INTO `exercises` VALUES (1,'Push-up','Build muscle','Chest','https://www.youtube.com/watch?v=IODxDxX7oi4','Hướng dẫn push-up đúng form để phát triển ngực, vai, tay sau.','2025-08-14 07:00:00'),(2,'Squat','Lose weight','Legs','https://www.youtube.com/watch?v=P-yaD24bUE8','Squat bằng trọng lượng cơ thể, nhấn mạnh tư thế an toàn cho đầu gối và hông.','2025-08-14 07:00:00'),(3,'Plank','Core strength','Core','https://www.youtube.com/watch?v=k6x_Eujln1k','Plank đúng kỹ thuật, cue và progression cho người mới.','2025-08-14 07:00:00'),(5,'Forward Lunge','Functional','Legs','https://www.youtube.com/watch?v=fydLSJlGx-0','Lunge cho người mới, giảm áp lực gối với form chuẩn.','2025-08-14 07:00:00'),(6,'Dumbbell Row (1-Arm)','Build muscle','Back','https://www.youtube.com/watch?v=roCP6wCXPqo','Kéo tạ đơn phát triển lưng giữa, hạn chế vẹo người.','2025-08-14 07:00:00'),(7,'Overhead Press (DB)','Build muscle','Shoulders','https://www.youtube.com/watch?v=qEwKCR5JCog','Đẩy vai với tạ đơn, kiểm soát core và biên độ.','2025-08-14 07:00:00'),(10,'Mountain Climbers','Conditioning','Full Body','https://www.youtube.com/watch?v=nmwgirgXLYM','Cardio cường độ vừa, phối hợp core và vai.','2025-08-14 07:00:00'),(11,'Burpee','Conditioning','Full Body 1','https://www.youtube.com/watch?v=TU8QYVW0gDU','Bài toàn thân cường độ cao, tăng sức bền nhanh.','2025-08-14 07:00:00'),(12,'Push Set','Tăng cơ thân trên ','Ngực- Vai - Tay sau','https://www.youtube.com/watch?v=wfX4Do4O6QY&list=PLSMhVD05LUwIBG0CdymDtqfkjjRYX49u6','','2025-08-26 16:25:02');
/*!40000 ALTER TABLE `exercises` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goal`
--

DROP TABLE IF EXISTS `goal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goal` (
  `goal_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `goal_type` varchar(50) DEFAULT NULL,
  `workout_duration` int DEFAULT NULL,
  `intensity` varchar(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`goal_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `goal_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goal`
--

LOCK TABLES `goal` WRITE;
/*!40000 ALTER TABLE `goal` DISABLE KEYS */;
INSERT INTO `goal` VALUES (1,1,'Build muscle',30,'High','2025-08-14 07:00:00'),(2,3,'Lose weight',45,'Medium','2025-08-14 07:00:00'),(3,6,'Core strength',20,'Low','2025-08-14 07:00:00');
/*!40000 ALTER TABLE `goal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_data`
--

DROP TABLE IF EXISTS `health_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_data` (
  `health_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `height` decimal(5,2) DEFAULT NULL,
  `weight` decimal(5,2) DEFAULT NULL,
  `blood_pressure` varchar(20) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`health_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `health_data_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_data`
--

LOCK TABLES `health_data` WRITE;
/*!40000 ALTER TABLE `health_data` DISABLE KEYS */;
INSERT INTO `health_data` VALUES (1,1,175.50,70.00,'120/80','Dữ liệu mẫu','2025-08-14 14:00:00','2025-08-30 09:18:02'),(2,3,170.00,65.50,'110/70','Sau khi tập gym','2025-08-14 14:00:00','2025-08-30 09:18:02'),(3,6,180.00,80.00,'130/85','Khỏe mạnh','2025-08-14 14:00:00','2025-08-30 09:18:02'),(4,6,171.00,64.00,'115/75','Giảm cân','2025-08-16 13:32:21','2025-08-30 09:18:02');
/*!40000 ALTER TABLE `health_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `message` text NOT NULL,
  `type` enum('reminder','advice','system') DEFAULT 'system',
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,1,'Reminder: Complete your workout today!','reminder',0,'2025-08-14 07:00:00'),(2,3,'Advice: Stay hydrated during workouts.','advice',0,'2025-08-14 07:00:00'),(3,6,'System: Profile updated successfully.','system',1,'2025-08-14 07:00:00'),(4,6,'Bạn có lịch tập mới hôm nay!','reminder',0,'2025-08-16 19:37:55');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan_detail`
--

DROP TABLE IF EXISTS `plan_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan_detail` (
  `detail_id` int NOT NULL AUTO_INCREMENT,
  `plan_id` int NOT NULL,
  `exercises_id` int NOT NULL,
  `day_of_week` int DEFAULT NULL,
  `duration` int DEFAULT NULL,
  PRIMARY KEY (`detail_id`),
  KEY `plan_id` (`plan_id`),
  KEY `plan_detail_ibfk_2` (`exercises_id`),
  CONSTRAINT `plan_detail_ibfk_1` FOREIGN KEY (`plan_id`) REFERENCES `workout_plan` (`plan_id`) ON DELETE CASCADE,
  CONSTRAINT `plan_detail_ibfk_2` FOREIGN KEY (`exercises_id`) REFERENCES `exercises` (`exercises_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan_detail`
--

LOCK TABLES `plan_detail` WRITE;
/*!40000 ALTER TABLE `plan_detail` DISABLE KEYS */;
INSERT INTO `plan_detail` VALUES (1,1,1,1,15),(2,1,2,3,20),(3,2,2,2,30),(4,3,3,4,10),(5,4,1,1,30),(6,4,2,3,20),(7,4,3,7,60),(8,4,3,7,60),(9,4,3,4,60),(10,4,3,6,40),(11,5,1,2,30);
/*!40000 ALTER TABLE `plan_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `statistic`
--

DROP TABLE IF EXISTS `statistic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `statistic` (
  `stat_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `stat_type` varchar(50) DEFAULT NULL,
  `value` decimal(10,2) DEFAULT NULL,
  `generated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`stat_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `statistic_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `statistic`
--

LOCK TABLES `statistic` WRITE;
/*!40000 ALTER TABLE `statistic` DISABLE KEYS */;
INSERT INTO `statistic` VALUES (1,1,'Calories Burned',500.00,'2025-08-14 07:00:00'),(2,3,'Steps Taken',10000.00,'2025-08-14 07:00:00'),(3,6,'Workout Duration',45.00,'2025-08-14 07:00:00');
/*!40000 ALTER TABLE `statistic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `gender` enum('Male','Female') DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `role` enum('ROLE_USER','ROLE_ADMIN') DEFAULT 'ROLE_USER',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin01','$2a$10$YoQp7jxFyKHZSihrvbT/Ou5PH9BNPlRnDhdEOvSsCob/F.L.k1Cv6','admin@example.com','Super','Admin','https://res.cloudinary.com/dywix6n0z/image/upload/v1755500316/x4amfwteod9zm9gfrnxr.jpg','Female','2025-08-01','ROLE_ADMIN','2025-07-19 18:02:56','2025-08-18 07:31:43'),(2,'admin','$2a$10$dHgQ.tqAoG0Dqsbdt11.vuZ4EkaxkksmJb8ciMfbJm3/fWfGCpJqa','admin@mail.com','Nguyễn','Nhật',NULL,'Female','1990-01-01','ROLE_ADMIN','2025-07-19 18:12:06','2025-08-17 20:15:30'),(3,'admin2','$2a$10$YoQp7jxFyKHZSihrvbT/Ou5PH9BNPlRnDhdEOvSsCob/F.L.k1Cv6','admin1@example.com','Admin','User','https://res.cloudinary.com/dywix6n0z/image/upload/v1752950585/ewayfxdkimgov9zpx1eg.jpg','Male','1990-01-01','ROLE_USER','2025-07-19 18:43:03','2025-07-19 18:43:03'),(6,'nhat','$2a$10$dHgQ.tqAoG0Dqsbdt11.vuZ4EkaxkksmJb8ciMfbJm3/fWfGCpJqa','abc@gmail.com','Nhat','Nguyen','https://res.cloudinary.com/dywix6n0z/image/upload/v1756301370/nsyhmux4obbwxkjmt765.jpg','Male','2000-01-14','ROLE_USER','2025-08-13 15:29:08','2025-08-27 13:29:32'),(7,'user1','$2a$10$475xTSzKBHRJ19.NEMpFBe.oQWDpwaFoig21tswyob4uoLptN5xY2','abc1@gmail.com','Nhat','Nguyen','https://res.cloudinary.com/dywix6n0z/image/upload/v1755107539/weex4zfmt4skwv6zm6y7.jpg',NULL,NULL,'ROLE_USER','2025-08-13 17:52:18',NULL),(8,'user123','$2a$10$jbbo9APIF1UmJL8w.VKmx.cjTKzpR20pOFtKg4zEZGDutG00nCIEC','abc123@gmail.com','Nhat','Nguyen','https://res.cloudinary.com/dywix6n0z/image/upload/v1755107701/zdi6milwupjsn1vsnf0u.jpg',NULL,NULL,'ROLE_USER','2025-08-13 17:54:58',NULL),(10,'nhat123','$2a$10$flJCon9nltXAx3n3bRSTx./oz0Gh.f4xr8uDJ72BZMXUpwWCbTk32','vmct@gmail.com','Nhat','Nguyen','https://res.cloudinary.com/dywix6n0z/image/upload/v1755226231/oxd64qxypznfmdtt0vir.jpg','Female','2004-08-19','ROLE_USER','2025-08-15 02:50:28',NULL),(11,'phuc','$2a$10$9ypx3Qym3I0gEGh6kIQ49uaedOczJVsaxy29M.BGbvxvIKl0c9Cz6','nguyenthan213hnhat1908@gmail.com','Nguyễn','Nhật','https://res.cloudinary.com/dywix6n0z/image/upload/v1755503684/hnotqgqfiicrenfvuuuy.jpg','Female','2025-08-06','ROLE_USER','2025-08-18 07:54:41',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_workout_history`
--

DROP TABLE IF EXISTS `user_workout_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_workout_history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `plan_id` int NOT NULL,
  `exercises_id` int NOT NULL,
  `user_id` int NOT NULL,
  `completed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('completed','pending') DEFAULT 'pending',
  `duration` int DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `plan_id` (`plan_id`),
  KEY `user_id` (`user_id`),
  KEY `user_workout_history_ibfk_2` (`exercises_id`),
  CONSTRAINT `user_workout_history_ibfk_1` FOREIGN KEY (`plan_id`) REFERENCES `workout_plan` (`plan_id`) ON DELETE CASCADE,
  CONSTRAINT `user_workout_history_ibfk_2` FOREIGN KEY (`exercises_id`) REFERENCES `exercises` (`exercises_id`) ON DELETE CASCADE,
  CONSTRAINT `user_workout_history_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_workout_history`
--

LOCK TABLES `user_workout_history` WRITE;
/*!40000 ALTER TABLE `user_workout_history` DISABLE KEYS */;
INSERT INTO `user_workout_history` VALUES (1,1,1,1,'2025-08-14 07:00:00','completed',15),(2,2,2,3,'2025-08-14 07:00:00','pending',30),(3,3,3,6,'2025-08-14 07:00:00','completed',10),(10,1,1,1,'2025-08-25 23:00:00','completed',15),(11,1,2,1,'2025-08-26 01:00:00','completed',20),(12,2,3,2,'2025-08-26 03:00:00','completed',30),(13,3,5,3,'2025-08-26 05:00:00','completed',60),(14,2,6,2,'2025-08-26 08:00:00','completed',NULL),(15,3,7,3,'2025-08-26 11:00:00','pending',45),(18,3,10,3,'2025-08-25 14:00:00','completed',40),(19,1,11,1,'2025-08-23 23:00:00','completed',35),(20,2,1,2,'2025-08-24 01:00:00','completed',25),(21,3,2,3,'2025-08-24 10:00:00','completed',30),(22,1,3,1,'2025-08-23 00:00:00','completed',60),(23,2,5,2,'2025-08-23 13:00:00','completed',35),(24,3,6,3,'2025-08-23 15:00:00','completed',45),(25,1,7,1,'2025-08-22 01:00:00','completed',40),(28,1,10,1,'2025-08-21 02:00:00','completed',30),(29,2,11,2,'2025-08-21 08:00:00','completed',30),(30,3,1,3,'2025-08-21 13:00:00','completed',20),(31,1,2,1,'2025-08-20 00:00:00','completed',20),(32,2,3,2,'2025-08-20 11:00:00','completed',25),(33,3,5,3,'2025-08-20 14:00:00','completed',30);
/*!40000 ALTER TABLE `user_workout_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workout_plan`
--

DROP TABLE IF EXISTS `workout_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workout_plan` (
  `plan_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `plan_name` varchar(100) NOT NULL,
  `goal_id` int DEFAULT NULL,
  `is_template` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`plan_id`),
  KEY `user_id` (`user_id`),
  KEY `goal_id` (`goal_id`),
  CONSTRAINT `workout_plan_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `workout_plan_ibfk_2` FOREIGN KEY (`goal_id`) REFERENCES `goal` (`goal_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workout_plan`
--

LOCK TABLES `workout_plan` WRITE;
/*!40000 ALTER TABLE `workout_plan` DISABLE KEYS */;
INSERT INTO `workout_plan` VALUES (1,1,'Muscle Building Plan',1,0,'2025-08-14 07:00:00'),(2,3,'Weight Loss Plan',2,0,'2025-08-14 07:00:00'),(3,6,'Core Strength Plan',3,0,'2025-08-14 07:00:00'),(4,6,'Kế hoạch tuần 7',3,1,'2025-08-16 14:52:46'),(5,6,'Tăng cân',3,0,'2025-08-29 19:36:32');
/*!40000 ALTER TABLE `workout_plan` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-31 15:49:46
