package com.acg.edu.itc4247.controllers;

import com.acg.edu.itc4247.DbUtil;
import com.acg.edu.itc4247.model.Customer;
import com.acg.edu.itc4247.model.DataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/customers")
public class CustomersController {
  private static Logger logger = LoggerFactory.getLogger(CustomersController.class);


  /**
   * @param tenant_id the tenant associated with the api key making this request.
   * @return a list of all customers belonging to this tenant. If no customers exist 404-NotFound is returned
   * @throws SQLException in case of a database error
   */
  @GetMapping("")
  public ResponseEntity<List<Customer>> getCustomers(@RequestAttribute(name="tenant_id", required=true) long tenant_id)
          throws SQLException {
    logger.info("GET /customers, tenant_id={}", tenant_id);
    String sql = "select id, fullname, age, address, zipcode from customers where tenant_id=?";
    List<Customer> customers = DbUtil.getJdbcTemplate()
            .query(sql, new Object[]{tenant_id}, (rs, rowNum) -> {
              Customer customer = fromRs(rs);
              customer.setTenantId(tenant_id); //this is ONLY for testing, normally we shouldn't be adding this info for security reasons.
              return customer;
            } );
    return customers.size() > 0 ? ResponseEntity.ok(customers) : ResponseEntity.notFound().build();
  }

  /**
   * @param tenant_id the tenant associated with the api key making this request.
   * @return the customer with the given id or 404-NotFound
   * @throws SQLException in case of a database error
   */
  @GetMapping("/{id}")
  public ResponseEntity<Customer> getCustomer(@PathVariable long id,
                                                @RequestAttribute(name="tenant_id", required=true) long tenant_id){
    logger.info("GET /customers/{}, tenant_id={}", id, tenant_id);
    String sql = "select id, fullname, age, address, zipcode from customers where id=? and tenant_id=?";
    List<Customer> customers = DbUtil.getJdbcTemplate()
            .query(sql, new Object[]{id, tenant_id}, (rs, rowNum) -> fromRs(rs));
    return customers.size() == 1 ? ResponseEntity.ok(customers.get(0)) : ResponseEntity.notFound().build();
  }

  /**
   * This method is vulnerable to SQL injection.
   * @param tenant_id the tenant associated with the api key making this request.
   * @return the customer with the given id or 404-NotFound
   * @throws SQLException in case of a database error
   */
  @GetMapping("/vulnerable/{id}")
  public ResponseEntity<List<Customer>> getCustomerVulnerable(@PathVariable String id,
                                                    @RequestAttribute(name="tenant_id", required=true) long tenant_id){
    logger.info("GET /customers/vulnerable/{}, tenant_id={}", id, tenant_id);
    String sql = "select id, fullname, age, address, zipcode from customers where tenant_id = " + tenant_id + " and id=" + id;
    List<Customer> customers = DbUtil.getJdbcTemplate()
            .query(sql, (rs, rowNum) -> fromRs(rs));
    return  ResponseEntity.ok(customers);
  }

  /**
   * Updates an existing customer. All customer data are updated after being validated.
   * @param tenant_id the tenant associated with the api key making this request.
   * @return the id of the customer being updated in json (e.g. <code>{"id": 1}</code>)
   * @throws SQLException in case of a database error
   */
  @PutMapping("/{id}")
  public ResponseEntity<Map> updateCustomer(@PathVariable Long id, @RequestBody Customer customer,
                                            @RequestAttribute(name="tenant_id", required=true) long tenant_id) {
    logger.info("PUT /customers/{}, tenant_id={}", id, tenant_id);
    validateCustomer(customer);
    return updateCustomerInternal(id, customer, tenant_id);
  }


  /**
   * This method is vulnerable since it does no validation on received customer data
   * Updates an existing customer.
   *
   * @param tenant_id the tenant associated with the api key making this request.
   * @return the id of the customer being updated in json (e.g. <code>{"id": 1}</code>)
   * @throws SQLException in case of a database error
   */
  @PutMapping("/vulnerable/{id}")
  public ResponseEntity<Map> updateCustomerVulnerable(@PathVariable Long id, @RequestBody Customer customer,
                                                      @RequestAttribute(name="tenant_id", required=true) long tenant_id){
    logger.info("PUT /customers/{}, tenant_id={}", id, tenant_id);
    return updateCustomerInternal(id, customer, tenant_id);
  }

