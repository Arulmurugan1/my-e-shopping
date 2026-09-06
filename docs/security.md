# Security and Authentication Design

## Authentication

- JWT-based access and refresh tokens
- BCrypt password hashing
- Customer login through email + password
- Registration supports email, password, date of birth, and gender
- Google sign-in is optional and not required for initial delivery

## Authorization

Roles:
- ADMIN
- CUSTOMER

## JWT claims

- userId
- email
- role
- issuedAt
- expiration

## Security controls

- Authorization checks on all protected endpoints
- HTTPS-ready configuration
- CORS policies
- Input validation and Bean Validation
- Parameterized queries and repository-level controls
- No credentials, tokens, or payment data logging
- Inactive user blocking at auth layer

## Gateway responsibilities

- Validate JWTs
- Enforce role checks and route protection
- Add correlation IDs and request metadata
- Centralize auth and error handling
