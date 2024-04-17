package kcs.funding.fundingboost.domain.dto.response;

import kcs.funding.fundingboost.domain.entity.Funding;
import kcs.funding.fundingboost.domain.entity.FundingItem;
import kcs.funding.fundingboost.domain.entity.Member;
import lombok.Builder;

import java.util.List;

@Builder
public record CommonFriendFundingDto(
        Long fundingId,
        String nickName,
        String friendProfileImgUrl,
        String friendFundingDeadlineDate,
        String tag,
        int collectPrice,
        int friendFundingPercent,
        List<FriendFundingItemDto> friendFundingItemDtoList
) {
    public static CommonFriendFundingDto fromEntity(
            Funding funding,
            String deadline,
            int fundingTotalPercent,
            List<FriendFundingItemDto> friendFundingItemDtoList
    ) {
        return CommonFriendFundingDto.builder()
               .fundingId(funding.getFundingId())
               .nickName(funding.getMember().getNickName())
               .friendProfileImgUrl(funding.getMember().getProfileImgUrl())
               .friendFundingDeadlineDate(deadline)
               .tag(funding.getTag().getTag())
                .collectPrice(funding.getCollectPrice())
               .friendFundingPercent(fundingTotalPercent)
                .friendFundingItemDtoList(friendFundingItemDtoList)
               .build();
    }
}
