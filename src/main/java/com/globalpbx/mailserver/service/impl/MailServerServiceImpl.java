package com.globalpbx.mailserver.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalpbx.mailserver.constant.TableNameConstants;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.repository.MailServerRepository;
import com.globalpbx.mailserver.repository.MailServerVersionRepository;
import com.globalpbx.mailserver.service.MailServerService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;
import java.util.Properties;

@Service
public class MailServerServiceImpl implements MailServerService {

    @Value("${sqlite.database.url}")
    private String databaseUrl;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    private final String mailsTable = TableNameConstants.MAILS;

    private final String versionTable = TableNameConstants.VERSIONS;

    private MailServerRepository mailServerRepository;
    private MailServerVersionRepository mailServerVersionRepository;

    private final RedisTemplate<String, String> redisTemplate;


    public void addToQueue(String data) {
        redisTemplate.opsForList().rightPush(mailsTable, data);
    }

    public String processQueue() {
        return redisTemplate.opsForList().leftPop(mailsTable);
    }

    @Autowired
    public MailServerServiceImpl(MailServerRepository mailServerRepository, MailServerVersionRepository mailServerVersionRepository, RedisTemplate<String, String> redisTemplate) {
        this.mailServerRepository = mailServerRepository;
        this.mailServerVersionRepository = mailServerVersionRepository;
        this.redisTemplate = redisTemplate;
    }


    public void sendMailWithRedis(MailInfoDto mailInfoDto) {
        System.out.println("entry is ->" + mailInfoDto);
        Connection connection = null;

        try {
            // SQLite JDBC driver has been created
            Class.forName(databaseUrl);

            // SQLite db connection has been created
            connection = DriverManager.getConnection(mailInfoDto.getPath());

            System.out.println("You have successfully connected to the SQLite database.");

            DatabaseMetaData metaData = connection.getMetaData();

            createTableIfNotExists(connection, metaData, versionTable);
            createTableIfNotExists(connection, metaData, mailsTable);

            String versionNumber = mailServerVersionRepository.findLastVersion(connection);
            if (Float.parseFloat(versionNumber) > -1) {
                if (Float.parseFloat(versionNumber) < mailInfoDto.getVersionNumber()) {
                    // new column added
                    String alterTableQuery = "ALTER TABLE " + mailsTable + " ADD COLUMN new_column_name varchar(255)";
                    try (Statement statement = connection.createStatement()) {
                        statement.executeUpdate(alterTableQuery);
                        System.out.println("The new column has been added successfully.");
                    }
                }
                System.out.println("Last Version Number: " + versionNumber);
            }


            String savedVersion = mailServerVersionRepository.saveVersion(connection, mailInfoDto);
            System.out.println("New version added to version table. New version : " + savedVersion);

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailInfoDto.getRecipient()));
                message.setSubject(mailInfoDto.getSubject());
                message.setText(mailInfoDto.getBody());

                Transport.send(message);

                MailInfoDto savedMail = mailServerRepository.saveMail(connection, mailInfoDto);
                System.out.println(savedMail);
                System.out.println("Email sent successfully.");

            } catch (MessagingException | SQLException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }

            List<MailInfoDto> allMails = mailServerRepository.getAllMails(connection);
            allMails.forEach(System.out::println);

            List<String> allVersions = mailServerVersionRepository.getAllVersion(connection);
            allVersions.forEach(System.out::println);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String sendMail(List<MailInfoDto> mailInfoDtoList) throws JsonProcessingException {
        for (MailInfoDto mailInfoDto : mailInfoDtoList) {
            addToQueue(new ObjectMapper().writeValueAsString(mailInfoDto));
        }
        return "Email sent successfully!";
    }
    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void mailSendAndTransferDatabase() throws JsonProcessingException {
        while (true) {
            String mail = processQueue();
            if (mail == null) {
                System.out.println("queue is empty");
                break;
            }
            MailInfoDto storedMailInfo = new ObjectMapper().readValue(mail, MailInfoDto.class);
            sendMailWithRedis(storedMailInfo);
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (var resultSet = metaData.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private void createTable(Connection connection, String tableName) throws SQLException {
        String createTableSQL;
        switch (tableName) {
            case mailsTable:
                createTableSQL = "CREATE TABLE " + tableName + "(\n" +
                        "    id BIGINT PRIMARY KEY,\n" +
                        "    path VARCHAR(255),\n" +
                        "    version_number FLOAT,\n" +
                        "    recipient  VARCHAR(255),\n" +
                        "    subject VARCHAR(255),\n" +
                        "    body TEXT\n" +
                        ");";
                break;
            case versionTable:
                createTableSQL = "CREATE TABLE " + tableName + " (id INTEGER PRIMARY KEY, version_number FLOAT)";
                break;
            default:
                // Handle the case where the table name is not recognized
                System.out.println("Handle the case where the table name is not recognized");
                throw new IllegalArgumentException("Unsupported table name: " + tableName);
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        }
    }

    private void createTableIfNotExists(Connection connection, DatabaseMetaData metaData, String tableName) throws SQLException {
        if (!tableExists(metaData, tableName)) {
            createTable(connection, tableName);
            System.out.println(tableName + " table has been created.");
        } else {
            System.out.println(tableName + " table exists.");
        }
    }
}
