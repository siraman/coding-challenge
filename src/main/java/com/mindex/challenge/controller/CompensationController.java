package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    CompensationService compensationService;

    @PostMapping(value = "/compensation")
    public Compensation create(@RequestBody Compensation compensation) {
        LOG.debug("Received compensation create request for [{}]", compensation);
        if(compensation.getEmployee() == null)
            throw new RuntimeException("Employee details cannot be null");
        if(compensation.getSalary() == 0)
            throw new RuntimeException("Employee salary cannot be null");
        if(compensation.getEffectiveDate() == null)
            throw new RuntimeException("Effective date cannot be null");
        return compensationService.create(compensation);
    }

    @GetMapping("/compensation/{id}")
    public Compensation read(@PathVariable String id, @RequestParam(value = "getFullDetails", required = false)
            boolean getFullDetails) {
        LOG.debug("Received compensation retrieve request for id [{}]", id);

        return compensationService.read(id, getFullDetails);
    }
    /*
    @PutMapping("/compensation")
    public Compensation update(@RequestBody Compensation compensation) {
        LOG.debug("Received compensation create request for [{}]", compensation);

        return compensationService.update(compensation);
    }
     */
}
