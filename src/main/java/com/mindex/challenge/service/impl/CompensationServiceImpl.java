package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompensationServiceImpl implements CompensationService {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    CompensationRepository compensationRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);
        String employeeId = compensation.getEmployee().getEmployeeId();
        boolean isEmployeeExists = verifyEmployee(employeeId);
        if(!isEmployeeExists)
            throw new RuntimeException(String.format("Employee with employeeId: %s doesn't exists", employeeId));
        compensationRepository.insert(compensation);
        return compensation;
    }

    private boolean verifyEmployee(String employeeId) {
        return employeeRepository.countByEmployeeId(employeeId) > 0;
    }

    @Override
    public Compensation read(String employeeId, boolean getFullDetails) {
        LOG.debug("Request for compensation with id [{}]", employeeId);
        boolean isEmployeeExists = verifyEmployee(employeeId);
        if(!isEmployeeExists)
            throw new RuntimeException(String.format("Employee with employeeId: %s doesn't exists", employeeId));

        Compensation compensation = compensationRepository.findByEmployeeId(employeeId);

        if (compensation == null) {
            throw new RuntimeException("No compensation available for employeeId: " + employeeId);
        }

        if(getFullDetails) {
            Employee employee = employeeRepository.findByEmployeeId(employeeId);
            compensation.setEmployee(employee);
        }

        return compensation;
    }
    /*
    @Override
    public Compensation update(Compensation compensation) {
        LOG.debug("Updating compensation [{}]", compensation);
        String employeeId = compensation.getEmployee().getEmployeeId();
        boolean isEmployeeExists = verifyEmployee(employeeId);
        if(!isEmployeeExists)
            throw new RuntimeException(String.format("Employee with employeeId: %s doesn't exists", employeeId));
        compensationRepository.save(compensation);
        return compensation;
    }
     */
}
