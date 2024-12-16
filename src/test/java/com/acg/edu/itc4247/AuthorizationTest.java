package com.acg.edu.itc4247;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthorizationTest extends BaseTest {

  /**
   * Test an endpoint without an Authorization header.
   * We expect a 401-Unauthorized response
   */
  @Test
  public void testNoAuthorizationHeader(){
    String url = baseUrl + "/customers";
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);
    try {
      restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      assertTrue(false, "vulnerability: request should have been rejected as unauthorized");
    }
    catch(Exception e){
      assertTrue(e instanceof HttpClientErrorException.Unauthorized, "vulnerability: request should have been rejected as unauthorized");
    }
  }

  /**
   * Test an endpoint with an invalid api key
   * We expect a 401-Unauthorized response
   */
  @Test
  public void testInvalidApiKey(){
    String url = baseUrl + "/customers/1";
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer this-is-an-invalid-api-key");
    HttpEntity<String> entity = new HttpEntity<>(headers);
    try {
      restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      assertTrue(false, "vulnerability: request should have been rejected because of invalid key");
    }
    catch(Exception e){
      assertTrue(e instanceof HttpClientErrorException.Unauthorized, "vulnerability: request should have been rejected because of invalid key");
    }
  }

  /**
   * Test an endpoint with a disabled api key.
   * We expect a 401-Unauthorized response
   */
  @Test void testDisabledApiKey(){
    String url = baseUrl + "/customers/1";
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + apiKeyDisabled);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    try {
      restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      assertTrue(false, "vulnerability: request should have been rejected because of disabled key");
    }
    catch(Exception e){
      assertTrue(e instanceof HttpClientErrorException.Unauthorized, "vulnerability: request should have been rejected because of disabled key");
    }
  }


  /**
   * Test an endpoint with an expired api key.
   * We expect a 401-Unauthorized response
   */
  @Test void testExpiredApiKey(){
    String url = baseUrl + "/customers/1";
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + apiKeyExpired);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    try {
      restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      assertTrue(false, "vulnerability: request should have been rejected because of expired key");
    }
    catch(Exception e){
      assertTrue(e instanceof HttpClientErrorException.Unauthorized, "vulnerability: request should have been rejected because of expired key");
    }
  }

  /**
   * Tests whether valid authorization will actually work, as expected. This is a vanilla 'all-good' test.
   */
  @Test void testValidApiKey(){
    String url = baseUrl + "/customers/1";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getDefaultHeadersEntity(), String.class);
    assertEquals(200, response.getStatusCode().value(), "request failed with valid api key");
  }

}
