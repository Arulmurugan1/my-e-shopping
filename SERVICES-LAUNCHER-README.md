# Microservices Launcher Guide

This directory contains batch files to easily start, stop, and manage your microservices.

## Files Included

### 1. **start-all-services.bat** - Simple Quick Start
The easiest way to start all microservices at once.

**Usage:**
```
Double-click start-all-services.bat
```

**What it does:**
- Starts all 18 microservices in separate terminal windows
- Each service runs with `mvn spring-boot:run`
- Provides a brief startup summary

**Best for:** Quick testing when all services are already built

---

### 2. **services-launcher.bat** - Advanced Interactive Launcher
A comprehensive launcher with multiple options.

**Usage:**
```
Double-click services-launcher.bat
```

**Menu Options:**
- **1** - Start all microservices
- **2** - Start microservices + Frontend (Angular app)
- **3** - Start specific services (interactive selection)
- **4** - Stop all running services
- **5** - Rebuild all services + Start them
- **6** - Show currently running Java processes
- **0** - Exit launcher

**Best for:** 
- First-time setup (use option 5)
- Development when you need flexibility
- Debugging specific services

---

### 3. **stop-all-services.bat** - Stop All Services
Quickly terminates all running microservices.

**Usage:**
```
Double-click stop-all-services.bat
```

**What it does:**
- Kills all Java processes running your services
- Safe way to stop everything at once

---

## Quick Start Instructions

### First Time Setup (Recommended)
```
1. Open services-launcher.bat
2. Choose option 5 (Rebuild and start all services)
3. Wait for all services to build and start
4. Check individual terminal windows for startup messages
```

### Normal Daily Usage
```
1. Open start-all-services.bat
2. Wait ~30-60 seconds for all services to start
3. Services will be running on their default ports
```

### Stop Everything
```
1. Open stop-all-services.bat
2. Or close all the service terminal windows manually
```

---

## Microservices & Their Default Ports

| Service | Port | Notes |
|---------|------|-------|
| api-gateway | 8080 | Main API Gateway - entry point for all requests |
| auth-service | 8081 | Authentication & Authorization |
| cart-service | 8082 | Shopping Cart Management |
| customer-service | 8083 | Customer Data Management |
| order-service | 8084 | Order Management |
| payment-service | 8085 | Payment Processing |
| inventory-service | 8086 | Inventory Management |
| delivery-service | 8087 | Delivery & Shipment |
| notification-service | 8088 | Email/SMS Notifications |
| audit-service | 8089 | Audit Logging |
| logging-service | 8090 | Centralized Logging |
| invoice-service | 8091 | Invoice Generation |
| order-group-service | 8092 | Order Grouping |
| cartonization-service | 8093 | Carton/Box Management |
| picking-service | 8094 | Pick Operations |
| return-refund-service | 8095 | Returns & Refunds |
| scheduler-service | 8096 | Scheduled Tasks |

*Note: Verify actual ports in each service's application.yml file*

---

## Frontend

To start the Angular frontend separately:
```
1. Open command prompt
2. cd frontend\angular-app
3. npm install (first time only)
4. npm start
```

Or use option 2 in services-launcher.bat to start everything including frontend.

---

## Database & Infrastructure

Before starting services, ensure your database is running:

### PostgreSQL with Docker
```
cd infrastructure/docker
docker-compose up -d
```

### Local PostgreSQL
```
Make sure PostgreSQL is running on your system
Check database configuration in each service's application.yml
```

---

## Troubleshooting

### "Maven not found" or "java not found"
- Install Java 17+ and Maven
- Add them to your system PATH

### Port already in use
- Services are trying to use ports that are already taken
- Use `stop-all-services.bat` to free up ports
- Or modify port numbers in each service's application.yml

### Services not starting
- Check that Maven builds complete: `mvn clean install` in each service directory
- Review individual service terminal windows for error messages
- Ensure PostgreSQL is running

### Memory issues with multiple services
- If your system has limited RAM, start services selectively using option 3 in services-launcher.bat
- Or use `stop-all-services.bat` and rebuild with option 5

---

## Tips & Best Practices

1. **First Run**: Always use option 5 in services-launcher.bat to ensure all dependencies are installed
2. **Development**: Use option 3 to start only the services you're actively working on
3. **Performance**: Stagger service startups; the 2-second delay helps prevent resource contention
4. **Monitoring**: Use option 6 to check which services are currently running
5. **Logs**: Each service window shows its logs in real-time for debugging

---

## Advanced Usage

### Manual Service Start
Open command prompt and run:
```
cd <service-name>
mvn spring-boot:run
```

### Clean Build for Single Service
```
cd <service-name>
mvn clean install -DskipTests
mvn spring-boot:run
```

### Build All Without Running
```
cd services-launcher.bat
Choose option 5 but close windows before they start running
```

---

For more information, see the documentation in the `/docs` directory.
