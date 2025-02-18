package com.supercode.repository;

import com.supercode.entity.HeaderPayment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HeaderPaymentRepository implements PanacheRepository<HeaderPayment> {
}
