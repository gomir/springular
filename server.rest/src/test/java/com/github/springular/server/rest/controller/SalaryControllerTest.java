package com.github.springular.server.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springular.server.common.SpringularIntegerationRestControllerTest;
import com.github.springular.server.component.employee.Nationality;
import com.github.springular.server.component.employee.SalaryQueryCriteriaDO;
import com.github.springular.server.component.employee.entity.EmployeeBE;
import com.github.springular.server.component.employee.entity.SalaryBE;
import com.github.springular.server.component.employee.repository.EmployeeRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SalaryControllerTest extends SpringularIntegerationRestControllerTest {

    @Autowired
    EmployeeRepository employeeRepository;

    @Test
    public void shouldFilterSalariesByEmployeeFirstName() throws Exception {

        employee("login001", "Barrack", "Obama")
             .withSalary(10, 2012, 1000)
             .withSalary(11, 2012, 1100)
             .withSalary(11, 2013, 1200)
             .save();
        employee("login002", "Barrack", "Johnson").withSalary(9, 2011, 150).save();
        employee("login002", "Bill", "Clinton").withSalary(7, 2013, 50).save();

        byte[] criteriaJson = filterSalaries()
                                    .byFirstName("Barrack")
                                    .byLastName("Ob")
                                    .byMonth(11)
                                    .byYear(2012)
                                    .asJson();

        mvc().perform(post("/secured/salary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(criteriaJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount", is(1100)))
                .andExpect(jsonPath("$[0].employeeFirstName", is("Barrack")));
    }

    private CriteriaBuilder filterSalaries() {
        return new CriteriaBuilder();
    }

    class CriteriaBuilder {
        SalaryQueryCriteriaDO criteria = new SalaryQueryCriteriaDO();

        public byte[] asJson() throws JsonProcessingException {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(criteria);
        }

        public CriteriaBuilder byFirstName(String firstName) {
            criteria.setEmployeeFirstName(firstName);
            return this;
        }

        public CriteriaBuilder byMonth(int month) {
            criteria.setMonth(month);
            return this;
        }

        public CriteriaBuilder byYear(int year) {
            criteria.setYear(year);
            return this;
        }

        public CriteriaBuilder byLastName(String lastName) {
            criteria.setEmployeeLastName(lastName);
            return this;
        }
    }

    public EmployeeBuilder employee(String login, String firstName, String lastName) {
        return new EmployeeBuilder(login, firstName, lastName);
    }

    class EmployeeBuilder {
        EmployeeBE employee;
        public EmployeeBuilder(String login, String firstName, String lastName) {
            employee = new EmployeeBE();
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setLogin(login);
            employee.setNationality(Nationality.POLAND.code());
            employee.setSalaries(new ArrayList<SalaryBE>());
        }
        public EmployeeBuilder withSalary(int month, int year, int amount) {
            SalaryBE salary = new SalaryBE();
            salary.setMonth(month);
            salary.setYear(year);
            salary.setAmount(amount);
            salary.setEmployee(employee);
            employee.getSalaries().add(salary);
            return this;
        }
        public void save() {
            employeeRepository.save(employee);
        }
    }

}
