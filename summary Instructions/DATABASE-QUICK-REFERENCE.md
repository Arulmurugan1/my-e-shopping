# Database Architecture Quick Reference

## 🚀 Quick Start

```bash
# 1. Start all databases
cd infrastructure/docker
docker-compose up -d

# 2. Start all services
./start-all-services.bat        # Windows
./start-all-services.sh          # Linux/Mac

# 3. Verify health
curl http://localhost:8080/actuator/health
```

---

## 📊 Database Type by Service

| Service | Database | Port | Credentials |
|---------|----------|------|-------------|
| **auth-service** | Oracle XE | 1521 | system/myeshopping123 |
| **audit-service** | Oracle XE | 1521 | system/myeshopping123 |
| **logging-service** | Oracle XE | 1521 | system/myeshopping123 |
| **order-service** | PostgreSQL | 5432 | myeshopping/myeshopping |
| **payment-service** | PostgreSQL | 5432 | myeshopping/myeshopping |
| **customer-service** | PostgreSQL | 5432 | myeshopping/myeshopping |
| **cart-service** | PostgreSQL | 5432 | myeshopping/myeshopping |
| **picking-service** | PostgreSQL | 5432 | myeshopping/myeshopping |
| **delivery-service** | MySQL | 3306 | myeshopping/myeshopping |
| **shipment-service** | MySQL | 3306 | myeshopping/myeshopping |
| **notification-service** | MySQL | 3306 | myeshopping/myeshopping |
| **return-refund-service** | MySQL | 3306 | myeshopping/myeshopping |
| **inventory-service** | MongoDB | 27017 | myeshopping/myeshopping |
| **api-gateway** | None | 8080 | N/A |
| **cartonization-service** | None | 8093 | N/A |
| **order-group-service** | None | 8092 | N/A |
| **invoice-service** | None | 8089 | N/A |
| **scheduler-service** | None | 8097 | N/A |

---

## 🗄️ Database Connection Strings

### PostgreSQL
```
jdbc:postgresql://localhost:5432/{db_name}
Username: myeshopping
Password: myeshopping
```

**Databases:**
- order_db
- payment_db
- customer_db
- cart_db
- picking_db

### MySQL
```
jdbc:mysql://localhost:3306/{db_name}
Username: myeshopping
Password: myeshopping
```

**Databases:**
- delivery_db
- shipment_db
- notification_db
- return_refund_db

### MongoDB
```
mongodb://myeshopping:myeshopping@localhost:27017/{db_name}?authSource=admin
```

**Databases:**
- inventory_db

### Oracle XE
```
jdbc:oracle:thin:@localhost:1521:XE
Username: system
Password: myeshopping123
```

**Database:**
- XE (Express Edition - default)

---

## 🔌 Service Ports

```
8080 → API Gateway
8081 → Auth Service
8082 → Inventory Service (MongoDB)
8083 → Cart Service
8084 → Order Service
8085 → Payment Service
8086 → Picking Service
8087 → Shipment Service
8088 → Delivery Service
8089 → Invoice Service
8090 → Return-Refund Service
8091 → Customer Service
8092 → Order-Group Service
8093 → Cartonization Service
8094 → Notification Service
8095 → Logging Service
8096 → Audit Service
8097 → Scheduler Service
```

---

## 💾 Common Database Operations

### PostgreSQL Commands

**Connect to database:**
```bash
docker-compose exec postgres psql -U myeshopping -d order_db
```

**List tables:**
```sql
\dt
```

**View table structure:**
```sql
\d table_name
```

**Query data:**
```sql
SELECT * FROM orders LIMIT 10;
```

### MySQL Commands

**Connect to database:**
```bash
docker-compose exec mysql mysql -u myeshopping -p myeshopping -D delivery_db
```

**List tables:**
```sql
SHOW TABLES;
```

**View table structure:**
```sql
DESCRIBE table_name;
```

**Query data:**
```sql
SELECT * FROM deliveries LIMIT 10;
```

### MongoDB Commands

**Connect to MongoDB:**
```bash
docker-compose exec mongodb mongosh --authenticationDatabase admin -u myeshopping -p myeshopping
```

**List collections:**
```javascript
db.getCollectionNames()
```

**Query documents:**
```javascript
db.products.find().limit(10)
```

**Count documents:**
```javascript
db.products.countDocuments()
```

### Oracle Commands

**Connect to Oracle:**
```bash
docker-compose exec oracle sqlplus system/myeshopping123@XE
```

