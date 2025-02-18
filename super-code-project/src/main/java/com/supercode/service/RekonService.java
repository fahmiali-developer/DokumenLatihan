package com.supercode.service;

import com.supercode.repository.DetailPaymentAggregatorRepository;
import com.supercode.repository.PaymentMethodRepository;
import com.supercode.repository.PosRepository;
import com.supercode.request.GeneralRequest;
import com.supercode.response.BaseResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class RekonService {

    @Inject
    PosRepository posRepository;

    @Inject
    DetailPaymentAggregatorRepository detailPaymentAggregatorRepository;

    @Inject
    PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public Response rekonProcess(GeneralRequest request) {

        // get data payment method
        List<String> pmIds =  paymentMethodRepository.getPaymentMethods();
        for(String pmId : pmIds){
            // get data pos
            request.setPmId(pmId);
            int countDataPos = posRepository.getCountDataPost(request);
            List<BigDecimal> grossAmounts = posRepository.getAllGrossAmount(request);
            // get data aggregator
            int countDataAggregator = detailPaymentAggregatorRepository.getCountDataAggregator(request, grossAmounts);
            System.out.println("Count pos "+ countDataPos + " count aggre "+ countDataAggregator + " PM ID "+ pmId);
            if(countDataPos==countDataAggregator){
                posRepository.updateFlagByCondition(request);
                detailPaymentAggregatorRepository.updateFlagByCondition(request, grossAmounts);

            }else {
                int minCount = Math.min(countDataPos, countDataAggregator);
                System.out.println("beda");
            }

        }

        BaseResponse baseResponse = new BaseResponse(200, "Success");
        return Response.status(200).entity(baseResponse).build();
    }
}
