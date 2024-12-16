package com.acg.edu.itc4247;

import com.acg.edu.itc4247.model.Customer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In this set of tests, we attempt to have access to data that belong to another tenant.
 * This means that all requests here should pass authentication, yet we see whether there is
 * a vulnerability in authorization and access
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataBreachTest extends BaseTest {

  //this is a list of known customer IDs belonging to tenant_id=1 that is associated with the api-key we use for these tests.
  Set<Long> customerIDs = Set.of(1L, 2L, 3L);

  /**
   * This test requests all customers from the API and checks that all their tenant_id = 1
   * Normally the Customer object should not contain the tenant_id because this is unnecessary info reveal, but
   * we do it JUST for this test.
   */
  @Test
  @Order(-1)
  void testGetAllCustomersBelongToOwnTenant(){
    String url = baseUrl + "/customers";
    ResponseEntity<List<Customer>> response = restTemplate.exchange(url, HttpMethod.GET, getDefaultHeadersEntity(), new ParameterizedTypeReference<List<Customer>>() {});

    Optional<Customer> anyCustomer = response.getBody().stream()
            .filter(c -> c.getTenantId() != 1).findAny();
    assertTrue(anyCustomer.isEmpty(), "API returned customer belonging to another tenant");
  }

  /**
   * This test attempts to get a customer that belongs to another tenant.
   * During the test we try to get customers from id=0 to id=10.
   *
   * This test makes sense since we run this on a test-controlled system where we know beforehand the actual data in the db.
   * We know that the customers that belong to tenant_id=1 are the ones defined in the customreIDs Set above.
   * We will try to get customers with different IDs.
   * Remember the Customer does not contain the tenant_id for security reasons, so we need to know beforehand
   * which customer belongs to which tenant
   *
   */
  @Test
  void testTryGetCustomersBelongingToAntherTenant(){
    for(int i=0; i<=10; i++) {
      try {
        String url = baseUrl + "/customers/" + i;
        ResponseEntity<Customer> response = restTemplate.exchange(url, HttpMethod.GET, getDefaultHeadersEntity(), Customer.class);
      }
      catch(HttpClientErrorException.NotFound ex){
        assertTrue(i<1 || i>3, "vulnerability: was able to get access to a customer belonging to a different tenant");
      }
    }
  }

  /**
   * This test tries to delete a customer with id=4 that belongs to another tenant.
   * The API should return NOTFOUND to indicate that the customer does not exists: in reality the
   * customer with this ID exists but belongs to another tenant. A secure system should not reveal this
   * information.
   */
  @Test
  void testToDeleteCustomerBelongingToAntherTenant(){
    try {
      String url = baseUrl + "/customers/4"; //customer with id=4 belongs to tenant_id=2
      ResponseEntity<Customer> response = restTemplate.exchange(url, HttpMethod.DELETE, getDefaultHeadersEntity(), Customer.class);
    }
    catch(Exception ex){
      assertTrue(ex instanceof HttpClientErrorException.NotFound);
    }
  }


}
