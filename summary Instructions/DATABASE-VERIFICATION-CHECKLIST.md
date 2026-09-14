# Database Migration Verification Checklist

Complete this checklist to verify the multi-database migration is working correctly.

---

## Pre-Startup Verification

- [ ] Docker Desktop is running
- [ ] `docker-compose.yml` exists in `infrastructure/docker/`
- [ ] All service `pom.xml` files have been updated (no H2 dependencies in non-test scope)
- [ ] All service `application.properties` or `application.yml` files have correct database URLs
- [ ] Port 8080-8097 are not in use by other applications

---

## Docker Compose Startup

### Start Databases
```bash
cd infrastructure/docker
docker-compose up -d
```

Verify all containers are running:
```bash
docker-compose ps
```

**Expected Output:**
```
NAME                IMAGE               STATUS
postgres            postgres:16-alpine  Up (healthy)
mysql               mysql:8.0-alpine    Up (healthy)
mongodb             mongo:7-alpine      Up (healthy)
oracle              oracle-xe:21        Up (starting... → healthy after 2-3 min)
redis               redis:7-alpine      Up (healthy)
kafka               kafka:7.6.1         Up (healthy)
zookeeper           zookeeper:7.6.1     Up (healthy)
```

- [ ] All database containers are running
- [ ] All healthchecks are passing (or pending for Oracle)
- [ ] No container restart loops

---

## Service Startup Verification

### Option 1: Batch File (Windows)
```batch
start-all-services.bat
```

**Expected:** 18 terminal windows open, one per service

### Option 2: Manual Startup (by database type)

#### PostgreSQL Services
```bash
# Terminal 1
cd order-service && mvn spring-boot:run

# Terminal 2
cd payment-service && mvn spring-boot:run

# Terminal 3
cd customer-service && mvn spring-boot:run

# Terminal 4
cd cart-service && mvn spring-boot:run

# Terminal 5
cd picking-service && mvn spring-boot:run
```

- [ ] Services start without H2 console warnings
- [ ] No "Connection refused" errors
- [ ] Services show "Started [ServiceName]" messages

#### MySQL Services
```bash
cd delivery-service && mvn spring-boot:run
cd shipment-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd return-refund-service && mvn spring-boot:run
```

- [ ] MySQL services connect without errors
- [ ] No JDBC URL parsing errors

#### MongoDB Service
```bash
cd inventory-service && mvn spring-boot:run
```

- [ ] MongoDB connection succeeds
- [ ] Collections created automatically on startup

#### Oracle Services
```bash
cd auth-service && mvn spring-boot:run
cd audit-service && mvn spring-boot:run
cd logging-service && mvn spring-boot:run
```

- [ ] Oracle services connect (may take 30+ seconds on first run)
- [ ] No "Listener refused connection" errors

#### Stateless Services
```bash
cd api-gateway && mvn spring-boot:run
cd cartonization-service && mvn spring-boot:run
cd order-group-service && mvn spring-boot:run
cd invoice-service && mvn spring-boot:run
cd scheduler-service && mvn spring-boot:run
```

- [ ] All stateless services start successfully
- [ ] No database warnings

---

## Connectivity Tests

### Test PostgreSQL Services

**Order Service:**
```bash
curl -X GET http://localhost:8084/actuator/health
```
Expected: `{"status":"UP","components":{"db":{"status":"UP","details":{"database":"PostgreSQL"...}}}}`

**Payment Service:**
```bash
curl -X GET http://localhost:8085/actuator/health
```

**Customer Service:**
```bash
curl -X GET http://localhost:8091/actuator/health
```

**Cart Service:**
```bash
curl -X GET http://localhost:8083/actuator/health
```

**Picking Service:**
```bash
curl -X GET http://localhost:8086/actuator/health
```

- [ ] All PostgreSQL services report UP status
- [ ] Database component shows PostgreSQL dialect

