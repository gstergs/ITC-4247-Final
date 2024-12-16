package com.acg.edu.itc4247.controllers;

import com.acg.edu.itc4247.DbUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A controller that just renders in html the db tables: used for debugging, not part of the API
 * note: the path is NOT below /api/v1.0
 */
@RestController
@RequestMapping("/tables")
public class TablesController {

  @GetMapping("")
  public String getTables() throws SQLException {
    StringBuilder sb = new StringBuilder();
    String[] tables = new String[]{"tenants", "apikeys", "customers"};
    try( Connection con = DbUtil.getConnection() ){
      for(String table: tables){
        sb.append(String.format("<h3>Table: %s</h3>", table));
        sb.append( DbUtil.generateHtmlTable(con, table) );
        sb.append("<hr>");
      }
      return "<html><body>" + sb.toString() + "</body></html>";
    }
  }
}
