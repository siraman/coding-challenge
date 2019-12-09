package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String numberOfReportsUrl;
    private ReportingStructure testReportingStructure;
    private Employee createdEmployee5;


    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        numberOfReportsUrl = "http://localhost:" + port + "/employee/{id}/numberOfReports";

        Employee testEmployee = new Employee();
        testEmployee.setFirstName("Trevor");
        testEmployee.setLastName("Hancock");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        Employee createdEmployee0 = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        testEmployee = new Employee();
        testEmployee.setFirstName("Pete");
        testEmployee.setLastName("Best");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        testEmployee.setDirectReports(Arrays.asList(createdEmployee0));
        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        testEmployee = new Employee();
        testEmployee.setFirstName("George");
        testEmployee.setLastName("Harrison");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        Employee createdEmployee2 = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        testEmployee = new Employee();
        testEmployee.setFirstName("Ringo");
        testEmployee.setLastName("Starr");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        testEmployee.setDirectReports(Arrays.asList(createdEmployee1, createdEmployee2));
        Employee createdEmployee3 = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        testEmployee = new Employee();
        testEmployee.setFirstName("Paul");
        testEmployee.setLastName("McCartney");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        Employee createdEmployee4 = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Lennon");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        testEmployee.setDirectReports(Arrays.asList(createdEmployee3, createdEmployee4));
        createdEmployee5 = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        testReportingStructure = new ReportingStructure();
        testEmployee = new Employee();
        testEmployee.setEmployeeId(createdEmployee5.getEmployeeId());
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Lennon");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        testEmployee.setDirectReports(Arrays.asList(createdEmployee0, createdEmployee1, createdEmployee2, createdEmployee3, createdEmployee4));
        testReportingStructure.setEmployee(testEmployee);
        testReportingStructure.setNumberOfReports(5);
    }

    @Test
    public void testCreateReadUpdate() {
        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, createdEmployee5, Employee.class).getBody();
        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee5, createdEmployee);

        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);

        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testGetNumberOfReportsCount() {
        ReportingStructure reportingStructure = restTemplate.getForEntity(numberOfReportsUrl, ReportingStructure.class, createdEmployee5.getEmployeeId()).getBody();
        assertEquals(reportingStructure.getNumberOfReports(), testReportingStructure.getNumberOfReports());
    }

    @Test
    public void testGetReportingStructure() {
        ReportingStructure actualReportingStructure = restTemplate.getForEntity(numberOfReportsUrl, ReportingStructure.class, createdEmployee5.getEmployeeId()).getBody();
        assertReportingStructure(testReportingStructure,actualReportingStructure);
    }

    @Test
    public void testGetReportingStructureWhenEmployeeDoesnotExists() {
        createdEmployee5.setEmployeeId(UUID.randomUUID().toString());
        boolean isNotValid = restTemplate.getForEntity(numberOfReportsUrl, ReportingStructure.class, createdEmployee5.getEmployeeId()).getStatusCode().is5xxServerError();
        assertTrue(isNotValid);
    }

    @Test
    public void testGetEmployeeDoesnotExist() {
        createdEmployee5.setEmployeeId(UUID.randomUUID().toString());
        boolean isNotValid = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee5.getEmployeeId()).getStatusCode().is5xxServerError();
        assertTrue(isNotValid);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }

    private static void assertReportingStructure(ReportingStructure expected, ReportingStructure actual) {
        assertEquals(expected.getEmployee().getEmployeeId(), actual.getEmployee().getEmployeeId());
        for(Employee e : actual.getEmployee().getDirectReports()) {
            assertTrue(expected.getEmployee().getDirectReports().stream().anyMatch(m -> m.getEmployeeId().equals(e.getEmployeeId())));
        }
    }
}
