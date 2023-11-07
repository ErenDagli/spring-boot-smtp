package com.globalpbx.mailserver.repository.jdbc;

import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.repository.MailServerVersionRepository;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcMailServerVersionRepository implements MailServerVersionRepository {
    @Override
    public List<String> getAllVersion(Connection connection) {

        List<String> versionList = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM versions")) {
            while (resultSet.next()) {
                float versionNumber = resultSet.getFloat("version_number");
                versionList.add(String.valueOf(versionNumber));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle any potential exceptions properly
        }
        return versionList;
    }

    @Override
    public String saveVersion(Connection connection, MailInfoDto mailInfoDto) throws SQLException {
        String insertQueryToVersionTable = "INSERT INTO versions (version_number) VALUES (?)";
        PreparedStatement preparedStatementVersionTable = connection.prepareStatement(insertQueryToVersionTable);

        preparedStatementVersionTable.setString(1, String.valueOf(mailInfoDto.getVersionNumber()));

        preparedStatementVersionTable.executeUpdate();
        return String.valueOf(mailInfoDto.getVersionNumber());
    }

    @Override
    public String findLastVersion(Connection connection) throws SQLException {
        String selectLastRowQuery = "SELECT * FROM versions ORDER BY rowid DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectLastRowQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("version_number");
            }
        }
        return String.valueOf(-1);
    }


}
