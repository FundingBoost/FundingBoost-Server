package kcs.funding.fundingboost.api.dto;


public record DefaultMessageDto(String objType, String text, String webUrl, String btnTitle) {
    public static DefaultMessageDto createDefaultMessageDto(String objType, String text, String webUrl, String btn) {
        return new DefaultMessageDto(objType, text, webUrl, btn);
    }
}
