CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(128),
    password VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    major VARCHAR(64),
    skill_level VARCHAR(32),
    tech_stack VARCHAR(1024),
    interests VARCHAR(512),
    expected_difficulty VARCHAR(32),
    expected_duration INT,
    bio VARCHAR(512)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    slug VARCHAR(128),
    description LONGTEXT,
    project_type VARCHAR(64),
    category VARCHAR(64),
    tech_stack VARCHAR(512),
    difficulty VARCHAR(32),
    estimated_duration INT,
    suitable_for VARCHAR(256),
    modules LONGTEXT,
    architecture LONGTEXT,
    db_design LONGTEXT,
    deploy_guide LONGTEXT,
    sample_code LONGTEXT,
    tutorial LONGTEXT,
    remaining_count INT DEFAULT 1,
    sale_price INT DEFAULT 399,
    deploy_price INT DEFAULT 199,
    guide_price INT DEFAULT 299,
    deploy_service_enabled INT DEFAULT 1,
    status VARCHAR(32) DEFAULT 'PUBLISHED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS project_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    resource_type VARCHAR(32),
    title VARCHAR(128),
    content LONGTEXT,
    access_type VARCHAR(32),
    file_name VARCHAR(256),
    file_path VARCHAR(512),
    file_size BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS project_claim (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    delivery_type VARCHAR(32) DEFAULT 'SELF_DOWNLOAD',
    contact VARCHAR(128),
    amount INT DEFAULT 0,
    guide_amount INT DEFAULT 0,
    claim_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_recommendation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    project_id BIGINT,
    match_score DECIMAL(5,2),
    recommend_reason LONGTEXT,
    status VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS site_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    msg_type VARCHAR(32),
    title VARCHAR(128),
    content LONGTEXT,
    read_flag INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS deploy_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    contact VARCHAR(128),
    environment_note LONGTEXT,
    amount INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    pay_time TIMESTAMP NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(256) NOT NULL,
    slug VARCHAR(128),
    summary VARCHAR(512),
    content LONGTEXT,
    keywords VARCHAR(256),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS site_visit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visitor_id VARCHAR(64) NOT NULL,
    user_id BIGINT,
    username VARCHAR(64),
    ip VARCHAR(64),
    path VARCHAR(256),
    user_agent VARCHAR(256),
    visit_date DATE NOT NULL,
    page_views INT DEFAULT 1,
    first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_visitor_date (visitor_id, visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS site_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_key VARCHAR(32) NOT NULL UNIQUE,
    content VARCHAR(512),
    font_family VARCHAR(128),
    font_size VARCHAR(32),
    color VARCHAR(32),
    bg_color VARCHAR(32),
    enabled_flag INT DEFAULT 1,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS custom_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    contact_name VARCHAR(64),
    contact VARCHAR(128) NOT NULL,
    company VARCHAR(128),
    title VARCHAR(256),
    requirement LONGTEXT,
    budget VARCHAR(64),
    status VARCHAR(32) DEFAULT 'PENDING',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
