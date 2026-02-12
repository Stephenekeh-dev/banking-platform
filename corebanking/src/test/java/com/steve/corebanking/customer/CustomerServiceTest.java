package com.steve.corebanking.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@email.com");
        customer.setPhone("08012345678");
        customer.setAddress("Lagos");
        customer.setBvn("12345678901");
    }

    // ✅ SUCCESS CASE
    @Test
    void shouldCreateCustomerSuccessfully() {
        // given
        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(false);
        when(customerRepository.existsByBvn(customer.getBvn())).thenReturn(false);
        when(customerRepository.save(customer)).thenReturn(customer);

        // when
        Customer savedCustomer = customerService.createCustomer(customer);

        // then
        assertNotNull(savedCustomer);
        assertEquals(customer.getEmail(), savedCustomer.getEmail());

        verify(customerRepository).save(customer);
    }

    // ❌ EMAIL EXISTS
    @Test
    void shouldThrowExceptionIfEmailExists() {
        // given
        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(true);

        // when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.createCustomer(customer)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(customerRepository, never()).save(any());
    }

    // ❌ BVN EXISTS
    @Test
    void shouldThrowExceptionIfBvnExists() {
        // given
        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(false);
        when(customerRepository.existsByBvn(customer.getBvn())).thenReturn(true);

        // when & then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.createCustomer(customer)
        );

        assertEquals("BVN already exists", exception.getMessage());

        verify(customerRepository, never()).save(any());
    }

    // ✅ FETCH ALL CUSTOMERS
    @Test
    void shouldReturnAllCustomers() {
        // given
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        // when
        List<Customer> customers = customerService.getAllCustomers();

        // then
        assertEquals(1, customers.size());
        verify(customerRepository).findAll();
    }
}
