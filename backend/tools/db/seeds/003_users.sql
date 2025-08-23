-- 003_users.sql — Initial users/roles seed (idempotent; PostgreSQL)
-- Best practices:
-- - Avoids plaintext passwords in Git; generates bcrypt hashes at runtime via pgcrypto: crypt(..., gen_salt('bf'))
-- - Defensive table discovery for common auth schemas.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
  user_candidates text[] := ARRAY[
    'auth.users','public.users','users','public.app_user','auth.app_user','app_user'
  ];
  role_candidates text[] := ARRAY[
    'auth.roles','public.roles','roles'
  ];
  ur_candidates text[] := ARRAY[
    'auth.user_roles','public.user_roles','user_roles','auth.users_roles','public.users_roles','users_roles'
  ];

  v_users regclass; users_schema text; users_table text;
  v_roles regclass; roles_schema text; roles_table text;
  v_ur    regclass; ur_schema text; ur_table text;

  has_email boolean := false;
  has_password boolean := false;        -- 'password' column
  has_password_hash boolean := false;   -- 'password_hash' column
  has_full_name boolean := false;
  has_enabled boolean := false;
  has_verified boolean := false;

  FUNCTION col_exists(_schema text, _table text, _col text) RETURNS boolean AS $$
  BEGIN
    RETURN EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = _schema AND table_name = _table AND column_name = _col
    );
  END;
  $$ LANGUAGE plpgsql;

