package com.cluely.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class DbHealthController {

    private final DataSource dataSource;

    public DbHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health/db")
    public String checkDb() {
        try (Connection ignored = dataSource.getConnection()) {
            return "DB CONNECTED ✅";
        } catch (Exception e) {
            return "DB CONNECTION FAILED ❌";
        }
    }
}
