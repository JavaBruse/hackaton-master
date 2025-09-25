CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100),
    status VARCHAR(100),
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM now()) * 1000,
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM now()) * 1000
);

CREATE TABLE photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_path VARCHAR(1000),
    file_hash VARCHAR(250),
    task_id UUID REFERENCES tasks(id) ON DELETE SET NULL,
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM now()) * 1000
);

CREATE TABLE photo_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    photo_id UUID REFERENCES photo(id) ON DELETE SET NULL,
    address VARCHAR(1000),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

