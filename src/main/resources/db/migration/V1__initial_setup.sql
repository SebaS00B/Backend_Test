-- Creación de tabla roles
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Insertar roles iniciales
INSERT INTO roles (name) VALUES ('USER');
INSERT INTO roles (name) VALUES ('ADMIN');

-- Creación de tabla users
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100),
    lastname VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Tabla de relación users-roles
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Creación de tabla posts
CREATE TABLE posts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(MAX) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);