package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {
    private String compensationUrl;
    private String compensationIdUrl;
    private String employeeUrl;
    private Employee createdEmployee;
    private Compensation testCompensation;

    @Autowired
    CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() throws ParseException{
        employeeUrl = "http://localhost:" + port + "/employee";
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        testCompensation = new Compensation();
        testCompensation.setEmployee(createdEmployee);
        testCompensation.setSalary(10000.121);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        Date date = format.parse("12-7-2019");
        testCompensation.setEffectiveDate(date);
    }

    @Test
    public void testCreateReadUpdate() throws ParseException {
        //creation check
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();
        assertCompensationEquivalence(testCompensation, createdCompensation);

        // Read checks
        Compensation readCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class, createdEmployee.getEmployeeId()).getBody();
        assertCompensationEquivalence(createdCompensation, readCompensation);
    }

    @Test
    public void testCreateNullEmployee() {
        testCompensation.setEmployee(null);
        boolean hasFailed = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getStatusCode().is5xxServerError();
        assertTrue(hasFailed);
    }

    @Test
    public void testCreateEmployeeDoesnotExist() {
        createdEmployee.setEmployeeId(UUID.randomUUID().toString());
        testCompensation.setEmployee(createdEmployee);
        boolean hasFailed = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getStatusCode().is5xxServerError();
        assertTrue(hasFailed);
    }

    @Test
    public void testCreateCompensationSalaryIsNull() {
        testCompensation.setSalary(0);
        boolean hasFailed = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getStatusCode().is5xxServerError();
        assertTrue(hasFailed);
    }

    @Test
    public void testCreateCompensationEffectiveDateIsNull() {
        testCompensation.setEffectiveDate(null);
        boolean hasFailed = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getStatusCode().is5xxServerError();
        assertTrue(hasFailed);
    }

    @Test
    public void testCreateCompensationInvalidEffectiveDateFormat() throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date date = format.parse("13/7/2019");
        testCompensation.setEffectiveDate(date);
        boolean hasFailed = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getStatusCode().is4xxClientError();
        assertFalse(hasFailed);
    }

    @Test
    public void testGetCompensationEmployeeDoesnotExist() {
        createdEmployee.setEmployeeId(UUID.randomUUID().toString());
        testCompensation.setEmployee(createdEmployee);
        boolean hasFailed = restTemplate.getForEntity(compensationIdUrl, Compensation.class, createdEmployee.getEmployeeId()).getStatusCode().is5xxServerError();
        assertTrue(hasFailed);
    }

    @Test
    public void testGetCompensationWithNoCompensation() {
        boolean hasFailed = restTemplate.getForEntity(compensationIdUrl, Compensation.class, createdEmployee.getEmployeeId()).getStatusCode().is5xxServerError();
        assertTrue(hasFailed);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
        assertEquals(expected.getSalary(), actual.getSalary(), 0.1);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertNotNull(expected.getEmployeeId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
