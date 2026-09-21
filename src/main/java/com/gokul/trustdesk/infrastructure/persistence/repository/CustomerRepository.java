package com.gokul.trustdesk.infrastructure.persistence.repository;

import com.gokul.trustdesk.infrastructure.persistence.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
}