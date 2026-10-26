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
-- Table structure for table `trips`
--

DROP TABLE IF EXISTS `trips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trips` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL,
  `tractor_id` bigint NOT NULL,
  `tractor_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `trip_date` date NOT NULL,
  `sugar_mill_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sugarcane_owner_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_village` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `to_village_mill` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `weight_tons` decimal(10,3) NOT NULL,
  `diesel_liters` decimal(10,2) NOT NULL,
  `rate_per_ton` decimal(10,2) NOT NULL DEFAULT '350.00',
  `diesel_rate` decimal(10,2) NOT NULL DEFAULT '95.00',
  `trip_income` decimal(12,2) NOT NULL,
  `diesel_expense` decimal(12,2) NOT NULL,
  `labour_expense` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total_expense` decimal(12,2) NOT NULL,
  `profit` decimal(12,2) NOT NULL,
  `pending_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `payment_status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_trip_tractor` (`tractor_id`),
  KEY `idx_trip_date` (`trip_date`),
  KEY `idx_trip_owner_date` (`owner_id`,`trip_date`),
  KEY `idx_trip_tractor` (`tractor_number`),
  KEY `idx_trip_mill` (`sugar_mill_name`),
  KEY `idx_trip_cust` (`sugarcane_owner_name`),
  CONSTRAINT `fk_trip_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_trip_tractor` FOREIGN KEY (`tractor_id`) REFERENCES `tractors` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trips`
--

LOCK TABLES `trips` WRITE;
/*!40000 ALTER TABLE `trips` DISABLE KEYS */;
/*!40000 ALTER TABLE `trips` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-11  1:17:46
