CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100),
    status VARCHAR(100),
    user_id UUID NOT NULL,
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM now()) * 1000,
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM now()) * 1000
);

CREATE TABLE photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(250),
    content_type VARCHAR(250),
    status VARCHAR(100),
    file_size BIGINT,
    file_path_original VARCHAR(1000),
    file_path_complete VARCHAR(1000),
    file_hash VARCHAR(250),
    task_id UUID REFERENCES tasks(id) ON DELETE SET NULL,
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM now()) * 1000
);

-- Таблица метаданных с камеры (строго одна запись на фото)
CREATE TABLE cam_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    photo_id UUID NOT NULL UNIQUE REFERENCES photos(id) ON DELETE CASCADE,
    address VARCHAR(1000),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    bearing DOUBLE PRECISION,          -- азимут (в навигации)
    elevation DOUBLE PRECISION        -- угол наклона/места
);

-- Таблица метаданных конструкций (много записей на одно фото)
CREATE TABLE construct_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    position INTEGER,
    type VARCHAR(100),
    photo_id UUID NOT NULL REFERENCES photos(id) ON DELETE CASCADE,
    address VARCHAR(1000),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

-- Самые важные индексы
CREATE INDEX idx_photos_task_id ON photos(task_id);
CREATE INDEX idx_cam_metadata_photo_id ON cam_metadata(photo_id);
CREATE INDEX idx_construct_metadata_photo_id ON construct_metadata(photo_id);
CREATE INDEX idx_tasks_user_id ON tasks(user_id);