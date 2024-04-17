package kcs.funding.fundingboost.domain.controller;

import kcs.funding.fundingboost.domain.dto.common.CommonSuccessDto;
import kcs.funding.fundingboost.domain.dto.global.ResponseDto;
import kcs.funding.fundingboost.domain.dto.request.FriendPayProcessDto;
import kcs.funding.fundingboost.domain.dto.request.PaymentDto;
import kcs.funding.fundingboost.domain.dto.response.FriendFundingPayingDto;
import kcs.funding.fundingboost.domain.dto.response.MyPayViewDto;
import kcs.funding.fundingboost.domain.service.FriendPayService;
import kcs.funding.fundingboost.domain.service.MyPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pay")
public class PayController {

    private final FriendPayService friendPayService;
    private final MyPayService myPayService;

    @GetMapping("/order")
    public ResponseDto<MyPayViewDto> myOrderPayView(@RequestParam(name = "memberId") Long memberId) {
        return ResponseDto.ok(myPayService.viewOrder(memberId));
    }

    @GetMapping("/funding")
    public ResponseDto<MyPayViewDto> myFundingPayView( @RequestParam(name = "memberId") Long memberId){
        return ResponseDto.ok(myPayService.viewFunding(memberId));
    }

    @PostMapping
    public ResponseDto<CommonSuccessDto> processMyPayment( @RequestBody PaymentDto paymentDto, @RequestParam("memberId") Long memberId){
        return ResponseDto.ok(myPayService.pay(paymentDto, memberId));
    }

    @GetMapping("/friends/{fundingId}")
    public ResponseDto<FriendFundingPayingDto> friendPayView(
        @RequestParam("memberId") Long memberId,
        @PathVariable("fundingId") Long fundingId) {
        return ResponseDto.ok(friendPayService.findFriendFundingPay(fundingId, memberId));
    }

    @PostMapping("/friends/{fundingId}")
    public ResponseDto<CommonSuccessDto> pay(
        @RequestParam("memberId") Long memberId,
        @PathVariable("fundingId") Long fundingId,
        @RequestBody FriendPayProcessDto friendPayProcessDto) {
        return ResponseDto.ok(
            friendPayService.pay(memberId, fundingId, friendPayProcessDto));
    }
}