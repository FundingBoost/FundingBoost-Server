package kcs.funding.fundingboost.domain.dto.response;

import kcs.funding.fundingboost.domain.entity.Funding;
import kcs.funding.fundingboost.domain.entity.Member;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FriendFundingDto(
        CommonFriendFundingDto commonFriendFundingDto
) {
    public static FriendFundingDto fromEntity(
            CommonFriendFundingDto commonFriendFundingDto
    ) {
        return FriendFundingDto.builder()
                .commonFriendFundingDto(commonFriendFundingDto)
                .build();
    }
}
