DROP TRIGGER trg_sync_customer_name_versions ON customers;
DROP FUNCTION sync_customer_name_versions();

ALTER TABLE customers
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL,
    DROP COLUMN full_name;
