package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getNumberOfReports(String id) {
        ReportingStructure reportingStructure = new ReportingStructure();
        Employee employee = employeeRepository.findByEmployeeId(id);
        if(employee == null)
            throw new RuntimeException("Employee doesn't exists with id: " + id);
        HashSet<Employee> reports = getDirectReports(employee);
        employee.setDirectReports(new ArrayList<Employee>(reports));
        reportingStructure.setEmployee(employee);
        int numberOfReports = reports.size();
        reportingStructure.setNumberOfReports(numberOfReports);
        return reportingStructure;
    }

    private HashSet<Employee> getDirectReports(Employee employee) {
        HashSet<Employee> directReports = new HashSet<>();
        Queue<Employee> queue = new LinkedList<>();
        if (employee != null) {
            if (employee.getDirectReports() != null) {
                List<Employee> reports = getDirectReportsEmployee(employee);
                queue.addAll(reports);
                directReports.addAll(reports);
                while (!queue.isEmpty()) {
                    Employee e = queue.poll();
                    if (e.getDirectReports() != null) {
                        reports = getDirectReportsEmployee(e);
                        queue.addAll(reports);
                        directReports.addAll(reports);
                    }
                }
            }
        }
        return directReports;
    }

    private List<Employee> getDirectReportsEmployee(Employee employee) {
        List<Employee> reports = new ArrayList<>();
        for (Employee e : employee.getDirectReports()) {
            reports.add(employeeRepository.findByEmployeeId(e.getEmployeeId()));
        }
        return reports;
    }
}
