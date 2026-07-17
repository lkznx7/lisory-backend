-- Move 'Colar Sweet Cherry' from 'Pulseiras' to 'Colares'
UPDATE products 
SET category_id = (SELECT id FROM categories WHERE slug = 'colares' OR name = 'Colares' LIMIT 1)
WHERE slug = 'colar-sweet-cherry' OR name = 'Colar Sweet Cherry';
