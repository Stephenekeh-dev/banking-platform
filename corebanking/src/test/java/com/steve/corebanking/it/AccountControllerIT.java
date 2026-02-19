package com.steve.corebanking.it;

import com.steve.corebanking.CoreBankingApplication;
import com.steve.corebanking.account.Account;
import com.steve.corebanking.account.AccountRepository;
import com.steve.corebanking.account.AccountType;
import com.steve.corebanking.customer.Customer;
import com.steve.corebanking.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;


import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AccountControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;


    @Autowired
    private ApplicationContext context;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCustomer_shouldPersistCustomer() throws Exception {

        String payload = """
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phone": "08012345678",
      "address": "Lagos",
      "bvn": "12345678901"
    }
    """;

        mockMvc.perform(post("/api/customers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"));

        assertEquals(1, customerRepository.count());
    }

    @Test
    @WithMockUser(username = "teller1", roles = {"TELLER"})
    void depositTransaction_shouldUpdateAccountBalance() throws Exception {

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@mail.com");
        customer.setPhone("080");
        customer.setAddress("Lagos");
        customer.setBvn("12345678901");

        customer = customerRepository.save(customer);

        Account account = new Account();
        account.setAccountNumber("ACC123");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.ZERO);
        account.setCustomer(customer);

        accountRepository.save(account);

        String payload = """
    {
      "accountNumber": "ACC123",
      "amount": 100.00,
      "type": "DEPOSIT",
      "narration": "Cash deposit"
    }
    """;

        mockMvc.perform(post("/api/transactions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Account updated =
                accountRepository.findByAccountNumber("ACC123").orElseThrow();

        assertEquals(
                0,
                updated.getBalance().compareTo(new BigDecimal("100")),
                "Balance should be updated to 100"
        );
    }

    @Test
    void printAllControllers() {
        context.getBeansWithAnnotation(RestController.class)
                .forEach((k, v) -> System.out.println(v.getClass().getName()));
    }

    @Test
    void printAllMappings() {
        handlerMapping.getHandlerMethods()
                .forEach((k, v) -> System.out.println(k + " -> " + v));
    }

}