### Test MySQL Services

**Delivery Service:**
```bash
curl -X GET http://localhost:8088/actuator/health
```

**Shipment Service:**
```bash
curl -X GET http://localhost:8087/actuator/health
```

**Notification Service:**
```bash
curl -X GET http://localhost:8094/actuator/health
```

**Return-Refund Service:**
```bash
curl -X GET http://localhost:8090/actuator/health
```

- [ ] All MySQL services report UP status
- [ ] Database component shows MySQL 8.0 dialect

### Test MongoDB Service

**Inventory Service:**
```bash
curl -X GET http://localhost:8082/actuator/health
```

- [ ] Inventory service reports UP status
- [ ] Database component shows MongoDB dialect

### Test Oracle Services

**Auth Service:**
```bash
curl -X GET http://localhost:8081/actuator/health
```

**Audit Service:**
```bash
curl -X GET http://localhost:8096/actuator/health
```

**Logging Service:**
```bash
curl -X GET http://localhost:8095/actuator/health
```

- [ ] All Oracle services report UP status
- [ ] Database component shows Oracle dialect

### Test Stateless Services

**API Gateway:**
```bash
curl -X GET http://localhost:8080/actuator/health
```

**Other Services:**
```bash
curl -X GET http://localhost:8093/actuator/health  # cartonization
curl -X GET http://localhost:8092/actuator/health  # order-group
curl -X GET http://localhost:8089/actuator/health  # invoice
curl -X GET http://localhost:8097/actuator/health  # scheduler
```

- [ ] All stateless services report UP status

---

## Database Content Verification

### PostgreSQL: Check Order Database
```bash
docker-compose exec postgres psql -U myeshopping -d order_db -c "\dt"
```

Expected: Tables like `orders`, `order_items`, etc.

- [ ] Tables created successfully
- [ ] No H2 system tables visible

### PostgreSQL: Verify All Databases Exist
```bash
docker-compose exec postgres psql -U myeshopping -l
```

Expected databases:
- order_db
- payment_db
- customer_db
- cart_db
- picking_db

- [ ] All 5 PostgreSQL databases exist

### MySQL: Check Delivery Database
```bash
docker-compose exec mysql mysql -u myeshopping -p myeshopping -e "USE delivery_db; SHOW TABLES;"
```

Expected: Tables related to delivery operations

- [ ] Delivery tables created
- [ ] No H2 system tables

### MySQL: Verify All Databases
```bash
docker-compose exec mysql mysql -u myeshopping -p myeshopping -e "SHOW DATABASES;"
```

Expected databases:
- delivery_db
- shipment_db
- notification_db
- return_refund_db

- [ ] All 4 MySQL databases exist

### MongoDB: Check Inventory Database
```bash
docker-compose exec mongodb mongosh --authenticationDatabase admin -u myeshopping -p myeshopping --eval "db.adminCommand('listCollections')"
```

- [ ] Collections exist in MongoDB
- [ ] No H2 artifacts

### Oracle: Check Auth Database
```bash
docker-compose exec oracle sqlplus system/myeshopping123@XE <<EOF
SELECT table_name FROM user_tables;
EXIT;
EOF
```

- [ ] Tables created in Oracle
- [ ] Auth service tables visible

---

## Load Testing (Optional)

### Simple POST Request to Order Service
```bash
curl -X POST http://localhost:8084/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "totalAmount": 99.99}'
```

- [ ] Request accepted (verify status code 200-201)
- [ ] Order persisted to PostgreSQL
- [ ] Verify with: 
```bash
docker-compose exec postgres psql -U myeshopping -d order_db -c "SELECT * FROM orders;"
```

### Query Inventory from MongoDB
```bash
curl -X GET http://localhost:8082/api/products
```

- [ ] Returns product data from MongoDB
- [ ] Response contains MongoDB document IDs (_id)

---

## Performance Checks

### CPU Usage
```bash
docker stats
```

