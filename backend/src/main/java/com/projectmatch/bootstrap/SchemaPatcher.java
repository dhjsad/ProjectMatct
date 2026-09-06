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
                addColumnIfMissing(connection, statement, "project", "deploy_service_enabled", "INT DEFAULT 1");
                addColumnIfMissing(connection, statement, "project_resource", "file_name", "VARCHAR(256)");
                addColumnIfMissing(connection, statement, "project_resource", "file_path", "VARCHAR(512)");
                addColumnIfMissing(connection, statement, "project_resource", "file_size", "BIGINT");
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
                statement.execute("UPDATE project SET deploy_price = 199 WHERE deploy_price IS NULL");
                statement.execute("UPDATE project SET deploy_service_enabled = 1 WHERE deploy_service_enabled IS NULL");
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
