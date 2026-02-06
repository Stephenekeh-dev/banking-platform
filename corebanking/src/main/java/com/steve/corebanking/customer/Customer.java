package com.steve.corebanking.customer;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private String address;

    @Column(unique = true)
    private String bvn; // Bank Verification Number
}