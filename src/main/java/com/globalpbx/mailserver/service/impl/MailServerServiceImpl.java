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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

@Service
public class MailServerServiceImpl implements MailServerService {

    @Value("${sqlite.database.url}")
    private String databaseUrl;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    private final int numThreads = 5;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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


    public synchronized void sendMailWithRedis(MailInfoDto mailInfoDto, Connection connection, DatabaseMetaData metaData) {
        System.out.println("entry is ->" + mailInfoDto);

        try {


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

//            List<MailInfoDto> allMails = mailServerRepository.getAllMails(connection);
//            allMails.forEach(System.out::println);
//
//            List<String> allVersions = mailServerVersionRepository.getAllVersion(connection);
//            allVersions.forEach(System.out::println);

        } catch (SQLException e) {
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
    public String sendMail(List<MailInfoDto> mailInfoDtoList) {
        for (MailInfoDto mailInfoDto : mailInfoDtoList) {
            executor.execute(() -> {
                try {
                    addToQueue(new ObjectMapper().writeValueAsString(mailInfoDto));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return "Email sent successfully!";
    }

    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void mailSendAndTransferDatabase() throws JsonProcessingException, ClassNotFoundException, SQLException {
        while (true) {
            String mail = processQueue();
            Connection connection = null;
            if (mail == null) {
                System.out.println("queue is empty");
                if (connection != null) {
                    connection.close();
                }
                break;
            }
            MailInfoDto storedMailInfo = new ObjectMapper().readValue(mail, MailInfoDto.class);

            // SQLite JDBC driver has been created
            Class.forName(databaseUrl);

            // SQLite db connection has been created
            connection = DriverManager.getConnection(storedMailInfo.getPath());

            System.out.println("You have successfully connected to the SQLite database.");

            DatabaseMetaData metaData = connection.getMetaData();
            Connection finalConnection = connection;


            createTable(connection, versionTable);
            createTable(connection, mailsTable);
            executor.execute(() ->
                    sendMailWithRedis(storedMailInfo, finalConnection, metaData));


            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                executor.shutdown();
            }));
        }
    }


    private void createTable(Connection connection, String tableName) {
        switch (tableName) {
            case mailsTable:
                mailServerRepository.createMailsTable(connection);
                break;
            case versionTable:
                mailServerVersionRepository.createVersionTable(connection);
                break;
            default:
                // Handle the case where the table name is not recognized
                System.out.println("Handle the case where the table name is not recognized");
                throw new IllegalArgumentException("Unsupported table name: " + tableName);
        }
    }
}
