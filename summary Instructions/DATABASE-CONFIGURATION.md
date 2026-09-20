# Multi-Database Configuration Guide

This document outlines the database configuration for the My E-Shopping microservices platform.

## Database Distribution

### 🍃 MongoDB - Product Catalog Data
**Service:** inventory-service (Port 8082)
- **Database:** MongoDB
- **Connection:** `mongodb://myeshopping:myeshopping@localhost:27017/inventory_db?authSource=admin`
- **Use Case:** Product information, stock levels, flexible schema for product attributes
- **Started:** Automatic with `docker-compose up`

### 🐘 PostgreSQL - Transactional Data
**Services:**
1. **order-service** (Port 8084)
   - Database: `order_db`
   - Tables: Orders, order items, order status tracking
   
2. **payment-service** (Port 8085)
   - Database: `payment_db`
   - Tables: Payment records, transactions, invoices
   
3. **customer-service** (Port 8091)
   - Database: `customer_db`
   - Tables: Customer profiles, contact info, preferences
   
4. **cart-service** (Port 8082)
   - Database: `cart_db`
   - Tables: Shopping carts, cart items

**Connection Details:**
- **Host:** localhost:5432
- **Username:** myeshopping
- **Password:** myeshopping
- **Connection String:** `jdbc:postgresql://localhost:5432/<db_name>`

### 🐬 MySQL - Operational Data
**Services:**
1. **delivery-service** (Port 8088)
   - Database: `delivery_db`
   - Tables: Delivery routes, tracking info
   
2. **shipment-service** (Port 8087)
   - Database: `shipment_db`
   - Tables: Shipments, packages, tracking
   
3. **notification-service** (Port 8094)
   - Database: `notification_db`
   - Tables: Notifications, templates, delivery logs
   
4. **return-refund-service** (Port 8090)
   - Database: `return_refund_db`
   - Tables: Returns, refunds, RMA (Return Merchandise Authorization)

**Connection Details:**
- **Host:** localhost:3306
- **Username:** myeshopping
- **Password:** myeshopping
- **Connection String:** `jdbc:mysql://localhost:3306/<db_name>`

### 🦁 Oracle - Enterprise Critical Data
**Services:**
1. **auth-service** (Port 8081)
   - Database: XE (Oracle Express Edition)
   - Tables: Users, roles, permissions, security policies
   
2. **audit-service** (Port 8096)
   - Database: XE
   - Tables: Audit logs, compliance records
   
3. **logging-service** (Port 8095)
   - Database: XE
   - Tables: Application logs, debug traces

**Connection Details:**
- **Host:** localhost
- **Port:** 1521
- **SID:** XE
- **Username:** system
- **Password:** myeshopping123
- **Connection String:** `jdbc:oracle:thin:@localhost:1521:XE`

### ⚪ Other Services (No Database)
- **api-gateway** (Port 8080) - Route dispatcher, no DB needed
- **cartonization-service** (Port 8093) - Stateless operations
- **picking-service** (Port 8086) - Stateless operations
- **order-group-service** (Port 8092) - Stateless operations
- **scheduler-service** (Port 8089) - State maintained in-memory or cache

---

## Docker Compose Setup

### Start All Databases
```bash
cd infrastructure/docker
docker-compose up -d
```

### Stop All Databases
```bash
docker-compose down
```

### View Database Logs
```bash
docker-compose logs -f <service-name>
# Example: docker-compose logs -f postgres
```

### Database Services in docker-compose.yml
- **postgres** - PostgreSQL 16-alpine
- **mysql** - MySQL 8.0-alpine
- **mongodb** - MongoDB 7-alpine
- **oracle** - Oracle XE 21-alpine
- **redis** - Redis 7-alpine (caching)
- **kafka** - Kafka 7.6.1 (message broker)
- **zookeeper** - Zookeeper 7.6.1 (Kafka coordination)

---

## Database Initialization

### PostgreSQL Databases
Automatic initialization happens on first startup. To manually create databases:
```sql
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE customer_db;
CREATE DATABASE cart_db;
```

### MySQL Databases
Automatic initialization. If needed, create manually:
```sql
CREATE DATABASE delivery_db;
CREATE DATABASE shipment_db;
CREATE DATABASE notification_db;
CREATE DATABASE return_refund_db;
```

### Oracle Database
- **Default Database:** XE
- **System User:** system / myeshopping123
- **Auto-create:** Tablespaces on first service startup

### MongoDB Databases
- **Admin User:** myeshopping / myeshopping
- **Auto-initialize:** Collections created on demand

---

## Service-to-Database Mapping Reference

