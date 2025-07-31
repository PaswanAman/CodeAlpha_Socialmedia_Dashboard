-- Migration to add is_default column to vehicle table
-- This migration adds the is_default column and ensures only one vehicle per user can be default

-- Add the is_default column
ALTER TABLE vehicle ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;

-- Create an index for better performance when querying default vehicles
CREATE INDEX idx_vehicle_user_default ON vehicle(user_id, is_default);

-- Add a unique constraint to ensure only one default vehicle per user
-- This constraint will prevent multiple default vehicles for the same user
CREATE UNIQUE INDEX idx_vehicle_user_single_default 
ON vehicle(user_id) 
WHERE is_default = TRUE;

-- Update existing vehicles: make the first vehicle of each user the default
-- This handles existing data by setting the oldest vehicle as default for each user
WITH first_vehicles AS (
    SELECT DISTINCT ON (user_id) vehicle_id, user_id
    FROM vehicle
    ORDER BY user_id, added_at ASC
)
UPDATE vehicle 
SET is_default = TRUE 
WHERE vehicle_id IN (SELECT vehicle_id FROM first_vehicles);