BEGIN
  -- Discover users table
  FOREACH users_table IN ARRAY user_candidates LOOP
    v_users := to_regclass(users_table); EXIT WHEN v_users IS NOT NULL;
  END LOOP;

  IF v_users IS NULL THEN
    RAISE NOTICE '[users seed] No users table found. Skipping.';
    RETURN;
  END IF;

  SELECT n.nspname, c.relname INTO users_schema, users_table
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE c.oid = v_users;

  has_email := col_exists(users_schema, users_table, 'email');
  has_password := col_exists(users_schema, users_table, 'password');
  has_password_hash := col_exists(users_schema, users_table, 'password_hash');
  has_full_name := col_exists(users_schema, users_table, 'full_name') OR col_exists(users_schema, users_table, 'name');
  has_enabled := col_exists(users_schema, users_table, 'enabled') OR col_exists(users_schema, users_table, 'is_active');
  has_verified := col_exists(users_schema, users_table, 'email_verified') OR col_exists(users_schema, users_table, 'is_email_verified');

  IF NOT has_email THEN
    RAISE NOTICE '[users seed] Users table %.% lacks required column "email". Skipping.', users_schema, users_table;
    RETURN;
  END IF;

  -- Ensure unique email constraint for upsert
  BEGIN
    EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I UNIQUE (email)',
      users_schema, users_table, users_table || '_email_uniq');
  EXCEPTION WHEN duplicate_object THEN
    -- ignore
  END;

  -- Insert users (admin + demo)
  -- password placeholder: "ChangeMe123!"
  IF has_password_hash THEN
    EXECUTE format(
      'INSERT INTO %I.%I (%s) ' ||
      'SELECT %s ' ||
      'WHERE NOT EXISTS (SELECT 1 FROM %I.%I u WHERE u.email = %L)',
      users_schema, users_table,
      array_to_string(ARRAY[
        'email',
        CASE WHEN has_full_name THEN (CASE WHEN col_exists(users_schema, users_table, 'full_name') THEN 'full_name' ELSE 'name' END) ELSE NULL END,
        'password_hash',
        CASE WHEN has_enabled THEN (CASE WHEN col_exists(users_schema, users_table, 'enabled') THEN 'enabled' ELSE 'is_active' END) ELSE NULL END,
        CASE WHEN has_verified THEN (CASE WHEN col_exists(users_schema, users_table, 'email_verified') THEN 'email_verified' ELSE 'is_email_verified' END) ELSE NULL END
      ]::text[], ', '),
      array_to_string(ARRAY[
        '%L','%L', 'crypt(%L, gen_salt(''bf''))',
        CASE WHEN has_enabled THEN 'TRUE' ELSE NULL END,
        CASE WHEN has_verified THEN 'TRUE' ELSE NULL END
      ]::text[], ', '),
      users_schema, users_table, 'admin@veggieshop.local'
    )
    USING 'admin@veggieshop.local', 'Administrator', 'ChangeMe123!';
  ELSIF has_password THEN
    EXECUTE format(
      'INSERT INTO %I.%I (%s) ' ||
      'SELECT %s ' ||
      'WHERE NOT EXISTS (SELECT 1 FROM %I.%I u WHERE u.email = %L)',
      users_schema, users_table,
      array_to_string(ARRAY[
        'email',
        CASE WHEN has_full_name THEN (CASE WHEN col_exists(users_schema, users_table, 'full_name') THEN 'full_name' ELSE 'name' END) ELSE NULL END,
        'password',
        CASE WHEN has_enabled THEN (CASE WHEN col_exists(users_schema, users_table, 'enabled') THEN 'enabled' ELSE 'is_active' END) ELSE NULL END,
        CASE WHEN has_verified THEN (CASE WHEN col_exists(users_schema, users_table, 'email_verified') THEN 'email_verified' ELSE 'is_email_verified' END) ELSE NULL END
      ]::text[], ', '),
      array_to_string(ARRAY[
        '%L','%L', 'crypt(%L, gen_salt(''bf''))',
        CASE WHEN has_enabled THEN 'TRUE' ELSE NULL END,
        CASE WHEN has_verified THEN 'TRUE' ELSE NULL END
      ]::text[], ', '),
      users_schema, users_table, 'admin@veggieshop.local'
    )
    USING 'admin@veggieshop.local', 'Administrator', 'ChangeMe123!';
  ELSE
    EXECUTE format(
      'INSERT INTO %I.%I (email) VALUES (%L) ' ||
      'ON CONFLICT (email) DO NOTHING',
      users_schema, users_table, 'admin@veggieshop.local'
    );
  END IF;

  -- Optional: roles and user_roles if present
  FOREACH roles_table IN ARRAY role_candidates LOOP
    v_roles := to_regclass(roles_table); EXIT WHEN v_roles IS NOT NULL;
  END LOOP;
  FOREACH ur_table IN ARRAY ur_candidates LOOP
    v_ur := to_regclass(ur_table); EXIT WHEN v_ur IS NOT NULL;
  END LOOP;

  IF v_roles IS NOT NULL THEN
    SELECT n.nspname, c.relname INTO roles_schema, roles_table
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.oid = v_roles;

    -- ensure unique name/code
    BEGIN
      IF col_exists(roles_schema, roles_table, 'code') THEN
        EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I UNIQUE (code)',
          roles_schema, roles_table, roles_table || '_code_uniq');
      ELSE
        EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I UNIQUE (name)',
          roles_schema, roles_table, roles_table || '_name_uniq');
      END IF;
    EXCEPTION WHEN duplicate_object THEN END;

    -- insert roles
    IF col_exists(roles_schema, roles_table, 'code') THEN
      EXECUTE format('INSERT INTO %I.%I (code, name) VALUES (%L, %L) ON CONFLICT DO NOTHING',
        roles_schema, roles_table, 'ADMIN', 'Administrator');
      EXECUTE format('INSERT INTO %I.%I (code, name) VALUES (%L, %L) ON CONFLICT DO NOTHING',
        roles_schema, roles_table, 'USER', 'User');
    ELSE
      EXECUTE format('INSERT INTO %I.%I (name) VALUES (%L) ON CONFLICT DO NOTHING',
        roles_schema, roles_table, 'ADMIN');
      EXECUTE format('INSERT INTO %I.%I (name) VALUES (%L) ON CONFLICT DO NOTHING',
        roles_schema, roles_table, 'USER');
    END IF;
  END IF;

  -- link admin to ADMIN role if mapping table exists
  IF v_ur IS NOT NULL AND v_roles IS NOT NULL THEN
    SELECT n.nspname, c.relname INTO ur_schema, ur_table
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.oid = v_ur;

    -- locate ids
    PERFORM 1 FROM pg_class WHERE oid = v_users;  -- ensure users present
    IF col_exists(users_schema, users_table, 'id') THEN
      -- find admin id
      EXECUTE format('
        WITH u AS (SELECT id FROM %I.%I WHERE email = %L),
             r AS (SELECT id FROM %I.%I WHERE %s = %L)
        INSERT INTO %I.%I (user_id, role_id)
        SELECT u.id, r.id FROM u CROSS JOIN r
        ON CONFLICT DO NOTHING
      ',
        users_schema, users_table, 'admin@veggieshop.local',
        roles_schema, roles_table, CASE WHEN col_exists(roles_schema, roles_table, 'code') THEN 'code' ELSE 'name' END, 'ADMIN',
        ur_schema, ur_table
      );
    END IF;
  END IF;

  RAISE NOTICE '[users seed] Completed.';
END$$;
