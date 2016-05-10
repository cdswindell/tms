CREATE USER 'davids'@'localhost' IDENTIFIED BY 'mysql';
GRANT ALL PRIVILEGES ON *.* TO 'davids'@'localhost' WITH GRANT OPTION;
CREATE USER 'davids'@'%' IDENTIFIED BY 'mysql';
GRANT ALL PRIVILEGES ON *.* TO 'davids'@'%' WITH GRANT OPTION;
