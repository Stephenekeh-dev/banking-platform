package com.steve.corebanking.customer;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (customerRepository.existsByBvn(customer.getBvn())) {
            throw new RuntimeException("BVN already exists");
        }

        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }
}