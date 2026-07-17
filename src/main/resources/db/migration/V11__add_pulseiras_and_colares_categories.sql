-- Add Pulseiras and Colares categories
INSERT INTO categories (id, name, slug, description, active, created_at, updated_at)
VALUES 
    (uuid_generate_v4(), 'Pulseiras', 'pulseiras', 'Coleção especial de pulseiras', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'Colares', 'colares', 'Coleção especial de colares', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (slug) DO NOTHING;
