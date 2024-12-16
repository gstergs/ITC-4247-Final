-- we assume our db contains data for multiple tenants
-- customers are tenant-specific, products are global
CREATE TABLE IF NOT EXISTS tenants (
  id INT,
  name varchar(32)
);

-- API authorization: each apikey only has access to customers of given tenant
create TABLE IF NOT EXISTS apikeys (
  id INT AUTO_INCREMENT,
  tenant_id INT,
  apikey varchar(255),
  description varchar(255),
  expiration timestamp,
  enabled boolean
);

-- customers: each tenant only has access to his own customers
CREATE TABLE IF NOT EXISTS customers (
  id INT AUTO_INCREMENT,
  tenant_id INT,
  fullname varchar(32),
  age int,
  address varchar(100), zipcode varchar(5)
);

-- just two tenants for now
insert into tenants(id, name) values  (1, 'Athens Branch');
insert into tenants(id, name) values  (2, 'Thesalloniki Branch');

-- we have one key for each tenant, an api key that is disabled, and api key that has expired
insert into apikeys(tenant_id, apikey, description, enabled) values(1, '0000-1111-2222-3333', 'Test API key', true);
insert into apikeys(tenant_id, apikey, description, enabled) values(1, '000-1111-2222-disabled', 'Test API key (disabled)', false);
insert into apikeys(tenant_id, apikey, description, enabled, expiration) values(1, '000-1111-2222-expired', 'Test API key (disabled)', true, now()-1);
insert into apikeys(tenant_id, apikey, description, enabled) values(2, '4444-5555-6666-7777', 'Test API key', true);

-- We create 3 customers for each tenant
-- CAREFUL: the junit tests assume that we have the first 3 customers with tenant_id=1 and then another 3 with tenant_id=2
-- DO NOT CHANGE that since the tests assume their IDs in order to test cross-tenant data breaches
INSERT INTO customers (tenant_id, fullname, age, address, zipcode) VALUES (1, 'Alice Johnson', 25, '123 Maple Street', '12345');
INSERT INTO customers (tenant_id, fullname, age, address, zipcode) VALUES (1, 'Emily Davis', 30, '456 Oak Avenue', '23456');
INSERT INTO customers (tenant_id, fullname, age, address, zipcode) VALUES (1, 'Sophia Brown', 22, '789 Pine Road', '34567');
INSERT INTO customers (tenant_id, fullname, age, address, zipcode) VALUES (2, 'James Smith', 40, '555 Pine Avenue', '23432');
INSERT INTO customers (tenant_id, fullname, age, address, zipcode) VALUES (2, 'Liam Johnson', 38, '666 Maple Court', '34543');
INSERT INTO customers (tenant_id, fullname, age, address, zipcode) VALUES (2, 'Noah Williams', 35, '777 Birch Road', '45654');
