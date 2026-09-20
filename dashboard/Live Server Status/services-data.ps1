<#
Shared service/database definitions used by both generate-dashboard.ps1 (static snapshot)
and live-server.ps1 (real-time dashboard). Keeping one source of truth avoids the two
tools drifting out of sync.
#>

$Services = @(
    @{ Name = 'api-gateway';           Port = 8080; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-' },
    @{ Name = 'auth-service';          Port = 8081; DbType = 'Oracle';     DbHost = 'localhost'; DbPort = 1521;  DbName = 'XE';               DbUser = 'system';      DbPass = 'myeshopping123' },
    @{ Name = 'inventory-service';     Port = 8082; DbType = 'PostgreSQL'; DbHost = 'localhost'; DbPort = 5432;  DbName = 'inventory_db';     DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'cart-service';          Port = 8083; DbType = 'PostgreSQL'; DbHost = 'localhost'; DbPort = 5432;  DbName = 'cart_db';          DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'order-service';         Port = 8084; DbType = 'PostgreSQL'; DbHost = 'localhost'; DbPort = 5432;  DbName = 'order_db';         DbUser = 'myeshopping'; DbPass = 'myeshopping'; Profile = 'postgres' },
    @{ Name = 'payment-service';       Port = 8085; DbType = 'PostgreSQL'; DbHost = 'localhost'; DbPort = 5432;  DbName = 'payment_db';       DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'picking-service';       Port = 8086; DbType = 'PostgreSQL'; DbHost = 'localhost'; DbPort = 5432;  DbName = 'picking_db';       DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'shipment-service';      Port = 8087; DbType = 'MySQL';      DbHost = 'localhost'; DbPort = 3306;  DbName = 'shipment_db';      DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'delivery-service';      Port = 8088; DbType = 'MySQL';      DbHost = 'localhost'; DbPort = 3306;  DbName = 'delivery_db';      DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'invoice-service';       Port = 8089; DbType = 'PostgreSQL'; DbHost = 'localhost'; DbPort = 5432;  DbName = 'invoice_db';       DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'return-refund-service'; Port = 8090; DbType = 'MySQL';      DbHost = 'localhost'; DbPort = 3306;  DbName = 'return_refund_db'; DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'customer-service';      Port = 8091; DbType = 'PostgreSQL'; DbHost = 'localhost'; DbPort = 5432;  DbName = 'customer_db';      DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'cartonization-service'; Port = 8092; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-' },
    @{ Name = 'order-group-service';   Port = 8093; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-' },
    @{ Name = 'notification-service';  Port = 8094; DbType = 'MySQL';      DbHost = 'localhost'; DbPort = 3306;  DbName = 'notification_db';  DbUser = 'myeshopping'; DbPass = 'myeshopping' },
    @{ Name = 'logging-service';       Port = 8095; DbType = 'Oracle';     DbHost = 'localhost'; DbPort = 1521;  DbName = 'XE';               DbUser = 'system';      DbPass = 'myeshopping123' },
    @{ Name = 'audit-service';         Port = 8096; DbType = 'Oracle';     DbHost = 'localhost'; DbPort = 1521;  DbName = 'XE';               DbUser = 'system';      DbPass = 'myeshopping123' },
    @{ Name = 'scheduler-service';     Port = 8097; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-' },
    @{ Name = 'frontend';              Port = 4200; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-'; IsFrontend = $true },
    @{ Name = 'kafka';                 Port = 9092; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-'; IsDockerManaged = $true; IsTcpOnly = $true; LoginNote = 'No login (PLAINTEXT, no SASL) - bootstrap: localhost:9092' },
    @{ Name = 'kafka-ui';              Port = 8180; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-'; IsDockerManaged = $true; LoginNote = 'No login - open the URL to browse Kafka topics/brokers' },
    @{ Name = 'opensearch';            Port = 9200; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-'; IsDockerManaged = $true; IsTcpOnly = $true; UrlOverride = 'https://localhost:9200'; LoginUser = 'admin'; LoginPass = 'MyEshopping123!' },
    @{ Name = 'opensearch-dashboards'; Port = 5601; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-'; IsDockerManaged = $true; LoginUser = 'admin'; LoginPass = 'MyEshopping123!' },
    @{ Name = 'prometheus';            Port = 9090; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-'; IsDockerManaged = $true },
    @{ Name = 'grafana';               Port = 3000; DbType = 'None';       DbHost = '';          DbPort = 0;     DbName = '-';                DbUser = '-';           DbPass = '-'; IsDockerManaged = $true; LoginUser = 'admin'; LoginPass = 'myeshopping' }
) | Sort-Object { $_.Name }
