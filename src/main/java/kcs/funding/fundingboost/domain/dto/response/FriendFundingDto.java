package kcs.funding.fundingboost.domain.dto.response;

import kcs.funding.fundingboost.domain.entity.Funding;
import kcs.funding.fundingboost.domain.entity.Member;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FriendFundingDto(
        String friendProfileImgUrl,
        Long fundingId,
        String deadline,
        List<String> fundingItemImageUrlList,
        String tag,
        int fundingTotalPercent
) {
    public static FriendFundingDto fromEntity(
            Funding funding,
            Member member,
            String deadline,
            List<String> fundingItemImageUrlList,
            int fundingTotalPercent
    ) {
        return FriendFundingDto.builder()
                .friendProfileImgUrl(member.getProfileImgUrl())
                .fundingId(funding.getFundingId())
                .deadline(deadline)
                .fundingItemImageUrlList(fundingItemImageUrlList)
                .tag(funding.getTag().getTag())
                .fundingTotalPercent(fundingTotalPercent)
                .build();
    }
}