| Service | Port | Database | Type | Connection |
|---------|------|----------|------|-----------|
| api-gateway | 8080 | - | - | - |
| auth-service | 8081 | XE | Oracle | `jdbc:oracle:thin:@localhost:1521:XE` |
| inventory-service | 8082 | inventory_db | MongoDB | `mongodb://localhost:27017/inventory_db` |
| cart-service | 8083 | cart_db | PostgreSQL | `jdbc:postgresql://localhost:5432/cart_db` |
| order-service | 8084 | order_db | PostgreSQL | `jdbc:postgresql://localhost:5432/order_db` |
| payment-service | 8085 | payment_db | PostgreSQL | `jdbc:postgresql://localhost:5432/payment_db` |
| picking-service | 8086 | picking_db | PostgreSQL | `jdbc:postgresql://localhost:5432/picking_db` |
| shipment-service | 8087 | shipment_db | MySQL | `jdbc:mysql://localhost:3306/shipment_db` |
| delivery-service | 8088 | delivery_db | MySQL | `jdbc:mysql://localhost:3306/delivery_db` |
| invoice-service | 8089 | - | - | - |
| return-refund-service | 8090 | return_refund_db | MySQL | `jdbc:mysql://localhost:3306/return_refund_db` |
| customer-service | 8091 | customer_db | PostgreSQL | `jdbc:postgresql://localhost:5432/customer_db` |
| order-group-service | 8092 | - | - | - |
| cartonization-service | 8093 | - | - | - |
| notification-service | 8094 | notification_db | MySQL | `jdbc:mysql://localhost:3306/notification_db` |
| logging-service | 8095 | XE | Oracle | `jdbc:oracle:thin:@localhost:1521:XE` |
| audit-service | 8096 | XE | Oracle | `jdbc:oracle:thin:@localhost:1521:XE` |
| scheduler-service | 8097 | - | - | - |

---

## Troubleshooting

### PostgreSQL Connection Failed
1. Check if container is running: `docker ps | grep postgres`
2. Verify port 5432 is not in use: `netstat -an | grep 5432`
3. Restart: `docker-compose restart postgres`

### MySQL Connection Failed
1. Check if container is running: `docker ps | grep mysql`
2. Verify port 3306 is not in use
3. Restart: `docker-compose restart mysql`

### MongoDB Connection Failed
1. Check if container is running: `docker ps | grep mongo`
2. Verify port 27017 is not in use
3. Restart: `docker-compose restart mongodb`

### Oracle Connection Failed
1. Check if container is running: `docker ps | grep oracle`
2. Oracle startup takes longer (~2-3 minutes)
3. View logs: `docker-compose logs oracle`
4. Restart: `docker-compose restart oracle`

### Service Can't Connect to Database
1. Ensure docker-compose is running: `docker-compose ps`
2. Check service logs: `mvn spring-boot:run` (watch console output)
3. Verify credentials in `application.properties` or `application.yml`
4. Check if database is ready: Database healthchecks take 10-30 seconds

### Port Already in Use
If a port is already in use, modify the port in docker-compose.yml:
```yaml
postgres:
  ports: ["5433:5432"]  # Change external port to 5433
```

---

## Performance Tips

1. **MongoDB:** Disable auto-index for production: `spring.data.mongodb.auto-index-creation=false`
2. **PostgreSQL:** Enable connection pooling (HikariCP configured by default)
3. **MySQL:** Use batching: `spring.jpa.properties.hibernate.jdbc.batch_size=20`
4. **Oracle:** Use appropriate tablespace and partition strategy
5. **Redis:** Cache frequently accessed data (configured for all services)

---

## Backup & Restore

### PostgreSQL Backup
```bash
docker-compose exec postgres pg_dump -U myeshopping order_db > order_db_backup.sql
```

### MySQL Backup
```bash
docker-compose exec mysql mysqldump -u myeshopping -p myeshopping notification_db > notification_db_backup.sql
```

### MongoDB Backup
```bash
docker-compose exec mongodb mongodump --uri="mongodb://myeshopping:myeshopping@localhost:27017" --out=/backup
```

---

## Security Notes

⚠️ **Development Credentials Only!**
- Default passwords are for local development only
- Change all credentials for production deployment
- Use environment variables or vault for secrets management
- Enable SSL/TLS for all database connections in production
- Implement database-level authentication and authorization

---

## Additional Configuration Files

- Service configs: Each service has `application.properties` or `application.yml`
- Docker compose: `infrastructure/docker/docker-compose.yml`
- Database init script: `infrastructure/postgres/init.sql`
