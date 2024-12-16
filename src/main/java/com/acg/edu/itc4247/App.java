package com.acg.edu.itc4247;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.sql.*;

@SpringBootApplication
@RestController
public class App {

  public static void main(String[] args) throws SQLException{
    SpringApplication.run(App.class, args);
  }

  @PostConstruct
  public void init() throws Exception{
    DbUtil.initDb();
  }

}
