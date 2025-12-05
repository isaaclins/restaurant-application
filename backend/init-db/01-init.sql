-- Create databases for each service
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS receipt_db;
CREATE DATABASE IF NOT EXISTS settings_db;

-- Create user if not exists (MySQL 8.0+ syntax)
-- Note: The 'restaurant' user is also created by Docker's MYSQL_USER env var,
-- but we ensure it exists with proper privileges here
CREATE USER IF NOT EXISTS 'restaurant'@'%' IDENTIFIED BY 'RestaurantDev2025!';
CREATE USER IF NOT EXISTS 'restaurant'@'localhost' IDENTIFIED BY 'RestaurantDev2025!';

-- Grant permissions
GRANT ALL PRIVILEGES ON product_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON auth_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON receipt_db.* TO 'restaurant'@'%';
GRANT ALL PRIVILEGES ON settings_db.* TO 'restaurant'@'%';

GRANT ALL PRIVILEGES ON product_db.* TO 'restaurant'@'localhost';
GRANT ALL PRIVILEGES ON order_db.* TO 'restaurant'@'localhost';
GRANT ALL PRIVILEGES ON auth_db.* TO 'restaurant'@'localhost';
GRANT ALL PRIVILEGES ON receipt_db.* TO 'restaurant'@'localhost';
GRANT ALL PRIVILEGES ON settings_db.* TO 'restaurant'@'localhost';

FLUSH PRIVILEGES;
