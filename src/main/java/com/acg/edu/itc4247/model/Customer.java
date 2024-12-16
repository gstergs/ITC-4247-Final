package com.acg.edu.itc4247.model;

import java.util.Objects;

public class Customer {
  private static int FULLNAME_MAXLEN = 32;
  private static int ZIPCODE_MAXLEN = 5;
  private static int ADDRESS_MAXLEN = 100;

  private long id;

  //This should NOT be here: we only put it for testing purposes since it reveals security info
  private long tenantId;

  private String fullname;
  private int age;
  private String address;
  private String zipcode;

  public static Customer from(long id, String fullname, int age, String address, String zipcode){
    Customer c = new Customer();
    c.id = id;
    c.fullname = fullname;
    c.age = age;
    c.address = address;
    c.zipcode = zipcode;
    return c;
  }

  /**
   * This method will throw a {@link DataValidationException} if any of the data is invalid.
   * The message of the exception contains the details.
   * We check:
   *   - the max length of string fields
   *   - the age field to be between 1 and 120
   *   - the zipcode to be numeric (although stored as string)
   */
  public void validate() {
    if (age < 1 || age > 120)
      throw new DataValidationException(String.format("Invalid age %d: expected value between 1 and 120.", age));

    if (fullname == null || fullname.isEmpty() || fullname.length() > FULLNAME_MAXLEN)
      throw new DataValidationException(String.format("Invalid fullname %s: expecting a non-empty value with max-length of %d", fullname, FULLNAME_MAXLEN));

    if (address == null || address.isEmpty() || address.length() > ADDRESS_MAXLEN)
      throw new DataValidationException(String.format("Invalid address %s: expecting a non-empty value with max-length of %d", address, ADDRESS_MAXLEN));

    if (zipcode == null || zipcode.isEmpty() || zipcode.length() > ZIPCODE_MAXLEN)
      throw new DataValidationException(String.format("Invalid zipcode %s: expecting a non-empty value with max-length of %d", zipcode, ZIPCODE_MAXLEN));

    try {
      Long.parseLong(zipcode);
    }
    catch(NumberFormatException e){
      throw new DataValidationException(String.format("Invalid zipcode %s: expecting only numbers", zipcode));
    }
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getZipcode() {
    return zipcode;
  }

  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getTenantId() {
    return tenantId;
  }

  public void setTenantId(long tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Customer customer = (Customer) o;
    return age == customer.age && Objects.equals(fullname, customer.fullname) && Objects.equals(address, customer.address) && Objects.equals(zipcode, customer.zipcode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fullname, age, address, zipcode);
  }

  @Override
  public String toString() {
    return "Customer{" +
            "fullname='" + fullname + '\'' +
            ", age=" + age +
            ", address='" + address + '\'' +
            ", zipcode='" + zipcode + '\'' +
            '}';
  }

}
