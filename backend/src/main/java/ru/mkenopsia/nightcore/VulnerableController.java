//package ru.mkenopsia.nightcore;
//
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.Base64;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/vuln")
//public class VulnerableController {
//
//    private static final String DB_PASSWORD = "SuperSecret123!";
//
//    @GetMapping("/command")
//    public String commandInjection(@RequestParam String cmd) {
//        try {
//            Process process = Runtime.getRuntime().exec(cmd);
//            String output = new BufferedReader(
//                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
//                    .lines().collect(Collectors.joining("\n"));
//            return "Result: " + output;
//        } catch (Exception e) {
//            return "Error: " + e.getMessage();
//        }
//    }
//
//    @GetMapping("/hash")
//    public String weakHash(@RequestParam String data) throws NoSuchAlgorithmException {
//        MessageDigest md = MessageDigest.getInstance("MD5");
//        byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
//        StringBuilder sb = new StringBuilder();
//        for (byte b : hash) sb.append(String.format("%02x", b));
//        return "MD5: " + sb;
//    }
//
//    @GetMapping("/encrypt")
//    public String weakEncryption(@RequestParam String data) throws Exception {
//        byte[] keyBytes = "ThisIsASecretKey".getBytes(StandardCharsets.UTF_8);
//        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
//        byte[] ivBytes = "FixedIV1234567890".getBytes(StandardCharsets.UTF_8);
//        IvParameterSpec iv = new IvParameterSpec(ivBytes);
//
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
//        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//        return "Encrypted: " + Base64.getEncoder().encodeToString(encrypted);
//    }
//
//    @GetMapping("/xss")
//    public String reflectedXss(@RequestParam String name) {
//        return "<html><body><h1>Hello " + name + "</h1></body></html>";
//    }
//
//    @GetMapping("/traverse")
//    public String pathTraversal(@RequestParam String file) {
//        try {
//            File f = new File("/var/nightcore/uploads/" + file);
//            String content = new String(
//                    java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
//            return "File content: " + content;
//        } catch (Exception e) {
//            return "Error: " + e.getMessage();
//        }
//    }
//
//    @GetMapping("/sqli")
//    public String sqlInjection(@RequestParam String username, HttpServletResponse response) {
//        try {
//            Connection conn = DriverManager.getConnection(
//                    "jdbc:h2:mem:test", "sa", DB_PASSWORD);
//            Statement stmt = conn.createStatement();
//            String query = "SELECT * FROM users WHERE username = '" + username + "'";
//            ResultSet rs = stmt.executeQuery(query);
//            StringBuilder result = new StringBuilder();
//            while (rs.next()) {
//                result.append(rs.getString("username")).append(" ");
//            }
//            rs.close();
//            stmt.close();
//            conn.close();
//            return "Users: " + result;
//        } catch (Exception e) {
//            return "DB Error: " + e.getMessage();
//        }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestParam String user, @RequestParam String pass) {
//        if ("admin".equals(user) && DB_PASSWORD.equals(pass)) {
//            return ResponseEntity.ok("Welcome admin!");
//        }
//        return ResponseEntity.status(401).body("Invalid credentials");
//    }
//}
