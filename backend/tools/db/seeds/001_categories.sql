-- 001_categories.sql — Catalog categories seed (idempotent; PostgreSQL)
-- Best practices:
-- - Guarded against re-runs.
-- - Works with multiple plausible table names (catalog/public; category/categories).
-- - Inserts by natural key `code` when present.
-- - Hierarchical parent relationship is optional (if column exists).

-- Optional: enable pgcrypto for gen_random_uuid() (safe if already enabled)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
  candidates text[] := ARRAY[
    'catalog.categories','public.categories','categories',
    'catalog.category','public.category','category'
  ];
  v_tbl regclass;
  v_schema text;
  v_table  text;
  has_parent boolean := false;
  has_code   boolean := false;
  has_name   boolean := false;

  -- helper to test column existence on v_schema.v_table
  FUNCTION col_exists(col text) RETURNS boolean AS $$
  BEGIN
    RETURN EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = v_schema AND table_name = v_table AND column_name = col
    );
  END;
  $$ LANGUAGE plpgsql;

BEGIN
  -- Find the first existing table from candidates
  FOREACH v_table IN ARRAY candidates LOOP
    v_tbl := to_regclass(v_table);
    EXIT WHEN v_tbl IS NOT NULL;
  END LOOP;

  IF v_tbl IS NULL THEN
    RAISE NOTICE '[categories seed] No categories table found (tried: %). Skipping.', array_to_string(candidates, ', ');
    RETURN;
  END IF;

  SELECT n.nspname, c.relname INTO v_schema, v_table
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE c.oid = v_tbl;

  has_parent := col_exists('parent_id');
  has_code   := col_exists('code');
  has_name   := col_exists('name');

  IF NOT has_name THEN
    RAISE NOTICE '[categories seed] Table %.% lacks required column "name". Skipping.', v_schema, v_table;
    RETURN;
  END IF;

  -- Seed data set (code, name, parent_code)
  -- Adjust or extend as needed
  CREATE TEMP TABLE _seed_categories(code text, name text, parent_code text) ON COMMIT DROP;
  INSERT INTO _seed_categories(code, name, parent_code) VALUES
    ('vegetables',    'Vegetables',   NULL),
    ('leafy-greens',  'Leafy Greens', 'vegetables'),
    ('root-veg',      'Root Vegetables', 'vegetables'),
    ('cruciferous',   'Cruciferous',  'vegetables'),
    ('fruits',        'Fruits',       NULL),
    ('citrus',        'Citrus',       'fruits'),
    ('berries',       'Berries',      'fruits'),
    ('herbs',         'Herbs',        NULL);

  -- Upsert rows; prefer matching by code if column exists, else by name
  IF has_code THEN
    -- Ensure uniqueness by code if not already enforced
    BEGIN
      EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I UNIQUE (code)',
        v_schema, v_table, v_table || '_code_uniq');
    EXCEPTION WHEN duplicate_object THEN
      -- ignore
    END;

    -- Insert parents first
    EXECUTE format(
      'INSERT INTO %I.%I (%s%s%s) ' ||
      'SELECT s.code, s.name%s '   ||
      'FROM _seed_categories s '   ||
      'WHERE s.parent_code IS NULL '||
      'ON CONFLICT (code) DO NOTHING',
      v_schema, v_table,
      CASE WHEN has_code THEN 'code' ELSE '' END,
      CASE WHEN has_name AND has_code THEN ', name' WHEN has_name THEN 'name' ELSE '' END,
      CASE WHEN has_parent THEN ', parent_id' ELSE '' END,
      CASE WHEN has_parent THEN ', NULL' ELSE ''
      END
    );

    -- Then children with parent_id if possible
    IF has_parent THEN
      EXECUTE format(
        'INSERT INTO %I.%I (code, name, parent_id) ' ||
        'SELECT c.code, c.name, p.id ' ||
        'FROM _seed_categories c ' ||
        'JOIN %I.%I p ON p.code = c.parent_code ' ||
        'WHERE c.parent_code IS NOT NULL ' ||
        'ON CONFLICT (code) DO NOTHING',
        v_schema, v_table, v_schema, v_table
      );
    ELSE
      EXECUTE format(
        'INSERT INTO %I.%I (code, name) ' ||
        'SELECT c.code, c.name FROM _seed_categories c WHERE c.parent_code IS NOT NULL ' ||
        'ON CONFLICT (code) DO NOTHING',
        v_schema, v_table
      );
    END IF;
  ELSE
    -- Fallback: match by name
    BEGIN
      EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I UNIQUE (name)',
        v_schema, v_table, v_table || '_name_uniq');
    EXCEPTION WHEN duplicate_object THEN
      -- ignore
    END;

    EXECUTE format(
      'INSERT INTO %I.%I (name) ' ||
      'SELECT DISTINCT name FROM _seed_categories ' ||
      'ON CONFLICT (name) DO NOTHING',
      v_schema, v_table
    );
  END IF;

  RAISE NOTICE '[categories seed] Completed for %.%s', v_schema, v_table;
END$$;