**List tables:**
```sql
SELECT table_name FROM user_tables;
```

**Query data:**
```sql
SELECT * FROM auth_users WHERE ROWNUM <= 10;
```

---

## 🐛 Troubleshooting Quick Fixes

### Service won't start - "Connection refused"
```bash
# Check if database container is running
docker-compose ps

# Restart database
docker-compose restart postgres  # or mysql, mongodb, oracle

# Wait for healthcheck
docker-compose logs postgres --tail=20
```

### "Database not found" error
```bash
# PostgreSQL - create database
docker-compose exec postgres createdb -U myeshopping order_db

# MySQL - create database
docker-compose exec mysql mysql -u root -p myeshopping -e "CREATE DATABASE order_db;"

# MongoDB - creates automatically on first use
```

### Port already in use
```bash
# Find process using port
netstat -ano | findstr :5432  # Windows
lsof -i :5432                 # Mac/Linux

# Kill process (Windows)
taskkill /PID <process_id> /F

# Change port in docker-compose.yml
# postgres:
#   ports: ["5433:5432"]  # Use 5433 instead
```

### Oracle takes too long to start
```bash
# Oracle needs 2-3 minutes on first startup
docker-compose logs oracle --follow

# Wait for: "Startup warm beginning"
# Then: "Database Ready to use"
```

### Can't connect after restart
```bash
# Stop all containers and remove volumes (WARNING: loses data!)
docker-compose down -v

# Restart
docker-compose up -d

# Services will auto-reconnect
```

---

## 📝 Configuration Files

**Docker Compose:**
```
infrastructure/docker/docker-compose.yml
```

**Service Configurations:**
```
{service}/src/main/resources/application.properties
{service}/src/main/resources/application.yml
```

**Documentation:**
```
DATABASE-CONFIGURATION.md           # Comprehensive guide
DATABASE-MIGRATION-SUMMARY.md       # What changed and why
DATABASE-VERIFICATION-CHECKLIST.md  # Testing procedures
SERVICES-LAUNCHER-README.md         # Batch file usage
```

---

## ✅ Health Check URLs

```bash
# All services
curl http://localhost:{port}/actuator/health

# Examples:
curl http://localhost:8080/actuator/health    # API Gateway
curl http://localhost:8084/actuator/health    # Order Service
curl http://localhost:8082/actuator/health    # Inventory Service
curl http://localhost:8081/actuator/health    # Auth Service
```

---

## 🔐 Security Notes

⚠️ **Development Credentials Only!**

For **Production**, use:
- AWS Secrets Manager / Azure Key Vault
- Environment variables
- Kubernetes secrets
- HashiCorp Vault

---

## 📈 Performance Tips

1. **PostgreSQL:** Enable connection pooling (automatic with HikariCP)
2. **MySQL:** Use batch inserts for high-volume operations
3. **MongoDB:** Disable auto-index creation in production
4. **Oracle:** Use appropriate tablespaces and partitioning
5. **All:** Enable query result caching with Redis

---

## 🚨 Emergency Procedures

### Restart All Services
```bash
stop-all-services.bat          # Windows
pkill java                      # Mac/Linux
```

### Backup All Databases
```bash
# PostgreSQL
docker-compose exec postgres pg_dump -U myeshopping > backup.sql

# MySQL
docker-compose exec mysql mysqldump -u root -p > backup.sql

# MongoDB
docker-compose exec mongodb mongodump --out=/backup
```

### Reset Everything
```bash
# Stop services
stop-all-services.bat

# Stop databases
cd infrastructure/docker
docker-compose down

# Remove all data
docker-compose down -v

# Restart
docker-compose up -d
```

---

## 📚 Resources

- **Spring Data JPA:** https://spring.io/projects/spring-data-jpa
- **Spring Data MongoDB:** https://spring.io/projects/spring-data-mongodb
- **PostgreSQL JDBC:** https://jdbc.postgresql.org/documentation/
- **MySQL JDBC:** https://dev.mysql.com/doc/connector-j/
- **Oracle JDBC:** https://docs.oracle.com/en/database/oracle/oracle-database/

---

## 👥 Getting Help

1. **Check logs:** `docker-compose logs <service>`
2. **Verify credentials:** Match docker-compose.yml and application.properties
3. **Read docs:** DATABASE-CONFIGURATION.md has detailed troubleshooting
4. **Check health:** `curl http://localhost:{port}/actuator/health`

---

**Last Updated:** 2024
**Status:** ✅ Multi-Database Architecture Active
