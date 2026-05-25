CREATE TABLE data_upls (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(50),
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_by VARCHAR(50),
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version INT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status TINYINT UNSIGNED NOT NULL,
    upload_id BIGINT,
    resource VARCHAR(255) NOT NULL,
    instruction_no VARCHAR(150) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    invalid_rows INT NOT NULL DEFAULT 0,
    committed_rows INT NOT NULL DEFAULT 0,
    upload_status TINYINT UNSIGNED NOT NULL,
    error_message TEXT
);

CREATE TABLE data_row_upls (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(50),
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_by VARCHAR(50),
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version INT NOT NULL DEFAULT 0,
    source_id BIGINT,
    lifecycle_status TINYINT UNSIGNED NOT NULL,
    upload_id BIGINT,
    data_upl_id BIGINT NOT NULL,
    row_number INT NOT NULL,
    row_data TEXT,
    row_status TINYINT UNSIGNED NOT NULL,
    identifier VARCHAR(255),
    lookup_value1 VARCHAR(255),
    lookup_value2 VARCHAR(255),
    lookup_value3 VARCHAR(255),
    error_message TEXT,
    CONSTRAINT fk_data_row_upls_data_upl_id FOREIGN KEY (data_upl_id) REFERENCES data_upls(id)
);

