package kcs.funding.fundingboost.domain.service;

import kcs.funding.fundingboost.domain.dto.common.CommonSuccessDto;
import kcs.funding.fundingboost.domain.dto.request.RegisterFundingDto;
import kcs.funding.fundingboost.domain.dto.request.RegisterFundingItemDto;
import kcs.funding.fundingboost.domain.dto.response.CommonFriendFundingDto;
import kcs.funding.fundingboost.domain.dto.response.FriendFundingDto;
import kcs.funding.fundingboost.domain.dto.response.FriendFundingItemDto;
import kcs.funding.fundingboost.domain.dto.response.FundingRegistrationItemDto;
import kcs.funding.fundingboost.domain.entity.*;
import kcs.funding.fundingboost.domain.exception.CommonException;
import kcs.funding.fundingboost.domain.exception.ErrorCode;
import kcs.funding.fundingboost.domain.repository.FundingItemRepository;
import kcs.funding.fundingboost.domain.repository.ItemRepository;
import kcs.funding.fundingboost.domain.repository.MemberRepository;
import kcs.funding.fundingboost.domain.repository.funding.FundingRepository;
import kcs.funding.fundingboost.domain.repository.relationship.RelationshipRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FundingService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final FundingRepository fundingRepository;
    private final FundingItemRepository fundingItemRepository;
    private final RelationshipRepositoryRepository relationshipRepository;

    public List<FundingRegistrationItemDto> getFundingRegister(List<Long> itemList){

        return IntStream.range(0, itemList.size())
                .mapToObj(i -> FundingRegistrationItemDto.createFundingRegistrationItemDto(
                        itemRepository.findById(itemList.get(i))
                                .orElseThrow(()-> new RuntimeException("Item not found")),
                        (long) i + 1)).toList();
    }

    @Transactional
    public CommonSuccessDto putFundingAndFundingItem(Long memberId, RegisterFundingDto registerFundingDto) {

        List<RegisterFundingItemDto> registerFundingItemDtoList = registerFundingDto.registerFundingItemDtoList();

        List<Item> itemList = registerFundingItemDtoList.stream()
                .map(registerFundingItemDto -> itemRepository.findById(registerFundingItemDto.itemId())
                        .orElseThrow(() -> new RuntimeException("Item Not Found"))).toList();

        int sum = 0;
        for (Item item : itemList) {
            sum += item.getItemPrice();
        }

        Funding funding = Funding.createFunding(memberRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Member Not Found")),
                registerFundingDto.fundingMessage(),
                Tag.getTag(registerFundingDto.tag()),
                sum,
                registerFundingDto.deadline());

        fundingRepository.save(funding);

        for (int i = 0; i < registerFundingItemDtoList.size(); i++) {
            FundingItem fundingItem = FundingItem.createFundingItem(
                    funding,
                    itemRepository.findById(registerFundingItemDtoList.get(i).itemId())
                            .orElseThrow(() -> new RuntimeException("Item Not Found")),
                    i + 1);
            fundingItemRepository.save(fundingItem);
        }

        return CommonSuccessDto.fromEntity(true);
    }
      public CommonSuccessDto terminateFunding(Long fundingId) {
          Funding funding = fundingRepository.findById(fundingId)
                  .orElseThrow(() -> new RuntimeException("Funding not found"));
          funding.terminate();
          return CommonSuccessDto.fromEntity(true);
      }

    public List<CommonFriendFundingDto> getCommonFriendFundingList(Long memberId){
        List<CommonFriendFundingDto> commonFriendFundingDtoList = new ArrayList<>();
        List<Relationship> relationshipList= relationshipRepository.findFriendByMemberId(memberId);
        for(Relationship relationship : relationshipList){
            Funding friendFunding = fundingRepository.findByMemberIdAndStatus(relationship.getFriend().getMemberId(), true);

            int leftDate = (int) ChronoUnit.DAYS.between(LocalDate.now(),
                    friendFunding.getDeadline());
            String deadline = "D-" + leftDate;

            List<FundingItem> fundingItemList = fundingItemRepository.findFundingItemIdListByFunding(
                    friendFunding.getFundingId());
            List<FriendFundingItemDto> fundingItemDtoList = fundingItemList.stream()
                    .map(fundingItem -> FriendFundingItemDto.fromEntity(fundingItem.getItem())).toList();
            int totalPrice = friendFunding.getTotalPrice();

            if(totalPrice == 0){
                throw new CommonException(ErrorCode.INVALID_FUNDING_STATUS);
            }
            int fundingTotalPercent = friendFunding.getCollectPrice() * 100 / totalPrice;
            commonFriendFundingDtoList.add(CommonFriendFundingDto.fromEntity(
                    friendFunding,
                    deadline,
                    fundingTotalPercent,
                    fundingItemDtoList
            ));
        }

        return commonFriendFundingDtoList;
    }

    public List<FriendFundingDto> getFriendFundingList(Long memberId){
        List<CommonFriendFundingDto> commonFriendFundingDtoList = getCommonFriendFundingList(memberId);
        List<FriendFundingDto> friendFundingDtoList = new ArrayList<>();

        for(CommonFriendFundingDto commonFriendFundingDto : commonFriendFundingDtoList){
            friendFundingDtoList.add(FriendFundingDto.fromEntity(commonFriendFundingDto));
        }

        return friendFundingDtoList;
    }
}
