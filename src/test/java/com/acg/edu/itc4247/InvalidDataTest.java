package com.acg.edu.itc4247;

import com.acg.edu.itc4247.model.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class contains tests where we try to post/update invalid customer data.
 * We expect the REST endpoint to make a set of data validations and trust nothing
 * when accepting a payload.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvalidDataTest extends BaseTest {

  List<Customer> customersWithInvalidData = List.of(
          //age issues
          Customer.from(0, "John Adams", 0, "Dionysou 2, Chalandri", "12345"), //invalid age
          Customer.from(0, "John Adams", 0, "Dionysou 2, Chalandri", "12345"), //invalid age

          //fullname issues
          Customer.from(0, "A customer with a very long name, more than 32 characters", 20, "Dionysou 2, Chalandri", "12345"), // very long name
          Customer.from(0, "", 20, "Dionysou 2, Chalandri", "12345"), //no name
          Customer.from(0, null, 20, "Dionysou 2, Chalandri", "12345"), //null name

          //address issues
          Customer.from(0, "John Adams", 20, "This is a very very very long address " +
                  "(more than 100 chars that is the maximum accepted size for this field)", "12345"), //very long address
          Customer.from(0, "John Adams", 20, "", "12345"), //no address
          Customer.from(0, "John Adams", 20, null, "12345"), //null address

          //zipcode issues
          Customer.from(0, "John Adams", 20, "Dionysou 2, Chalandri", "123456"), //long zipcode
          Customer.from(0, "John Adams", 20, "Dionysou 2, Chalandri", ""), //no zipcode
          Customer.from(0, "John Adams", 20, "Dionysou 2, Chalandri", null), //null zipcode
          Customer.from(0, "John Adams", 20, "Dionysou 2, Chalandri", "abcde") //not-numeric zip code
          );

  /**
   * In this test we try to insert/add the list of customers above: all are invalid and we should get a BadRequest
   */
  @Test
  public void testAddCustomerWithInvalidData(){
    for(Customer customer: customersWithInvalidData) {
      try {
        String url = baseUrl + "/customers";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, getHeadersWithEntity(customer), String.class);
        Assertions.fail("vulnerability: customer with invalid data seems to have been accepted"); //this should never happen
      } catch (Exception ex) {
        assertTrue(ex instanceof HttpClientErrorException.BadRequest, "un-expected error from endpoint");
      }
    }
  }

  /**
   * In this test we try to insert/add the list of customers above: all are invalid and we should get a BadRequest
   */
  @Test
  public void testUpdateCustomerWithInvalidData(){
    for(Customer customer: customersWithInvalidData) {
      try {
        String url = baseUrl + "/customers/1"; //customer with id=1 belongs to tenant_id=1, so we are OK here
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, getHeadersWithEntity(customer), String.class);
        assertTrue(false, "vulnerability: customer with invalid data seems to have been accepted"); //this should never happen
      } catch (Exception ex) {
        assertTrue(ex instanceof HttpClientErrorException.BadRequest);
      }
    }
  }

  /**
   * THIS TEST WILL FAIL.
   * This is a test that calls a purposefully vulnerable endpoint to update a customer with invalid data.
   */
  @Test
  public void testUpdateCustomerWithInvalidData_WILL_FAIL(){
    Customer customer = Customer.from(0, "", 0, "", "");
    String url = baseUrl + "/customers/vulnerable/1"; //customer with id=1 belongs to tenant_id=1, so we are OK here
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, getHeadersWithEntity(customer), String.class);
    Assertions.fail("vulnerability: customer with invalid data seems to have been accepted"); //this should never happen
  }

}
