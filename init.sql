-- Создаем схему
CREATE SCHEMA IF NOT EXISTS budget_journal;

-- Устанавливаем схему по умолчанию
SET search_path TO budget_journal;

-- Создаем таблицу категорий
CREATE TABLE IF NOT EXISTS budget_journal.categories (
    id_category SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL
);

-- Создаем таблицу записей
CREATE TABLE IF NOT EXISTS budget_journal.records (
    id_record SERIAL PRIMARY KEY,
    operation BOOLEAN NOT NULL, -- false for income, true for expense
    id_category INTEGER REFERENCES budget_journal.categories(id_category),
    date_operation DATE NOT NULL,
    total DECIMAL(10,2) NOT NULL
);

-- Создаем индексы
CREATE INDEX IF NOT EXISTS idx_records_date ON budget_journal.records(date_operation);
CREATE INDEX IF NOT EXISTS idx_records_category ON budget_journal.records(id_category);

-- Добавляем базовые категории
INSERT INTO budget_journal.categories (category_name) VALUES
    ('Зарплата'),
    ('Фриланс'),
    ('Инвестиции'),
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
    ('Спорт')
ON CONFLICT (category_name) DO NOTHING;

-- Предоставляем права доступа
GRANT ALL PRIVILEGES ON SCHEMA budget_journal TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA budget_journal TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA budget_journal TO postgres; 