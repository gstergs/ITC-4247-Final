package com.acg.edu.itc4247;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.*;

public class DbUtil {


  public static JdbcTemplate getJdbcTemplate(){
    return new JdbcTemplate(getDataSource());
  }

  public static DataSource getDataSource(){
    return DataSourceBuilder.create()
            .url("jdbc:h2:./test")
            .username("sa")
            .password("")
            .driverClassName("org.h2.Driver")
            .build();
  }

  public static Connection getConnection() throws SQLException {
    return getDataSource().getConnection();
  }

  public static void initDb() throws URISyntaxException, SQLException, IOException {
    //delete existing file
    (new File("test.mv.db")).delete();

    File schemaFile = new File(DbUtil.class.getResource("/db-schema.sql").toURI());
    String sqlSchema = Files.readString(schemaFile.toPath());

    //create db and load with test data
    try(Connection con = getConnection(); ) {
      for(String sql: sqlSchema.split(";")) {
        try (Statement stmt = con.createStatement()) {
          stmt.execute(sql);
        }
      }
    }
  }

  public static String generateHtmlTable(Connection conn, String tableName) throws  SQLException {
    StringBuilder html = new StringBuilder();

    // Start the HTML table with border
    html.append("<table border='1'>\n");

    // Retrieve table metadata and data
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

      // Add table headers (column names)
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();
      html.append("  <tr>\n");
      for (int i = 1; i <= columnCount; i++) {
        html.append("    <th>").append(metaData.getColumnName(i)).append("</th>\n");
      }
      html.append("  </tr>\n");

      // Add table rows (data)
      while (rs.next()) {
        html.append("  <tr>\n");
        for (int i = 1; i <= columnCount; i++) {
          html.append("    <td>").append(rs.getString(i)).append("</td>\n");
        }
        html.append("  </tr>\n");
      }
    }

    // Close the HTML table
    html.append("</table>\n");

    return html.toString();
  }

}
