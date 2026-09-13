package com.projectmatch.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Order(1)
public class SchemaPatcher implements CommandLineRunner {

    private final DataSource dataSource;

    public SchemaPatcher(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            Statement statement = connection.createStatement();
            try {
                addColumnIfMissing(connection, statement, "site_message", "project_id", "BIGINT");
                addColumnIfMissing(connection, statement, "site_message", "msg_type", "VARCHAR(32)");
                addColumnIfMissing(connection, statement, "project", "deploy_price", "INT DEFAULT 199");
                addColumnIfMissing(connection, statement, "project", "guide_price", "INT DEFAULT 299");
                addColumnIfMissing(connection, statement, "project", "sale_price", "INT DEFAULT 399");
                addColumnIfMissing(connection, statement, "project", "deploy_service_enabled", "INT DEFAULT 1");
                addColumnIfMissing(connection, statement, "project_resource", "file_name", "VARCHAR(256)");
                addColumnIfMissing(connection, statement, "project_resource", "file_path", "VARCHAR(512)");
                addColumnIfMissing(connection, statement, "project_resource", "file_size", "BIGINT");
                addColumnIfMissing(connection, statement, "project_claim", "delivery_type", "VARCHAR(32) DEFAULT 'SELF_DOWNLOAD'");
                addColumnIfMissing(connection, statement, "project_claim", "contact", "VARCHAR(128)");
                addColumnIfMissing(connection, statement, "project_claim", "amount", "INT DEFAULT 0");
                addColumnIfMissing(connection, statement, "project_claim", "guide_amount", "INT DEFAULT 0");
                statement.execute("CREATE TABLE IF NOT EXISTS site_visit ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "visitor_id VARCHAR(64) NOT NULL,"
                        + "user_id BIGINT,"
                        + "username VARCHAR(64),"
                        + "ip VARCHAR(64),"
                        + "path VARCHAR(256),"
                        + "user_agent VARCHAR(256),"
                        + "visit_date DATE NOT NULL,"
                        + "page_views INT DEFAULT 1,"
                        + "first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + "UNIQUE KEY uk_visitor_date (visitor_id, visit_date)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                statement.execute("CREATE TABLE IF NOT EXISTS site_banner ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "slot_key VARCHAR(32) NOT NULL UNIQUE,"
                        + "content VARCHAR(512),"
                        + "font_family VARCHAR(128),"
                        + "font_size VARCHAR(32),"
                        + "color VARCHAR(32),"
                        + "bg_color VARCHAR(32),"
                        + "enabled_flag INT DEFAULT 1,"
                        + "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                statement.execute("CREATE TABLE IF NOT EXISTS custom_order ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "user_id BIGINT,"
                        + "contact_name VARCHAR(64),"
                        + "contact VARCHAR(128) NOT NULL,"
                        + "company VARCHAR(128),"
                        + "title VARCHAR(256),"
                        + "requirement LONGTEXT,"
                        + "budget VARCHAR(64),"
                        + "status VARCHAR(32) DEFAULT 'PENDING',"
                        + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                statement.execute("UPDATE project SET deploy_price = 199 WHERE deploy_price IS NULL");
                statement.execute("UPDATE project SET deploy_service_enabled = 1 WHERE deploy_service_enabled IS NULL");
                statement.execute("UPDATE project SET sale_price = 399 WHERE sale_price IS NULL");
                statement.execute("UPDATE project SET guide_price = 299 WHERE guide_price IS NULL");
                statement.execute("UPDATE project_claim SET delivery_type = 'SELF_DOWNLOAD' WHERE delivery_type IS NULL");
            } finally {
                statement.close();
            }
        } finally {
            connection.close();
        }
    }

    private void addColumnIfMissing(Connection connection, Statement statement,
                                   String table, String column, String definition) throws Exception {
        ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), null, table, column);
        try {
            if (!columns.next()) {
                statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        } finally {
            columns.close();
        }
    }
}
