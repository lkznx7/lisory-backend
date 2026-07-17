-- Move 'Colar Sweet Cherry' from 'Pulseiras' to 'Colares'
UPDATE products 
SET category_id = (SELECT id FROM categories WHERE slug = 'colares' LIMIT 1)
WHERE slug = 'colar-sweet-cherry';