- [ ] No service consistently above 50% CPU
- [ ] No memory leaks (stable memory over time)

### Database Connection Pool Status
Check service logs for:
- "HikariPool-1 - Starting"
- "Connection pool initialized"

- [ ] Connection pools initialized successfully

### Response Time
```bash
time curl -X GET http://localhost:8084/actuator/health
```

- [ ] Response time < 100ms (healthy service)
- [ ] No timeout errors

---

## Cleanup Tests

### Stop All Services
Option 1 (Batch file):
```batch
stop-all-services.bat
```

Option 2 (Manual):
Kill all Java processes in terminal

- [ ] All service terminal windows close
- [ ] No orphaned processes

### Stop Databases
```bash
cd infrastructure/docker
docker-compose down
```

- [ ] All containers stop
- [ ] No error messages

### Restart Verification
Start everything again and verify:
- [ ] All databases reconnect successfully
- [ ] No data loss (verify previous test data is present)
- [ ] Services start in same order without issues

---

## Documentation Verification

- [ ] `DATABASE-CONFIGURATION.md` exists and is readable
- [ ] `DATABASE-MIGRATION-SUMMARY.md` exists
- [ ] Service port mapping table is accurate
- [ ] Connection strings documented

---

## Issue Resolution

### If ANY tests fail:

**Step 1: Check Error Messages**
```bash
# View service logs
docker-compose logs <service-name> --tail=50

# View specific service startup output
# (check the terminal window where service is running)
```

**Step 2: Verify Database Connectivity**
```bash
# Test PostgreSQL
docker-compose exec postgres psql -U myeshopping -c "SELECT version();"

# Test MySQL
docker-compose exec mysql mysql -u myeshopping -p myeshopping -e "SELECT VERSION();"

# Test MongoDB
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"

# Test Oracle
docker-compose exec oracle sqlplus system/myeshopping123@XE <<EOF
SELECT * FROM v$version;
EXIT;
EOF
```

**Step 3: Check Service Configuration**
- Verify `application.properties` has correct JDBC URL
- Verify database credentials match docker-compose.yml
- Verify port numbers are unique and not in use

**Step 4: Review DATABASE-CONFIGURATION.md**
- Reference correct connection patterns
- Check service-to-database mapping
- Verify all required environment variables are set

**Step 5: Escalate**
If issues persist:
1. Check migration summary for any missed files
2. Verify all pom.xml files have correct dependencies
3. Review git history to ensure all changes were committed
4. Consider manual H2 cleanup if traces remain

---

## Sign-Off Checklist

After completing all verifications:

- [ ] All 18 services start successfully
- [ ] All 4 database types are functional (PostgreSQL, MySQL, MongoDB, Oracle)
- [ ] All health checks pass (18 services + 7 infrastructure containers = 25 total)
- [ ] Data persists across restarts
- [ ] No H2 dependencies remain in production code
- [ ] Performance metrics are acceptable
- [ ] Documentation is complete and accurate

**Migration Status:** ✅ **COMPLETE AND VERIFIED**

---

## Next Actions

After successful verification:

1. **Backup Current State**
   ```bash
   git commit -m "Multi-database migration completed and verified"
   git tag v1.0-multi-db-migration
   ```

2. **Deploy to Development Environment**
   - Update deployment scripts to use docker-compose
   - Configure environment-specific credentials

3. **Load Testing**
   - Run integration test suite
   - Perform stress testing

4. **Production Readiness**
   - Configure managed database services (AWS RDS, Azure Database, etc.)
   - Set up automated backups
   - Enable encryption and audit logging
   - Configure monitoring and alerting

5. **Team Training**
   - Share `DATABASE-CONFIGURATION.md` with team
   - Document service restart procedures
   - Create troubleshooting guide

---

**Verification Date:** ___________
**Verified By:** ___________
**Status:** ✅ Ready for Deployment
