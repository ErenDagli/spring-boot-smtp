package com.globalpbx.mailserver.repository.jdbc;

import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.repository.MailServerRepository;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcMailRepository implements MailServerRepository {
    @Override
    public List<MailInfoDto> getAllMails(Connection connection) {
        List<MailInfoDto> mailList = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM mails")) {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String path = resultSet.getString("path");
                float versionNumber = resultSet.getFloat("version_number");
                String recipient = resultSet.getString("recipient");
                String subject = resultSet.getString("subject");
                String body = resultSet.getString("body");

                MailInfoDto mail = new MailInfoDto(id, path, versionNumber, recipient, subject, body);
                mailList.add(mail);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle any potential exceptions properly
        }

        return mailList;
    }

    @Override
    public MailInfoDto saveMail(Connection connection, MailInfoDto mailInfoDto) throws SQLException {

        String insertQuery = "INSERT INTO mails (path, version_number, recipient, subject, body)\n" +
                "                VALUES (?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        preparedStatement.setString(1, mailInfoDto.getPath());
        preparedStatement.setFloat(2, mailInfoDto.getVersionNumber());
        preparedStatement.setString(3, mailInfoDto.getRecipient());
        preparedStatement.setString(4, mailInfoDto.getSubject());
        preparedStatement.setString(5, mailInfoDto.getBody());

        preparedStatement.executeUpdate();
        return mailInfoDto;
    }

}
