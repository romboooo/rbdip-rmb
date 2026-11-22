ALTER TABLE customers
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255);

CREATE FUNCTION sync_customer_name_versions() RETURNS trigger AS $$
DECLARE
    normalized_full_name TEXT;
    separator_position INTEGER;
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.first_name IS NULL AND NEW.last_name IS NULL THEN
            normalized_full_name := btrim(NEW.full_name);
            separator_position := position(' ' IN normalized_full_name);
            NEW.first_name := split_part(normalized_full_name, ' ', 1);
            NEW.last_name := CASE
                WHEN separator_position = 0 THEN ''
                ELSE btrim(substring(normalized_full_name FROM separator_position + 1))
            END;
        ELSE
            NEW.full_name := btrim(concat_ws(
                ' ', NULLIF(btrim(NEW.first_name), ''), NULLIF(btrim(NEW.last_name), '')));
        END IF;
    ELSIF NEW.first_name IS DISTINCT FROM OLD.first_name
            OR NEW.last_name IS DISTINCT FROM OLD.last_name THEN
        NEW.full_name := btrim(concat_ws(
            ' ', NULLIF(btrim(NEW.first_name), ''), NULLIF(btrim(NEW.last_name), '')));
    ELSIF NEW.full_name IS DISTINCT FROM OLD.full_name THEN
        normalized_full_name := btrim(NEW.full_name);
        separator_position := position(' ' IN normalized_full_name);
        NEW.first_name := split_part(normalized_full_name, ' ', 1);
        NEW.last_name := CASE
            WHEN separator_position = 0 THEN ''
            ELSE btrim(substring(normalized_full_name FROM separator_position + 1))
        END;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_customer_name_versions
    BEFORE INSERT OR UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION sync_customer_name_versions();
