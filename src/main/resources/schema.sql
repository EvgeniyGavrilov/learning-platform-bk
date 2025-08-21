-- Пользователи
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    picture VARCHAR(500)
);

-- Коды
CREATE TABLE IF NOT EXISTS code (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

-- Курсы
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Разделы курса
CREATE TABLE IF NOT EXISTS sections (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Таблица уроков
CREATE TABLE IF NOT EXISTS lessons (
    id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    position INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE
);

-- Видео в уроке
CREATE TABLE IF NOT EXISTS videos (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    filename VARCHAR(500) NOT NULL,
    url VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- Доступ к курсам
CREATE TABLE course_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    purchased_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NULL,
    UNIQUE (user_id, course_id)
);