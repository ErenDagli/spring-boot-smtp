package com.globalpbx.mailserver.util;

public class FormatterUtil {

    public static String generateHTMLTable(String[] headers, String[][] data) {
        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<html>\n" +
                "<head>\n" +
                "    <title>Basit HTML Tablo</title>\n" +
                "</head>\n" +
                "<body>");
        htmlTable.append("\n<table border='1'>\n<tr>");

        for (String header : headers) {
            htmlTable.append("<th>").append(header).append("</th>");
        }
        htmlTable.append("</tr>");

        for (String[] row : data) {
            htmlTable.append("<tr>");
            for (String cell : row) {
                htmlTable.append("<td>").append(cell).append("</td>");
            }
            htmlTable.append("</tr>");
        }

        htmlTable.append("</table>");
        htmlTable.append("\n" +
                "</body>\n" +
                "</html>");
        return htmlTable.toString();
    }
}
