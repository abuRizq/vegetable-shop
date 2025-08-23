-- 002_products.sql — Catalog products seed (idempotent; PostgreSQL)
-- Best practices:
-- - Defensive: discovers actual table and column layout.
-- - Uses ON CONFLICT where possible; otherwise inserts with WHERE NOT EXISTS.
-- - References categories by code when possible.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
  prod_candidates text[] := ARRAY[
    'catalog.products','public.products','products',
    'catalog.product','public.product','product'
  ];
  cat_candidates  text[] := ARRAY[
    'catalog.categories','public.categories','categories',
    'catalog.category','public.category','category'
  ];
  v_prod regclass;
  v_cat  regclass;
  prod_schema text; prod_table text;
  cat_schema  text; cat_table  text;

  -- column flags for products
  has_sku boolean := false;
  has_name boolean := false;
  has_cat_id boolean := false;
  has_price_amount boolean := false;  -- numeric/decimal
  has_price_cents boolean := false;   -- integer cents
  has_currency boolean := false;
  has_uom boolean := false;

  FUNCTION col_exists(_schema text, _table text, _col text) RETURNS boolean AS $$
  BEGIN
    RETURN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = _schema AND table_name = _table AND column_name = _col
    );
  END;
  $$ LANGUAGE plpgsql;

