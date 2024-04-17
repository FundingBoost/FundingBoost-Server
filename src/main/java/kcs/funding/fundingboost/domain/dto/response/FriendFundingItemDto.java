package kcs.funding.fundingboost.domain.dto.response;

import kcs.funding.fundingboost.domain.entity.Item;
import lombok.Builder;

@Builder
public record FriendFundingItemDto(
        int itemPrice,
        String itemImageUrl
) {
    public static FriendFundingItemDto fromEntity(Item item) {
        return FriendFundingItemDto.builder()
                .itemPrice(item.getItemPrice())
                .itemImageUrl(item.getItemImageUrl())
                .build();
    }
}
