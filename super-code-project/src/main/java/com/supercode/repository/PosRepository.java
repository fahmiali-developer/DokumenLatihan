package com.supercode.repository;

import com.supercode.entity.DetailPaymentPos;
import com.supercode.request.GeneralRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class PosRepository implements PanacheRepository<DetailPaymentPos> {

    @PersistenceContext
    EntityManager entityManager;

    public int getCountDataPost(GeneralRequest request) {
        Object result = entityManager.createNativeQuery(
                        "select count(*) from detail_point_of_sales dpos " +
                                "where trans_date = ?1 and SUBSTRING(trans_time, 1, 2) = ?2  and pm_id = ?3 and branch_id =?4 " +
                                "and flag_rekon_ecom ='0'")
                .setParameter(1, request.getTransDate())
                .setParameter(2, request.getTransTime())
                .setParameter(3, "0")
                .setParameter(4, request.getBranchId())
                .getSingleResult();

        return ((Number) result).intValue();
    }

    public List<BigDecimal> getAllGrossAmount(GeneralRequest request) {
        List<BigDecimal> resultList = entityManager.createNativeQuery(
                        "select gross_amount from detail_point_of_sales dpos " +
                                "where trans_date = ?1 and SUBSTRING(trans_time, 1, 2) = ?2  " +
                                "and pm_id = ?3 and branch_id =?4 and flag_rekon_ecom ='0'")
                .setParameter(1, request.getTransDate())
                .setParameter(2, request.getTransTime())
                .setParameter(3, "0")
                .setParameter(4, request.getBranchId())
                .getResultList();

        return resultList;
    }

    public void updateFlagByCondition(GeneralRequest request) {
        entityManager.createNativeQuery(
                        "UPDATE detail_point_of_sales dpos " +
                                "SET flag_rekon_ecom = :newFlag " +
                                "WHERE trans_date = :transDate " +
                                "AND SUBSTRING(trans_time, 1, 2) = :transTime " +
                                "AND pm_id = :pmId " +
                                "AND branch_id = :branchId " +
                                "AND flag_rekon_ecom ='0'")
                .setParameter("newFlag", "1") // Ganti "Y" dengan nilai flag yang diinginkan
                .setParameter("transDate", request.getTransDate())
                .setParameter("transTime", request.getTransTime())
                .setParameter("pmId", "0")
                .setParameter("branchId", request.getBranchId())
                .executeUpdate();
    }
}
