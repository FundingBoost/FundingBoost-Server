package kcs.funding.fundingboost.domain.model;

import java.lang.reflect.Field;
import java.util.List;
import kcs.funding.fundingboost.domain.entity.Delivery;
import kcs.funding.fundingboost.domain.entity.member.Member;

public class DeliveryFixture {

    public static Delivery address1(Member member) throws NoSuchFieldException, IllegalAccessException {
        Delivery delivery = Delivery.createDelivery("경기도 성남시 분당구 판교역로 166", "010-1234-5678", "장이수", member);
        Field deliveryId = delivery.getClass().getDeclaredField("deliveryId");
        deliveryId.setAccessible(true);
        deliveryId.set(delivery, 1L);
        return delivery;
    }

    public static Delivery address2(Member member) {
        return Delivery.createDelivery("서울특별시 강남구 테헤란로", "010-1234-1234", "홍길동", member);
    }

    public static Delivery address3(Member member) {
        return Delivery.createDelivery("경기도 수원시 행궁동", "010-1234-1234", "마동석", member);
    }

    public static List<Delivery> addresses3(Member member) throws NoSuchFieldException, IllegalAccessException {
        return List.of(DeliveryFixture.address1(member),
                DeliveryFixture.address2(member),
                DeliveryFixture.address3(member));
    }
}
