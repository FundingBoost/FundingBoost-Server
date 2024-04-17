package kcs.funding.fundingboost.domain.controller;

import kcs.funding.fundingboost.domain.dto.common.CommonSuccessDto;
import kcs.funding.fundingboost.domain.dto.global.ResponseDto;
import kcs.funding.fundingboost.domain.dto.request.RegisterFundingDto;
import kcs.funding.fundingboost.domain.dto.response.FriendFundingDto;
import kcs.funding.fundingboost.domain.dto.response.FundingRegistrationItemDto;
import kcs.funding.fundingboost.domain.service.FundingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/funding")
@RequiredArgsConstructor
public class FundingController {

    private final FundingService fundingService;

    @GetMapping("")
    public ResponseDto<List<FundingRegistrationItemDto>> viewFundingRegistration(
            @RequestParam(name = "memberId") Long memberId,
            @RequestBody List<Long> itemList
    ){
        return ResponseDto.ok(fundingService.getFundingRegister(itemList));
    }

    @PostMapping("")
    public ResponseDto<CommonSuccessDto> registerFunding(
            @RequestParam(name = "memberId") Long memberId,
            @RequestBody RegisterFundingDto registerFundingDto
    ) {

        return ResponseDto.created(fundingService.putFundingAndFundingItem(memberId, registerFundingDto));
    }
    @PostMapping("/close/{fundingId}")
    public CommonSuccessDto closeFunding(@PathVariable("fundingId") Long fundingId) {
        return fundingService.terminateFunding(fundingId);

    }

    @GetMapping("/friends")
    public ResponseDto<List<FriendFundingDto>> viewFriendFundingList(
            @RequestParam(name = "memberId") Long memberId
    ){
        return ResponseDto.ok(fundingService.getFriendFundingList(memberId));
    }
}
