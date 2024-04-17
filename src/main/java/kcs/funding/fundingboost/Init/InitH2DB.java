package kcs.funding.fundingboost.Init;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import kcs.funding.fundingboost.domain.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class InitH2DB {
    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.init();
//        initService.mainPageTestInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    @Slf4j
    static class InitService{

        private final EntityManager em;

        public void init() {
            Member member1 = Member.createMember("nickname1", "email@gmail.com", "url");
            em.persist(member1);

            Member member2 = Member.createMemberWithPoint("nickname2", "email2@gmail.com", "url", 20000);
            em.persist(member2);

            Item item1 = Item.createItem("그릭요거트 딸기 생크림 피스 + 아메리카노 (R)", 10700,
                    "https://gift.kakao.com/product/5543372", "투썸플레이스",
                    "카페", "");
            Item item2 = Item.createItem("[예스24] 양수인간 : 삶의 격을 높이는 내면 변화 심리학", 17820
                    , "https://gift.kakao.com/product/9443715", "인문",
                    "책", "");

            Item item3 = Item.createItem(
                    "[각인/선물포장] NEW 디올 어딕트 립스틱", 57000,
                    "https://gift.kakao.com/product/4418907", "디올",
                    "화장", "[NEW] 481 데지");


            em.persist(item1);
            em.persist(item2);
            em.persist(item3);

            Order order1 = Order.createOrder(1, 10700, item1, member1);
            Order order2 = Order.createOrder(1, 17820, item2, member2);
            em.persist(order1);
            em.persist(order2);


            Delivery delivery1  = Delivery.createDelivery("서울시 가산 디지털단지", "010-1111-1111", "nickname1", member1, item1);
            Delivery delivery2  = Delivery.createDelivery("경기도 오산시", "010-2222-2222", "nickname2", member1, item2);
            em.persist(delivery1);
            em.persist(delivery2);


            Funding funding = Funding.createFunding(member1, "Thanks", Tag.BIRTHDAY, 57000, LocalDateTime.of(2023,12,27,0,0,0));
            em.persist(funding);

            FundingItem fundingItem = FundingItem.createFundingItem(funding,item3,1);
            em.persist(fundingItem);

            GiftHubItem giftHubItem1 = GiftHubItem.createGiftHubItem(1, item1, member1);
            GiftHubItem giftHubItem2 = GiftHubItem.createGiftHubItem(1, item2, member1);
            em.persist(giftHubItem1);
            em.persist(giftHubItem2);
        }

        public void mainPageTestInit() {
            Member member1 = Member.createMember("nickname", "email@gmail.com", "url");
            Member member2 = Member.createMember("friend1", "email2@gmail.com", "url2");
            Member member3 = Member.createMember("friend2", "email3@gmail.com", "url3");
            em.persist(member1);
            em.persist(member2);
            em.persist(member3);

            Item item1 = Item.createItem("그릭요거트 딸기 생크림 피스 + 아메리카노 (R)", 10700,
                "https://gift.kakao.com/product/5543372", "투썸플레이스",
                "카페", "");

            Item item2 = Item.createItem("[예스24] 양수인간 : 삶의 격을 높이는 내면 변화 심리학", 17820
                , "https://gift.kakao.com/product/9443715", "인문",
                "책", "");

            Item item3 = Item.createItem(
                "[각인/선물포장] NEW 디올 어딕트 립스틱", 57000,
                "https://gift.kakao.com/product/4418907", "디올",
                "화장", "[NEW] 481 데지");
            em.persist(item1);
            em.persist(item2);
            em.persist(item3);

            // 친구 관계 설정
            List<Relationship> relationshipList = Relationship.createRelationships(member1,
                member2);
            for (Relationship relationship : relationshipList) {
                em.persist(relationship);
            }

            // 내 펀딩 정보 추가
            Funding myFunding = Funding.createFunding(member1, "생일축하해줘", Tag.BIRTHDAY, 28520,
                LocalDateTime.now().plusDays(14));
            em.persist(myFunding);

            FundingItem fundingItem1 = FundingItem.createFundingItem(myFunding, item1, 1);
            FundingItem fundingItem2 = FundingItem.createFundingItem(myFunding, item2, 2);
            em.persist(fundingItem1);
            em.persist(fundingItem2);

            // 친구 펀딩 정보 추가
            Funding friend1Funding = Funding.createFunding(member2, "드디어 졸업 성공~~", Tag.GRADUATE,
                57000,
                LocalDateTime.now().plusDays(7));
            em.persist(friend1Funding);

            FundingItem friendFundingItem1 = FundingItem.createFundingItem(friend1Funding, item3,
                1);
            em.persist(friendFundingItem1);
        }
    }
}
