//package vn.vibeteam.vibe.app;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//import vn.vibeteam.vibe.repository.chat.MessageRepository;
//import vn.vibeteam.vibe.util.DataSeeder;
//
//@Component
//public class BenchmarkRunner implements CommandLineRunner {
//
//    private final DataSeeder dataSeeder;
//    private final MessageRepository messageRepository;
//    private JdbcTemplate jdbcTemplate;
//
//    public BenchmarkRunner(DataSeeder dataSeeder, MessageRepository messageRepository) {
//        this.dataSeeder = dataSeeder;
//        this.messageRepository = messageRepository;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("STARTING BENCHMARK DATA SEEDING...");
//
//        // 1. Generate CSV
//        String filePath = "messages_10m.csv"; // Store file temporarily on project root
////        dataSeeder.generateCsvFile(filePath, 10_000_000);
//
//        // 2. Clear existing data
//        System.out.println("Truncating table...");
//        jdbcTemplate.execute("TRUNCATE TABLE channel_messages RESTART IDENTITY CASCADE");
//
//        // 3. Import csv via stream
//        dataSeeder.importCsvViaStream(filePath);
//        System.out.println("DONE SEEDING! Ready to benchmark.");
//    }
//}