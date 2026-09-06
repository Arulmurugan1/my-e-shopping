# API Gateway Design

## Responsibilities

- Route incoming HTTP traffic to the correct microservice
- Validate JWT tokens before forwarding requests
- Enforce role-based access control
- Add correlation IDs and request metadata
- Centralize exception handling and standard error payloads
- Rate-limit traffic when needed
- Log all incoming and routed requests

## Example routing

- /api/v1/auth/** -> auth-service
- /api/v1/customers/** -> customer-service
- /api/v1/products/** -> inventory-service
- /api/v1/cart/** -> cart-service
- /api/v1/orders/** -> order-service
- /api/v1/payments/** -> payment-service
- /api/v1/returns/** -> return-refund-service
- /api/v1/admin/** -> admin-handling within shared services or gateway policy layer

## Response standard

Success response:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {},
  "timestamp": "2026-09-06T00:00:00Z",
  "correlationId": "uuid"
}
```

Error response:
```json
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-09-06T00:00:00Z",
  "correlationId": "uuid"
}
```
