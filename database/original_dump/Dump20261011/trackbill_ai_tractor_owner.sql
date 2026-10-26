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
-- Table structure for table `tractor_owner`
--

DROP TABLE IF EXISTS `tractor_owner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tractor_owner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `district` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `pincode` varchar(10) NOT NULL,
  `taluka` varchar(100) NOT NULL,
  `tractor_number` varchar(50) NOT NULL,
  `trolly_count` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `village` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2jaaqyp56xkt8agnp3lpobyby` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tractor_owner`
--

LOCK TABLES `tractor_owner` WRITE;
/*!40000 ALTER TABLE `tractor_owner` DISABLE KEYS */;
INSERT INTO `tractor_owner` VALUES (29,'2026-09-23 23:40:59.400090','Satara','sunil.7c2de1ba@trackbill.in','Sunil Gaikwad','$2a$10$lKL04M1F57cLevSMRUg1sOG.D/IzXKdbllugNpiQD7SgUvMFOIASC','415523','Phaltan','MH 11 SG 9090',2,'2026-09-23 23:40:59.400090','Phaltan'),(30,'2026-09-23 23:42:08.862173','Satara','sunil.6ab3ff4d@trackbill.in','Sunil Gaikwad','$2a$10$jhyBI.FYA7mYJKrs7oRbDuEen3K3L7KayLKxE8pw/jF0IDKHRdBJq','415523','Phaltan','MH 11 SG 9090',2,'2026-09-23 23:42:08.862173','Phaltan'),(59,'2026-09-24 12:29:01.263043','erwetre','sohampatil7920@gmail.com','soham','$2a$10$shLEcTTSD9fjVumG0Ri0BORYX4HpSZcCuldFvA0D6OmCt3OHxx/7u','412302','drtryet','MH46P8214',1,'2026-09-24 12:36:49.143821','shigao'),(60,'2026-09-27 10:39:25.272454','sangli','sohampatil@gmail.com','Soham Patil','$2a$10$.eB0OxvBKglnjw2l3DCayeJ122Fw5CTNPzbkBT9WJCR0H1hMpRxrS','416302','walwa','MH46P8214',2,'2026-09-27 10:39:25.272454','shigaon'),(61,'2026-09-29 20:18:30.065808','erwetre','sohampatil1234@gmail.com','soham','$2a$10$bxSLpq8T9WqfxaOdP2fEX.eexOxkoqmvZx1pEabg.dC5pd9n3qoZC','123456','drtryet','MH46P8214',1,'2026-09-29 20:18:30.065808','wrfg');
/*!40000 ALTER TABLE `tractor_owner` ENABLE KEYS */;
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
