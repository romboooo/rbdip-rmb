CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);

INSERT INTO customers (id, full_name, address, phone)
SELECT id, customer_full_name, customer_address, customer_phone
FROM orders;

SELECT setval(
    pg_get_serial_sequence('customers', 'id'),
    COALESCE((SELECT MAX(id) FROM customers), 1),
    EXISTS (SELECT 1 FROM customers)
);

ALTER TABLE orders ADD COLUMN customer_id BIGINT;
UPDATE orders SET customer_id = id;
ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id);
ALTER TABLE orders
    DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;

ALTER TABLE order_items ADD COLUMN product_id BIGINT;

INSERT INTO products (name, price, description)
SELECT oi.product_name, oi.product_price, 'Migrated from historical order item'
FROM order_items oi
WHERE NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.name = oi.product_name AND p.price = oi.product_price
)
GROUP BY oi.product_name, oi.product_price;

UPDATE order_items oi
SET product_id = matches.id
FROM (
    SELECT name, price, MIN(id) AS id
    FROM products
    GROUP BY name, price
) matches
WHERE oi.product_name = matches.name
  AND oi.product_price = matches.price;

ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id);
ALTER TABLE order_items
    DROP COLUMN product_name,
    DROP COLUMN product_price;
