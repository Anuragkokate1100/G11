-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: trackbill_ai
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `trip`
--

DROP TABLE IF EXISTS `trip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trip` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `billing_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `diesel_required` decimal(10,2) NOT NULL,
  `is_billed` bit(1) NOT NULL,
  `mill_name` varchar(150) NOT NULL,
  `sugarcane_owner_name` varchar(150) NOT NULL,
  `tractor_number` varchar(50) NOT NULL,
  `trip_date` date NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `weight` decimal(10,3) NOT NULL,
  `tractor_owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr46pjnv5ioob6uw3a15o07umx` (`tractor_owner_id`),
  CONSTRAINT `FKr46pjnv5ioob6uw3a15o07umx` FOREIGN KEY (`tractor_owner_id`) REFERENCES `tractor_owner` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trip`
--

LOCK TABLES `trip` WRITE;
/*!40000 ALTER TABLE `trip` DISABLE KEYS */;
INSERT INTO `trip` VALUES (9,NULL,'2026-09-23 23:42:10.500765',18.50,_binary '\0','Shri Datta SSK','Dattatray Shinde','MH 11 SG 9090','2026-09-23','2026-09-23 23:42:10.500765',55.400,30),(10,NULL,'2026-09-23 23:42:10.756096',15.00,_binary '\0','Shri Datta SSK','Hanumant Pawar','MH 11 SG 9090','2026-09-23','2026-09-23 23:42:10.756096',44.600,30);
/*!40000 ALTER TABLE `trip` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-11  1:17:44
