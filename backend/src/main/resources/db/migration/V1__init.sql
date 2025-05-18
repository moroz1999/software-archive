CREATE TABLE software
(
    id          UUID PRIMARY KEY,
    name        text NOT NULL,
    description text,
    created_at  timestamp DEFAULT now()
)