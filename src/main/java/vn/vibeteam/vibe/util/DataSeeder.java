package vn.vibeteam.vibe.util;

import net.datafaker.Faker;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // GENERATE CSV
    public void generateCsvFile(String filePath, int count) {
        System.out.println("Start generating " + count + " rows to " + filePath + "...");
        Faker faker = new Faker();

        /*try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath), 8192 * 4)) {
            LocalDateTime timeCursor = LocalDateTime.now().minusYears(1);

            for (int i = 1; i <= count; i++) {
                long id = i;
                long channelId = 1L;
                long authorId = faker.number().numberBetween(1, 10); // Random 10 user
                // Client unique id using index temporarily for simplicity
                String clientUniqueId = String.valueOf(i);
                String content = "Msg " + i + ": " + faker.lorem().sentence();
                String createdAt = Timestamp.valueOf(timeCursor.plusSeconds(i)).toString();

                // Format: id|channel_id|author_id|client_unique_id|content|created_at
                // Dùng dấu | để tránh va chạm với dấu phẩy trong content
                String line = String.format("%d|%d|%d|%s|%s|%s",
                                            id,
                                            channelId,
                                            authorId,
                                            clientUniqueId,
                                            content,
                                            createdAt
                );

                writer.write(line);
                writer.newLine();

                if (i % 100000 == 0) {
                    System.out.println("Generated " + i + " rows...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String[] randomContents = new String[1000];
        for(int i=0; i<1000; i++) randomContents[i] = faker.lorem().sentence();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath), 64 * 1024)) { // Buffer to
            LocalDateTime timeCursor = LocalDateTime.now().minusYears(1);
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i <= count; i++) {
                sb.setLength(0); // Reset builder

                sb.append(i)
                  .append("|")
                  .append(1L)
                  .append("|")
                  .append(faker.number().numberBetween(1, 10))
                  .append("|")
                  .append(i)
                  .append("Msg ").append(i).append(": ").append(randomContents[i % 1000]).append("|")
                  .append(java.sql.Timestamp.valueOf(timeCursor.plusSeconds(i)));

                writer.write(sb.toString());
                writer.newLine();

                if (i % 100000 == 0) System.out.println("Gen: " + i);
            }
        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("DONE generation!");
    }

    // IMPORT CSV (STREAMING)
    public void importCsvViaStream(String filePath) {
        System.out.println("Start importing from " + filePath + "...");

        jdbcTemplate.execute("ALTER TABLE channel_messages SET UNLOGGED");
        jdbcTemplate.execute("ALTER TABLE channel_messages DISABLE TRIGGER ALL");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_channel_messages_pagination");

        long startTime = System.currentTimeMillis();

        try {
            jdbcTemplate.execute((Connection conn) -> {
                try {
                    // 1. Unwrap connection to PostgreSQL connection
                    BaseConnection pgConn = conn.unwrap(BaseConnection.class);
                    // 2. Create CopyManager
                    CopyManager copyManager = new CopyManager(pgConn);
                    // Note: Column order must match CSV file
                    String sql = "COPY channel_messages (id, channel_id, author_id, client_unique_id, content, created_at) " +
                                 "FROM STDIN WITH (FORMAT CSV, DELIMITER '|', HEADER FALSE)";
                    // 3. Import data from CSV file
                    int bufferSize = 64 * 1024;

                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath), bufferSize)) {
                        long rowsAffected = copyManager.copyIn(sql, reader);
                        System.out.println("Imported " + rowsAffected + " rows successfully!");
                    }

                } catch (IOException | SQLException e) {
                    throw new RuntimeException("Error during COPY import", e);
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Import Failed!");
        }

        System.out.println("Restoring table settings & building indexes...");
        jdbcTemplate.execute("ALTER TABLE channel_messages SET LOGGED");
        jdbcTemplate.execute("ALTER TABLE channel_messages ENABLE TRIGGER ALL");
        jdbcTemplate.execute("CREATE INDEX idx_channel_messages_pagination ON channel_messages (channel_id, id DESC)");
        jdbcTemplate.execute("VACUUM ANALYZE channel_messages");

        long endTime = System.currentTimeMillis();
        System.out.println("Total Import Time: " + (endTime - startTime) + "ms");
    }
}