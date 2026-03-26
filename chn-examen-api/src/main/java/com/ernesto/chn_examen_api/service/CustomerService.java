package com.ernesto.chn_examen_api.service;

import com.ernesto.chn_examen_api.dto.CustomerDTO;

import java.util.List;

public interface CustomerService {

    /**
     * Create a new customer.
     * @param customerDTO The customer information.
     * @return The created customer.
     */
    CustomerDTO createCustomer(CustomerDTO customerDTO);

    /**
     * Retrieve a customer by their ID.
     * @param id The customer ID.
     * @return The customer information.
     */
    CustomerDTO getCustomerById(Integer id);

    /**
     * Retrieve all customers.
     * @return A list of all customers.
     */
    List<CustomerDTO> getAllCustomers();

    /**
     * Update an existing customer.
     * @param id The customer ID.
     * @param customerDTO The updated information.
     * @return The updated customer.
     */
    CustomerDTO updateCustomer(Integer id, CustomerDTO customerDTO);

    /**
     * Delete a customer and their associated loan applications.
     * @param id The customer ID.
     */
    void deleteCustomer(Integer id);
}
