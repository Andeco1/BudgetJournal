-- Drop existing schema if exists
DROP SCHEMA IF EXISTS budget_journal CASCADE;

-- Create schema
CREATE SCHEMA budget_journal;

-- Set search path
SET search_path TO budget_journal;

-- Create categories table
CREATE TABLE budget_journal.categories (
    id_category SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL
);

-- Create records table
CREATE TABLE budget_journal.records (
    id_record SERIAL PRIMARY KEY,
    operation BOOLEAN NOT NULL, -- false for income, true for expense
    id_category INTEGER REFERENCES budget_journal.categories(id_category),
    date_operation DATE NOT NULL,
    total DECIMAL(10,2) NOT NULL
);

-- Create indexes
CREATE INDEX idx_records_date ON budget_journal.records(date_operation);
CREATE INDEX idx_records_category ON budget_journal.records(id_category);

-- Insert income categories
INSERT INTO budget_journal.categories (category_name) VALUES
    ('Зарплата'),
    ('Фриланс'),
    ('Инвестиции'),
    ('Подработка'),
    ('Возврат долга'),
    ('Подарки')
ON CONFLICT (category_name) DO NOTHING;

-- Insert expense categories
INSERT INTO budget_journal.categories (category_name) VALUES
    ('Продукты'),
    ('Транспорт'),
    ('Коммунальные услуги'),
    ('Развлечения'),
    ('Одежда'),
    ('Здоровье'),
    ('Образование'),
    ('Подарки'),
    ('Аренда'),
    ('Связь и интернет'),
    ('Рестораны и кафе'),
    ('Хобби'),
    ('Красота и уход'),
    ('Домашние животные'),
    ('Ремонт'),
    ('Путешествия'),
    ('Спорт'),
    ('Кредиты'),
    ('Налоги'),
    ('Страхование')
ON CONFLICT (category_name) DO NOTHING;

-- Grant access rights
GRANT ALL PRIVILEGES ON SCHEMA budget_journal TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA budget_journal TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA budget_journal TO postgres;

-- Verify categories were created
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM budget_journal.categories LIMIT 1) THEN
        RAISE EXCEPTION 'Categories were not created properly';
    END IF;
END $$; 