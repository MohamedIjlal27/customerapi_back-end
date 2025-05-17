-- Insert initial countries
INSERT INTO countries (name, code) 
SELECT 'United States', 'US' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'US')
UNION ALL
SELECT 'United Kingdom', 'UK' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'UK')
UNION ALL
SELECT 'Canada', 'CA' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'CA')
UNION ALL
SELECT 'Australia', 'AU' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'AU')
UNION ALL
SELECT 'Germany', 'DE' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'DE')
UNION ALL
SELECT 'France', 'FR' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'FR')
UNION ALL
SELECT 'Japan', 'JP' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'JP')
UNION ALL
SELECT 'China', 'CN' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'CN')
UNION ALL
SELECT 'India', 'IN' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'IN')
UNION ALL
SELECT 'Brazil', 'BR' WHERE NOT EXISTS (SELECT 1 FROM countries WHERE code = 'BR');

-- Insert Cities
INSERT INTO cities (name, country_id)
SELECT 'New York', 1 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'New York' AND country_id = 1)
UNION ALL
SELECT 'Los Angeles', 1 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Los Angeles' AND country_id = 1)
UNION ALL
SELECT 'Chicago', 1 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Chicago' AND country_id = 1)
UNION ALL
SELECT 'London', 2 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'London' AND country_id = 2)
UNION ALL
SELECT 'Manchester', 2 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Manchester' AND country_id = 2)
UNION ALL
SELECT 'Toronto', 3 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Toronto' AND country_id = 3)
UNION ALL
SELECT 'Vancouver', 3 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Vancouver' AND country_id = 3)
UNION ALL
SELECT 'Sydney', 4 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Sydney' AND country_id = 4)
UNION ALL
SELECT 'Melbourne', 4 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Melbourne' AND country_id = 4)
UNION ALL
SELECT 'Berlin', 5 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Berlin' AND country_id = 5)
UNION ALL
SELECT 'Munich', 5 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Munich' AND country_id = 5)
UNION ALL
SELECT 'Paris', 6 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Paris' AND country_id = 6)
UNION ALL
SELECT 'Lyon', 6 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Lyon' AND country_id = 6)
UNION ALL
SELECT 'Tokyo', 7 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Tokyo' AND country_id = 7)
UNION ALL
SELECT 'Osaka', 7 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Osaka' AND country_id = 7)
UNION ALL
SELECT 'Beijing', 8 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Beijing' AND country_id = 8)
UNION ALL
SELECT 'Shanghai', 8 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Shanghai' AND country_id = 8)
UNION ALL
SELECT 'Mumbai', 9 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Mumbai' AND country_id = 9)
UNION ALL
SELECT 'Delhi', 9 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Delhi' AND country_id = 9)
UNION ALL
SELECT 'Rio de Janeiro', 10 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Rio de Janeiro' AND country_id = 10)
UNION ALL
SELECT 'São Paulo', 10 WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'São Paulo' AND country_id = 10); 