-- Создаем схему
CREATE SCHEMA IF NOT EXISTS budget_journal;

-- Устанавливаем схему по умолчанию
SET search_path TO budget_journal;

-- Создаем таблицу категорий
CREATE TABLE IF NOT EXISTS budget_journal.categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Создаем таблицу записей
CREATE TABLE IF NOT EXISTS budget_journal.records (
    id SERIAL PRIMARY KEY,
    operation_type CHAR(1) NOT NULL CHECK (operation_type IN ('+', '-')),
    category_id INTEGER REFERENCES budget_journal.categories(id),
    operation_date DATE NOT NULL,
    total DECIMAL(10,2) NOT NULL
);

-- Создаем индексы
CREATE INDEX IF NOT EXISTS idx_records_date ON budget_journal.records(operation_date);
CREATE INDEX IF NOT EXISTS idx_records_category ON budget_journal.records(category_id);

-- Добавляем базовые категории
INSERT INTO budget_journal.categories (name) VALUES
    ('Зарплата'),
    ('Продукты'),
    ('Транспорт'),
    ('Коммунальные услуги'),
    ('Развлечения'),
    ('Здоровье'),
    ('Одежда'),
    ('Инвестиции'),
    ('Аренда'),
    ('Связь и интернет')
ON CONFLICT (name) DO NOTHING;

-- Предоставляем права доступа
GRANT ALL PRIVILEGES ON SCHEMA budget_journal TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA budget_journal TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA budget_journal TO postgres; 