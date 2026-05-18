-- =====================================================================
-- Make resource_id nullable in storage_medias.
--
-- Supports uploading files before the owning entity is created.
-- The file can be linked later via the attach operation.
-- =====================================================================

ALTER TABLE storage_medias ALTER COLUMN resource_id DROP NOT NULL;
