package kcs.funding.fundingboost.domain.model;

import kcs.funding.fundingboost.domain.entity.Delivery;
import kcs.funding.fundingboost.domain.entity.Member;

public class DeliveryFixture {
    public static Delivery address1(Member member) {
        return Delivery.createDelivery("경기도 성남시 분당구 판교역로 166", "010-1234-5678", "사무실", member);
    }
}
