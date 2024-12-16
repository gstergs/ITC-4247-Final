This is the source code for the final assignment for ITC-4247 (ACG). 

Authors: Gavriil Stergiou, Ioannis Sinopoulos

To run the code you will need Java17+ and gradle 8+. 

To run type `grablde bootRun`: this will build the project 
and start the spring-boot application at default port `8080`.

To run the tests type `gradle test`: out of total 13 test, 11 should 
succeed and 2 will fail.

The system uses a local H2 db file named `test` (saved in `test.mv.db`)
which is recreated each time the system starts using 
the SQL file in `src/main/resources/db-schema.sql`.

