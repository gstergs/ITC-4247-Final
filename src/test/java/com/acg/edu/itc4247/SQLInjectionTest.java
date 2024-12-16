package com.acg.edu.itc4247;

import com.acg.edu.itc4247.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URLEncoder;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SQLInjectionTest extends BaseTest {


  /**
   * Attempt an SQL injection attack.
   */
  @Test
  public void testSQLInjection() {
    String sqlInjectionUrl = baseUrl + "/customers/" + URLEncoder.encode("1 OR 1=1 --");
    try {
       restTemplate.exchange(sqlInjectionUrl, HttpMethod.GET, getDefaultHeadersEntity(), Customer.class);
       assertFalse(true, "vulnerability found: a bad requested was expected");
    }
    catch(Exception e){
      assertTrue(e instanceof HttpClientErrorException.BadRequest, "expected BadRequest response response");

      //check the actual body to see if the "error" message starts with "Invalid path variable type"
      Map m = ((HttpClientErrorException.BadRequest) e).getResponseBodyAs(Map.class);
      assertTrue(m.get("error").toString().startsWith("Invalid path variable type"), "unexepcted error message");
    }
  }

  /**
   * This test calls the /customers/vulnerable endpoint which is purposefully vulnerable to SQL injection attack
   * THIS TEST WILL FAIL BY DESIGN
   */
  @Test
  void testSqlInjectionVulnerable_WILL_FAIL() {
    String sqlInjectionUrl = baseUrl + "/customers/vulnerable/1 OR 1=1 --";
    try {
      ResponseEntity<String> response = restTemplate.exchange(sqlInjectionUrl, HttpMethod.GET, getDefaultHeadersEntity(), String.class);
      assertFalse(true, "vulnerability found: a bad requested was expected but actual data were returned: " + response.getBody());
    }
    catch(Exception e){
      assertTrue(e instanceof HttpClientErrorException.BadRequest, "expected BadRequest response response");

      //check the actual body to see if the "error" message starts with "Invalid path variable type"
      Map m = ((HttpClientErrorException.BadRequest) e).getResponseBodyAs(Map.class);
      assertTrue(m.get("error").toString().startsWith("Invalid path variable type"), "unexepcted error message");
    }
  }

}
