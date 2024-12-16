package com.acg.edu.itc4247;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

public class BaseTest {
  protected String baseUrl = "http://localhost:8080/api/v1.0";
  protected String apiKeyTenant1 = "0000-1111-2222-3333";
  protected String apiKeyDisabled = "0000-1111-2222-disabled";
  protected String apiKeyExpired = "0000-1111-2222-expired";

  @Autowired
  protected RestTemplate restTemplate;

  protected HttpEntity<String> getDefaultHeadersEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + apiKeyTenant1);
    return new HttpEntity<>(headers);
  }

  protected <T> HttpEntity<T> getHeadersWithEntity(T t){
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + apiKeyTenant1);
    return new HttpEntity<>(t, headers);
  }

}
