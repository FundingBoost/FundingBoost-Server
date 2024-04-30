package kcs.funding.fundingboost.domain.service.pay;

import static kcs.funding.fundingboost.domain.exception.ErrorCode.NOT_FOUND_FUNDING_ITEM;
import static kcs.funding.fundingboost.domain.exception.ErrorCode.ONGOING_FUNDING_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kcs.funding.fundingboost.domain.dto.response.MyFundingPayViewDto;
import kcs.funding.fundingboost.domain.dto.response.MyOrderPayViewDto;
import kcs.funding.fundingboost.domain.entity.Delivery;
import kcs.funding.fundingboost.domain.entity.Funding;
import kcs.funding.fundingboost.domain.entity.FundingItem;
import kcs.funding.fundingboost.domain.entity.GiftHubItem;
import kcs.funding.fundingboost.domain.entity.Item;
import kcs.funding.fundingboost.domain.entity.Member;
import kcs.funding.fundingboost.domain.entity.Tag;
import kcs.funding.fundingboost.domain.exception.CommonException;
import kcs.funding.fundingboost.domain.repository.DeliveryRepository;
import kcs.funding.fundingboost.domain.repository.GiftHubItemRepository;
import kcs.funding.fundingboost.domain.repository.ItemRepository;
import kcs.funding.fundingboost.domain.repository.MemberRepository;
import kcs.funding.fundingboost.domain.repository.fundingItem.FundingItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyPayServiceTest {

    @Mock
    private FundingItemRepository fundingItemRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private MyPayService myPayService;

    private FundingItem fundingItem1;
    private FundingItem fundingItem2;

    private Member member;

    private Funding fundingFundingStatusIsTrue;
    private Funding fundingFundingStatusIsFalse;

    private Item item1;
    private Item item2;
    private Delivery delivery;
    private GiftHubItem giftHubItem;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GiftHubItemRepository giftHubItemRepository;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setup() {
        member = createMember();
        fundingFundingStatusIsTrue = createFundingFundingStatusIsTrue(member);
        fundingFundingStatusIsFalse = createFundingFundingStatusIsFalse(member);
        item1 = createItem1();
        item2 = createItem2();
        fundingItem1 = createFundingItem1(fundingFundingStatusIsTrue, item1);
        fundingItem2 = createFundingItem2(fundingFundingStatusIsFalse, item2);
        delivery = createDelivery(member);
        giftHubItem = GiftHubItem.createGiftHubItem(1, item1, member);
    }

    @DisplayName("내 펀딩 결제 정보를 조회 성공")
    @Test
    void myFundingPayView_Success() throws NoSuchFieldException, IllegalAccessException {
        //given
        Field memberId = member.getClass().getDeclaredField("memberId");
        memberId.setAccessible(true);
        memberId.set(member, 1L);

        Field fundingItemId = fundingItem2.getClass().getDeclaredField("fundingItemId");
        fundingItemId.setAccessible(true);
        fundingItemId.set(fundingItem2, 1L);

        when(fundingItemRepository.findById(member.getMemberId())).thenReturn(Optional.of(fundingItem2));
        when(deliveryRepository.findAllByMemberId(member.getMemberId())).thenReturn(Collections.emptyList());

        //when
        MyFundingPayViewDto result = myPayService.myFundingPayView(fundingItem2.getFundingItemId(),
                member.getMemberId());

        //then
        assertNotNull(result);
        verify(fundingItemRepository).findById(member.getMemberId());
        verify(deliveryRepository).findAllByMemberId(member.getMemberId());
    }

    @DisplayName("내 펀딩 결제 정보를 조회 실패 : 펀딩 상품이 존재하지 않는 경우")
    @Test
    void myFundingPayView_Fail_FundingItemNotFound() throws IllegalAccessException, NoSuchFieldException {
        //given
        Field memberId = member.getClass().getDeclaredField("memberId");
        memberId.setAccessible(true);
        memberId.set(member, 1L);

        Field fundingItemId = fundingItem2.getClass().getDeclaredField("fundingItemId");
        fundingItemId.setAccessible(true);
        fundingItemId.set(fundingItem2, 1L);

        when(fundingItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        //when
        CommonException exception = assertThrows(CommonException.class,
                () -> myPayService.myFundingPayView(fundingItem2.getFundingItemId(), member.getMemberId()));

        //then
        assertEquals(NOT_FOUND_FUNDING_ITEM.getMessage(), exception.getMessage());
    }

    @DisplayName("내 펀딩 결제 정보를 조회 실패 : 진행 중인 펀딩일 경우")
    @Test
    void myFundingPayView_Fail_OngoingFundingError() throws NoSuchFieldException, IllegalAccessException {
        //given
        Field fundingItemId = fundingItem1.getClass().getDeclaredField("fundingItemId");
        fundingItemId.setAccessible(true);
        fundingItemId.set(fundingItem1, 1L);

        when(fundingItemRepository.findById(fundingItem1.getFundingItemId())).thenReturn(Optional.of(fundingItem1));

        //when
        CommonException exception = assertThrows(CommonException.class,
                () -> myPayService.myFundingPayView(fundingItem1.getFundingItemId(), member.getMemberId()));

        //then
        assertEquals(ONGOING_FUNDING_ERROR.getMessage(), exception.getMessage());
    }

    //    @DisplayName("내 펀딩 결제 정보를 조회 실패 : 요청한 사용자가 펀딩의 소유자가 아닐 경우")
//    @ParameterizedTest
//    @CsvSource({
//            "'구태형', 'rnxogud136@gmail.com', '', 'https://p.kakaocdn.net/th/talkp/wowkAlwbLn/Ko25X6eV5bs1OycAz7n9Q1/lq4mv6_110x110_c.jpg', '', 'aFtpX2lZaFhvQ3JLe0J2QnFDcFxtXWhdbldgDA'",
//            "'맹인호', 'aoddlsgh98@gmail.com', '', 'https://p.kakaocdn.net/th/talkp/woBG0lIJfU/M6aVERkQ2Lv2sNfQaLMYrK/pzfmfl_110x110_c.jpg', '', 'aFluW29Ya1hpRXdBdEdyQHBGdlprW25baFFmDQ'"
//    })
//    void myFundingPayView_Fail_BadRequestParameter(String name, String email, String password, String profileImgUrl,
//                                                   String refreshToken, String kakaoUuid)
//            throws NoSuchFieldException, IllegalAccessException {
//        //given
//        Member anotherMember = Member.createMember(name, email, password, profileImgUrl, refreshToken, kakaoUuid);
//        Field memberId = anotherMember.getClass().getDeclaredField("memberId");
//        memberId.setAccessible(true);
//        memberId.set(anotherMember, 1L);
//
//        Field fundingItemId = fundingItem2.getClass().getDeclaredField("fundingItemId");
//        fundingItemId.setAccessible(true);
//        fundingItemId.set(fundingItem2, 1L);
//
//        when(fundingItemRepository.findById(fundingItem2.getFundingItemId())).thenReturn(Optional.of(fundingItem2));
//        when(deliveryRepository.findAllByMemberId(anotherMember.getMemberId())).thenReturn(Collections.emptyList());
//
//        //when
//        System.out.println(anotherMember.getMemberId());
//        System.out.println(fundingItem2.getFundingItemId());
//        System.out.println(fundingItem2.getFunding().getMember().getMemberId());
//        CommonException exception = assertThrows(CommonException.class,
//                () -> myPayService.myFundingPayView(fundingItem2.getFundingItemId(), anotherMember.getMemberId()));
//
//        //then
//        assertEquals(BAD_REQUEST_PARAMETER.getMessage(), exception.getMessage());
//    }
    @DisplayName("마이 페이 주문 페이지 조회")
    @Test
    void myOrderPayView() throws NoSuchFieldException, IllegalAccessException {
        //given
        Field memberId = member.getClass().getDeclaredField("memberId");
        memberId.setAccessible(true);
        memberId.set(member, 1L);

        List<Long> itemIds = Arrays.asList(item1.getItemId(), item2.getItemId());
//        List<DeliveryDto> mockDeliveryDtos = List.of(DeliveryDto.fromEntity(delivery));
//        List<Long> mockGiftHubItemIds = List.of(1L);

        when(memberRepository.findById(member.getMemberId())).thenReturn(Optional.of(member));
        when(deliveryRepository.findAllByMemberId(member.getMemberId())).thenReturn(List.of(delivery));
        when(giftHubItemRepository.findGiftHubItemByMemberIdAndItemIds(member.getMemberId(), itemIds)).thenReturn(
                List.of(giftHubItem));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
//        when(itemRepository.findById(anyLong())).thenAnswer(
//                i -> Optional.of(Item.createItem(i.getArgument(0), "URL", "ItemName", "OptionName", 100)));

        //when
        MyOrderPayViewDto result = myPayService.myOrderPayView(itemIds, member.getMemberId());

        //then
        assertNotNull(result);
        assertEquals(member.getPoint(), result.point());
        assertFalse(result.itemListDto().isEmpty());
        assertFalse(result.giftHubItemIds().isEmpty());
        assertFalse(result.deliveryListDto().isEmpty());

        verify(memberRepository, times(1)).findById(member.getMemberId());
        verify(deliveryRepository, times(1)).findAllByMemberId(member.getMemberId());
        verify(giftHubItemRepository, times(1)).findGiftHubItemByMemberIdAndItemIds(member.getMemberId(), itemIds);
        verify(itemRepository, times(itemIds.size())).findById(anyLong());
    }

    @Test
    void myOrderNowPayView() {
    }

//    @Transactional
//    public CommonSuccessDto payMyItem(MyPayDto paymentDto, Long memberId) {
//        Member findMember = memberRepository.findById(memberId)
//                .orElseThrow(() -> new CommonException(NOT_FOUND_MEMBER));
//        PayUtils.deductPointsIfPossible(findMember, paymentDto.usingPoint());
//        return CommonSuccessDto.fromEntity(true);
//    }

    @Test
    void payMyFunding() {
    }

    private static Member createMember() {
        return Member.createMemberWithPoint("임창희", "dlackdgml3710@gmail.com", "",
                "https://p.kakaocdn.net/th/talkp/wnbbRhlyRW/XaGAXxS1OkUtXnomt6S4IK/ky0f9a_110x110_c.jpg",
                46000,
                "", "aFxoWGFUZlV5SH9MfE9-TH1PY1JiV2JRaF83");
    }

    private static Funding createFundingFundingStatusIsTrue(Member member) {
        return Funding.createFunding(member, "생일축하해줘", Tag.BIRTHDAY, 100000,
                LocalDateTime.now().plusDays(14));
    }

    private static Funding createFundingFundingStatusIsFalse(Member member) {
        return Funding.createFundingForTest(member, "생일축하해줘", Tag.BIRTHDAY, 100000, 90000,
                LocalDateTime.now().plusDays(14));
    }

    private static Item createItem1() {
        return Item.createItem("NEW 루쥬 알뤼르 벨벳 뉘 블랑쉬 리미티드 에디션", 61000,
                "https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20240319133310_1fda0cf74e4f43608184bce3050ae22a.jpg",
                "샤넬", "뷰티", "00:00");
    }

    private static Item createItem2() {
        return Item.createItem("NEW 루쥬 코코 밤(+샤넬 기프트 카드)", 51000,
                "https://img1.kakaocdn.net/thumb/C320x320@2x.fwebp.q82/?fname=https%3A%2F%2Fst.kakaocdn.net%2Fproduct%2Fgift%2Fproduct%2F20220111185052_b92447cb764d470ead70b2d0fe75fe5c.jpg",
                "샤넬", "뷰티", "934 코랄린 [NEW]");
    }

    private static FundingItem createFundingItem1(Funding funding, Item item) {
        return FundingItem.createFundingItem(funding, item, 1);
    }

    private static FundingItem createFundingItem2(Funding funding, Item item) {
        return FundingItem.createFundingItem(funding, item, 2);
    }

    private static Delivery createDelivery(Member member) {
        return Delivery.createDelivery("서울 금천구 가산디지털1로 189 (주)LG 가산 디지털센터 12층", "010-1111-2222", "직장", member);
    }
}