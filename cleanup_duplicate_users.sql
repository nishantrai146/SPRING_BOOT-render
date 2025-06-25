-- Step 1: Identify duplicate usernames
SELECT username, COUNT(*) as count
FROM user
GROUP BY username
HAVING COUNT(*) > 1;

-- Step 2: Create a temporary table with the user IDs to keep (the ones with the lowest ID)
CREATE TEMPORARY TABLE users_to_keep AS
SELECT MIN(id) as id_to_keep
FROM user
GROUP BY username;

-- Step 3: Delete all user permissions for users that will be deleted
DELETE FROM page_permission
WHERE user_id NOT IN (SELECT id_to_keep FROM users_to_keep);

-- Step 4: Delete duplicate users (keeping only the one with the lowest ID)
DELETE FROM user
WHERE id NOT IN (SELECT id_to_keep FROM users_to_keep);

-- Step 5: Verify that duplicates are gone
SELECT username, COUNT(*) as count
FROM user
GROUP BY username
HAVING COUNT(*) > 1; 