  /**
   * Update a customer: this is called from the two methods above, one of which is purposefully vulnerable.
   * @return
   */
  private ResponseEntity<Map> updateCustomerInternal(Long id, Customer customer, long tenant_id){
    String sql = "update customers set fullname=?, age=?, address=?, zipcode=? where id=? and tenant_id=?";
    int count = DbUtil.getJdbcTemplate().update(sql, ps -> {
      ps.setString(1, customer.getFullname());
      ps.setInt(2, customer.getAge());
      ps.setString(3, customer.getAddress());
      ps.setString(4, customer.getZipcode());
      ps.setLong(5, id);
      ps.setLong(6, tenant_id);
    });
    return count == 1 ? ResponseEntity.ok(Map.of("id", id)) : ResponseEntity.notFound().build();
  }

  /**
   * Adds a new customers. All customer date are validate. The customer is added under the proper tenant.
   * @param tenant_id the tenant associated with the api key making this request.
   * @return the id of the customer being added in json (e.g. <code>{"id": 1}</code>)
   * @throws SQLException in case of a database error
   */
  @PostMapping
  public ResponseEntity<Map> addCustomer(@RequestBody Customer customer,
                                         @RequestAttribute(name="tenant_id", required=true) long tenant_id){
    logger.info("POST /customers, tenant_id={}", tenant_id);
    validateCustomer(customer);
    String sql = "insert into customers(fullname, age, address, zipcode, tenant_id) values(?,?,?,?,?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    DbUtil.getJdbcTemplate().update(con -> {
      PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
      ps.setString(1, customer.getFullname());
      ps.setInt(2, customer.getAge());
      ps.setString(3, customer.getAddress());
      ps.setString(4, customer.getZipcode());
      ps.setLong(5, tenant_id);
      return ps;
    }, keyHolder);
    return ResponseEntity.ok(Map.of("id", keyHolder.getKey()));
  }

  /**
   * Deletes the customer with the given id. The customer should be under the proper tenant else 404-NotFound is returned.
   * @param tenant_id the tenant associated with the api key making this request.
   * @return the id of the customer being deleted in json (e.g. <code>{"id": 1}</code>)
   * @throws SQLException in case of a database error
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Map> deleteCustomer(@PathVariable Long id,
                                            @RequestAttribute(name="tenant_id", required=true) long tenant_id){
    logger.info("DELTE /customers/{}, tenant_id={}", id, tenant_id);
    String sql = "delete from customers where id=? and tenant_id=?";
    int count = DbUtil.getJdbcTemplate().update(sql, ps -> {
      ps.setLong(1, id);
      ps.setLong(2, tenant_id);
    });
    return count == 1 ? ResponseEntity.ok(Map.of("id", id)) :
            ResponseEntity.notFound().build();
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map> handleTypeMismatch(MethodArgumentTypeMismatchException ex){
    return ResponseEntity.status(400).body(Map.of("error", "Invalid path variable type: " + ex.getParameter().getParameterName()));
  }


  /**
   * Utility method that extract a single row from a result set into a Customer object.
   * We expect that the query contains the 4 columns fullname, age, address, zipcode
   * @param rs the ResultSet pointing to the current row
   * @return a new Customer objectg
   * @throws SQLException
   */
  private Customer fromRs(ResultSet rs) throws SQLException{
    return Customer.from(rs.getLong("id"), rs.getString("fullname"), rs.getInt("age"),
            rs.getString("address"), rs.getString("zipcode"));
  }

  /**
   * Helper method that is called from endpoints that insert/update customer data.
   * Validates the customer and throws an exception with the proper message if something is wrong.
   * @param c
   */
  private void validateCustomer(Customer c){
    try {
      c.validate();
    }
    catch(DataValidationException e){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }
}
