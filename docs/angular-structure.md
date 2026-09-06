# Angular Frontend Structure

## Recommended layout

```text
src/app/
  core/
    auth/
    interceptors/
    guards/
    services/
  shared/
    components/
    models/
    pipes/
    directives/
  features/
    customer/
    admin/
    orders/
    inventory/
    cart/
    payment/
    returns/
```

## Frontend requirements

- Route guards for protected access
- HTTP interceptor for JWT and correlation ID propagation
- Lazy-loaded feature modules
- Reactive forms for login, registration, and profile management
- Loading indicators and centralized error handling
- Responsive design for customer and admin screens
- Notifications for success and failure states

## Customer screens

- Login
- Registration
- Dashboard
- Browse products
- Cart
- Order confirmation
- Delivery tracking
- Profile and address management

## Admin screens

- Login
- Orders
- Customers
- Inventory
- Grouping status
- Picking status
- Shipment status
- Delivery status
- Payment information
- Audit information
- Active users