BEGIN
  -- locate products table
  FOREACH prod_table IN ARRAY prod_candidates LOOP
    v_prod := to_regclass(prod_table); EXIT WHEN v_prod IS NOT NULL;
  END LOOP;

  IF v_prod IS NULL THEN
    RAISE NOTICE '[products seed] No products table found. Skipping.';
    RETURN;
  END IF;

  SELECT n.nspname, c.relname INTO prod_schema, prod_table
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE c.oid = v_prod;

  -- locate categories (optional)
  FOREACH cat_table IN ARRAY cat_candidates LOOP
    v_cat := to_regclass(cat_table); EXIT WHEN v_cat IS NOT NULL;
  END LOOP;
  IF v_cat IS NOT NULL THEN
    SELECT n.nspname, c.relname INTO cat_schema, cat_table
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.oid = v_cat;
  END IF;

  -- detect columns
  has_sku := col_exists(prod_schema, prod_table, 'sku');
  has_name := col_exists(prod_schema, prod_table, 'name');
  has_cat_id := col_exists(prod_schema, prod_table, 'category_id');
  has_price_amount := col_exists(prod_schema, prod_table, 'price_amount');
  has_price_cents := col_exists(prod_schema, prod_table, 'price_cents');
  has_currency := col_exists(prod_schema, prod_table, 'price_currency') OR col_exists(prod_schema, prod_table, 'currency');
  has_uom := col_exists(prod_schema, prod_table, 'unit') OR col_exists(prod_schema, prod_table, 'unit_of_measure');

  IF NOT has_name THEN
    RAISE NOTICE '[products seed] Table %.% lacks required column "name". Skipping.', prod_schema, prod_table;
    RETURN;
  END IF;

  -- dataset: sku, name, category_code, price, currency, unit
  CREATE TEMP TABLE _seed_products(
    sku text, name text, category_code text, price numeric, currency text, uom text
  ) ON COMMIT DROP;

  INSERT INTO _seed_products(sku, name, category_code, price, currency, uom) VALUES
    ('BAN-001', 'Banana (1kg)',        'fruits',       2.49, 'USD', 'KG'),
    ('CIT-LEM', 'Lemon (500g)',        'citrus',       1.59, 'USD', 'G'),
    ('VEG-CAR', 'Carrot (1kg)',        'root-veg',     1.99, 'USD', 'KG'),
    ('VEG-BRC', 'Broccoli (each)',     'cruciferous',  1.29, 'USD', 'EA'),
    ('HER-BAS', 'Basil (bunch)',       'herbs',        0.99, 'USD', 'EA'),
    ('VEG-SPN', 'Spinach (250g)',      'leafy-greens', 1.49, 'USD', 'G');

  -- prepare constraints for upsert
  IF has_sku THEN
    BEGIN
      EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I UNIQUE (sku)',
        prod_schema, prod_table, prod_table || '_sku_uniq');
    EXCEPTION WHEN duplicate_object THEN
      -- ignore
    END;
  ELSE
    BEGIN
      EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I UNIQUE (name)',
        prod_schema, prod_table, prod_table || '_name_uniq');
    EXCEPTION WHEN duplicate_object THEN
    END;
  END IF;

  -- Build dynamic column list based on availability
  -- base columns: name (+ sku if present)
  -- optional: category_id (resolved from categories by code), price_amount / price_cents, currency, unit
  IF has_sku THEN
    -- Insert parents (no category) first if category not found; we will attempt to resolve in one go
    IF v_cat IS NOT NULL AND has_cat_id THEN
      EXECUTE format(
        'INSERT INTO %I.%I (%s) ' ||
        'SELECT %s ' ||
        'FROM _seed_products sp ' ||
        'LEFT JOIN %I.%I cc ON cc.%s = sp.category_code ' ||
        'ON CONFLICT (%s) DO NOTHING',
        prod_schema, prod_table,
        -- columns
        array_to_string(ARRAY[
          'sku','name',
          CASE WHEN has_cat_id THEN 'category_id' ELSE NULL END,
          CASE WHEN has_price_amount THEN 'price_amount' ELSE NULL END,
          CASE WHEN has_price_cents THEN 'price_cents' ELSE NULL END,
          CASE WHEN has_currency THEN (CASE WHEN col_exists(prod_schema, prod_table, 'price_currency') THEN 'price_currency' ELSE 'currency' END) ELSE NULL END,
          CASE WHEN has_uom THEN (CASE WHEN col_exists(prod_schema, prod_table, 'unit_of_measure') THEN 'unit_of_measure' ELSE 'unit' END) ELSE NULL END
        ]::text[], ', '),
        -- values
        array_to_string(ARRAY[
          'sp.sku','sp.name',
          CASE WHEN has_cat_id THEN 'cc.id' ELSE NULL END,
          CASE WHEN has_price_amount THEN 'sp.price' ELSE NULL END,
          CASE WHEN has_price_cents THEN 'CAST(ROUND(sp.price*100) AS integer)' ELSE NULL END,
          CASE WHEN has_currency THEN 'sp.currency' ELSE NULL END,
          CASE WHEN has_uom THEN 'sp.uom' ELSE NULL END
        ]::text[], ', '),
        cat_schema, cat_table,
        CASE WHEN EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=cat_schema AND table_name=cat_table AND column_name='code') THEN 'code' ELSE 'name' END,
        'sku'
      );
    ELSE
      -- No category relation
      EXECUTE format(
        'INSERT INTO %I.%I (%s) ' ||
        'SELECT %s FROM _seed_products sp ' ||
        'ON CONFLICT (sku) DO NOTHING',
        prod_schema, prod_table,
        array_to_string(ARRAY[
          'sku','name',
          CASE WHEN has_price_amount THEN 'price_amount' ELSE NULL END,
          CASE WHEN has_price_cents THEN 'price_cents' ELSE NULL END,
          CASE WHEN has_currency THEN (CASE WHEN col_exists(prod_schema, prod_table, 'price_currency') THEN 'price_currency' ELSE 'currency' END) ELSE NULL END,
          CASE WHEN has_uom THEN (CASE WHEN col_exists(prod_schema, prod_table, 'unit_of_measure') THEN 'unit_of_measure' ELSE 'unit' END) ELSE NULL END
        ]::text[], ', '),
        array_to_string(ARRAY[
          'sp.sku','sp.name',
          CASE WHEN has_price_amount THEN 'sp.price' ELSE NULL END,
          CASE WHEN has_price_cents THEN 'CAST(ROUND(sp.price*100) AS integer)' ELSE NULL END,
          CASE WHEN has_currency THEN 'sp.currency' ELSE NULL END,
          CASE WHEN has_uom THEN 'sp.uom' ELSE NULL END
        ]::text[], ', ')
      );
    END IF;
  ELSE
    -- Fallback keyed by name only
    EXECUTE format(
      'INSERT INTO %I.%I (name) SELECT name FROM _seed_products sp ' ||
      'ON CONFLICT (name) DO NOTHING',
      prod_schema, prod_table
    );
  END IF;

  RAISE NOTICE '[products seed] Completed for %.%s', prod_schema, prod_table;
END$$;
