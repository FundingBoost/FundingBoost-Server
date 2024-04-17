package kcs.funding.fundingboost.domain.dto.response;

import kcs.funding.fundingboost.domain.entity.Funding;
import lombok.Builder;

@Builder
public record HomeFriendFundingDto(
        CommonFriendFundingDto commonFriendFundingDto,
        String nowFundingItemImageUrl
) {

    public static HomeFriendFundingDto fromEntity(
            CommonFriendFundingDto commonFriendFundingDto,
            String nowFundingItemImageUrl
    ) {

        return HomeFriendFundingDto.builder()
                .commonFriendFundingDto(commonFriendFundingDto)
                .nowFundingItemImageUrl(nowFundingItemImageUrl)
                .build();
    }
}
