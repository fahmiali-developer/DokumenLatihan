package com.supercode.repository;

import com.supercode.request.GeneralRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DetailPaymentAggregatorRepository implements PanacheRepository<com.supercode.entity.DetailPaymentAggregator> {

    @PersistenceContext
    EntityManager entityManager;
    public int getCountDataAggregator(GeneralRequest request, List<BigDecimal> grossAmounts) {

        Object result = entityManager.createNativeQuery(
                        "select count(*) from detail_agregator_payment dpos " +
                                "where trans_date = ?1 and SUBSTRING(trans_time, 1, 2) = ?2 " +
                                "and gross_amount IN (?3) and pm_id = ?4 and branch_id = ?5 " +
                                "and flag_rekon_pos ='0' ")
                .setParameter(1, request.getTransDate())
                .setParameter(2, request.getTransTime()) // Compare only first 2 digits
                .setParameter(3, grossAmounts) // Menggunakan IN dengan ()
                .setParameter(4, request.getPmId())
                .setParameter(5, request.getBranchId())
                .getSingleResult();

        return ((Number) result).intValue();
    }

    public void updateFlagByCondition(GeneralRequest request, List<BigDecimal> grossAmounts) {
        // Jika Anda ingin menggunakan BigDecimal, maka parameter "grossAmounts" sebaiknya menggunakan tipe BigDecimal
        String query = "UPDATE detail_agregator_payment dpos " +
                "SET flag_rekon_pos = :newFlag " +
                "WHERE trans_date = :transDate " +
                "AND SUBSTRING(trans_time, 1, 2) = :transTime " +
                "AND gross_amount IN :grossAmounts " +  // Menggunakan parameter untuk 'IN'
                "AND pm_id = :pmId " +
                "AND branch_id = :branchId " +
                "AND flag_rekon_pos = '0'";

        entityManager.createNativeQuery(query)
                .setParameter("newFlag", "1")
                .setParameter("transDate", request.getTransDate())
                .setParameter("transTime", request.getTransTime())
                .setParameter("grossAmounts", grossAmounts)  // Menggunakan parameter list untuk IN
                .setParameter("pmId", request.getPmId())
                .setParameter("branchId", request.getBranchId())
                .executeUpdate();
    }



}
