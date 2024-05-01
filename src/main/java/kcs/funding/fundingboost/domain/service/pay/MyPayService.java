package kcs.funding.fundingboost.domain.service.pay;

import static kcs.funding.fundingboost.domain.exception.ErrorCode.BAD_REQUEST_PARAMETER;
import static kcs.funding.fundingboost.domain.exception.ErrorCode.INVALID_FUNDINGITEM_STATUS;
import static kcs.funding.fundingboost.domain.exception.ErrorCode.NOT_FOUND_DELIVERY;
import static kcs.funding.fundingboost.domain.exception.ErrorCode.NOT_FOUND_ITEM;
import static kcs.funding.fundingboost.domain.exception.ErrorCode.NOT_FOUND_MEMBER;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kcs.funding.fundingboost.domain.dto.common.CommonSuccessDto;
import kcs.funding.fundingboost.domain.dto.request.pay.myPay.ItemPayDto;
import kcs.funding.fundingboost.domain.dto.request.pay.myPay.MyPayDto;
import kcs.funding.fundingboost.domain.dto.request.pay.myPay.PayRemainDto;
import kcs.funding.fundingboost.domain.dto.response.common.CommonItemDto;
import kcs.funding.fundingboost.domain.dto.response.myPage.deliveryManage.DeliveryDto;
import kcs.funding.fundingboost.domain.dto.response.pay.myPay.MyFundingPayViewDto;
import kcs.funding.fundingboost.domain.dto.response.pay.myPay.MyNowOrderPayViewDto;
import kcs.funding.fundingboost.domain.dto.response.pay.myPay.MyOrderPayViewDto;
import kcs.funding.fundingboost.domain.entity.Delivery;
import kcs.funding.fundingboost.domain.entity.FundingItem;
import kcs.funding.fundingboost.domain.entity.GiftHubItem;
import kcs.funding.fundingboost.domain.entity.Item;
import kcs.funding.fundingboost.domain.entity.member.Member;
import kcs.funding.fundingboost.domain.entity.Order;
import kcs.funding.fundingboost.domain.entity.OrderItem;
import kcs.funding.fundingboost.domain.exception.CommonException;
import kcs.funding.fundingboost.domain.exception.ErrorCode;
import kcs.funding.fundingboost.domain.repository.DeliveryRepository;
import kcs.funding.fundingboost.domain.repository.ItemRepository;
import kcs.funding.fundingboost.domain.repository.MemberRepository;
import kcs.funding.fundingboost.domain.repository.OrderRepository;
import kcs.funding.fundingboost.domain.repository.fundingItem.FundingItemRepository;
import kcs.funding.fundingboost.domain.repository.giftHubItem.GiftHubItemRepository;
import kcs.funding.fundingboost.domain.repository.orderItem.OrderItemRepository;
import kcs.funding.fundingboost.domain.service.utils.PayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPayService {

    private final DeliveryRepository deliveryRepository;
    private final MemberRepository memberRepository;
    private final FundingItemRepository fundingItemRepository;
    private final GiftHubItemRepository giftHubItemRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public MyFundingPayViewDto myFundingPayView(Long fundingItemId, Long memberId) {
        FundingItem fundingItem = fundingItemRepository.findById(fundingItemId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_FUNDING_ITEM));

        List<DeliveryDto> deliveryDtoList = deliveryRepository.findAllByMemberId(memberId)
                .stream()
                .map(DeliveryDto::fromEntity)
                .toList();

        if (!fundingItem.isFinishedStatus()) {
            throw new CommonException(ErrorCode.INVALID_FUNDINGITEM_STATUS);
        }

        if (fundingItem.getFunding().isFundingStatus()) {
            throw new CommonException(ErrorCode.ONGOING_FUNDING_ERROR);
        }

        if (!fundingItem.getFunding().getMember().getMemberId().equals(memberId)) {
            throw new CommonException(BAD_REQUEST_PARAMETER);
        }

        return MyFundingPayViewDto.fromEntity(fundingItem, deliveryDtoList);
    }

    public MyOrderPayViewDto myOrderPayView(List<Long> itemIds, Long memberId) {

        List<DeliveryDto> deliveryDtoList = deliveryRepository.findAllByMemberId(memberId)
                .stream()
                .map(DeliveryDto::fromEntity)
                .toList();

        List<Long> giftHubItemIds = giftHubItemRepository.findGiftHubItemByMemberIdAndItemIds(memberId, itemIds)
                .stream().map(GiftHubItem::getGiftHunItemId)
                .toList();

        List<CommonItemDto> commonItemDtoList = itemIds.stream()
                .flatMap(i -> itemRepository.findById(i).stream())
                .map(item -> CommonItemDto.fromEntity(item.getItemId(),
                        item.getItemImageUrl(),
                        item.getItemName(),
                        item.getOptionName(),
                        item.getItemPrice()))
                .toList();

        int point = memberRepository.findById(memberId)
                .orElseThrow(() -> new CommonException(NOT_FOUND_MEMBER))
                .getPoint();

        return MyOrderPayViewDto.fromEntity(commonItemDtoList, giftHubItemIds, deliveryDtoList, point);
    }

    public MyNowOrderPayViewDto MyOrderNowPayView(Long itemId, Long memberId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CommonException(NOT_FOUND_ITEM));
        CommonItemDto commonItemDto = CommonItemDto.fromEntity(item.getItemId(),
                item.getItemImageUrl(),
                item.getItemName(),
                item.getOptionName(),
                item.getItemPrice()
        );

        int point = memberRepository.findById(memberId)
                .orElseThrow(() -> new CommonException(NOT_FOUND_MEMBER))
                .getPoint();

        List<DeliveryDto> deliveryDtoList = deliveryRepository.findAllByMemberId(memberId)
                .stream()
                .map(DeliveryDto::fromEntity)
                .toList();

        return MyNowOrderPayViewDto.fromEntity(commonItemDto, deliveryDtoList, point);
    }

    @Transactional
    public CommonSuccessDto payMyItem(MyPayDto myPayDto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CommonException(NOT_FOUND_MEMBER));

        Delivery delivery = deliveryRepository.findById(myPayDto.deliveryId())
                .orElseThrow(() -> new CommonException((NOT_FOUND_DELIVERY)));
        if (delivery.getMember() != member) {
            throw new CommonException(BAD_REQUEST_PARAMETER);
        }

        if (myPayDto.itemPayDtoList().isEmpty()) {
            throw new CommonException(BAD_REQUEST_PARAMETER);
        }

        PayUtils.deductPointsIfPossible(member, myPayDto.usingPoint());

        List<Long> itemIds = myPayDto.itemPayDtoList().stream()
                .map(ItemPayDto::itemId)
                .toList();

        Map<Long, Item> itemMap = itemRepository.findItemsByItemIds(itemIds).stream()
                .collect(Collectors.toMap(Item::getItemId, item -> item));

        Order order = Order.createOrder(0, member, delivery);
        List<OrderItem> orderItems = myPayDto.itemPayDtoList().stream()
                .map(itemPayDto -> {
                    Item item = itemMap.get(itemPayDto.itemId());
                    if (item == null) {
                        throw new CommonException(NOT_FOUND_ITEM);
                    }
                    int quantity = itemPayDto.quantity();
                    if (quantity <= 0) {
                        throw new CommonException(BAD_REQUEST_PARAMETER);
                    }
                    order.plusTotalPrice(item.getItemPrice() * quantity);
                    return OrderItem.createOrderItem(order, item, quantity);
                })
                .toList();

        orderItemRepository.saveAll(orderItems);
        orderRepository.save(order);

        return CommonSuccessDto.fromEntity(true);
    }

    @Transactional
    public CommonSuccessDto payMyFunding(Long fundingItemId, PayRemainDto payRemainDto, Long memberId) {
        FundingItem fundingItem = fundingItemRepository.findFundingItemAndItemByFundingItemId(fundingItemId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CommonException(NOT_FOUND_MEMBER));
        Delivery delivery = deliveryRepository.findById(payRemainDto.deliveryId())
                .orElseThrow(() -> new CommonException(NOT_FOUND_DELIVERY));

        if (!fundingItem.isFinishedStatus()) {
            throw new CommonException(INVALID_FUNDINGITEM_STATUS);
        } else {
            fundingItem.finishFunding();
        }

        PayUtils.deductPointsIfPossible(member, payRemainDto.usingPoint());

        Order order = Order.createOrder(fundingItem.getItem().getItemPrice(), member, delivery);
        OrderItem orderItem = OrderItem.createOrderItem(order, fundingItem.getItem(), 1);
        orderRepository.save(order);
        orderItemRepository.save(orderItem);

        return CommonSuccessDto.fromEntity(true);
    }
}