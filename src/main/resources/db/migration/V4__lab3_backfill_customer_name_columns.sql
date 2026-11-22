UPDATE customers
SET first_name = split_part(btrim(full_name), ' ', 1),
    last_name = CASE
        WHEN position(' ' IN btrim(full_name)) = 0 THEN ''
        ELSE btrim(substring(btrim(full_name) FROM position(' ' IN btrim(full_name)) + 1))
    END
WHERE first_name IS NULL OR last_name IS NULL;
