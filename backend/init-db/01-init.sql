-- Create databases for each service
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS receipt_db;
CREATE DATABASE IF NOT EXISTS settings_db;

-- Grant permissions
GRANT ALL PRIVILEGES ON product_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON auth_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON receipt_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON settings_db.* TO 'restaurant'@'%';

FLUSH PRIVILEGES;
