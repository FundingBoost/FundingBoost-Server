package kcs.funding.fundingboost.api.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import kcs.funding.fundingboost.api.dto.DefaultMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class MessageService extends HttpCallService {
    private static final String MSG_SEND_TO_ME_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";
    private static final String MSG_SEND_TO_FRIENDS_URL = "https://kapi.kakao.com/v1/api/talk/friends/message/default/send";
    private static final String SEND_SUCCESS_MSG = "메시지 전송에 성공했습니다.";
    private static final String SEND_FAIL_MSG = "메시지 전송에 실패했습니다.";

    private static final String SUCCESS_CODE = "0"; //kakao api에서 return해주는 success code 값

    public CompletableFuture<Boolean> sendMessageToMe(String accessToken, DefaultMessageDto msgDto) {
        MultiValueMap<String, String> parameters = createMessageParameters(msgDto, null);
        return sendMessage(MSG_SEND_TO_ME_URL, accessToken, parameters);
    }

    public CompletableFuture<Boolean> sendMessageToFriends(String accessToken, DefaultMessageDto msgDto,
                                                           List<String> uuids) {
        JSONArray receiverUuids = new JSONArray(uuids);
        MultiValueMap<String, String> parameters = createMessageParameters(msgDto, receiverUuids.toString());
        return sendMessage(MSG_SEND_TO_FRIENDS_URL, accessToken, parameters);
    }

    private MultiValueMap<String, String> createMessageParameters(DefaultMessageDto msgDto, String receiverUuids) {
        JSONObject templateObj = createTemplateObject(msgDto);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("template_object", templateObj.toString());
        if (receiverUuids != null) {
            parameters.add("receiver_uuids", receiverUuids);
        }
        return parameters;
    }

    private CompletableFuture<Boolean> sendMessage(String url, String accessToken,
                                                   MultiValueMap<String, String> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response;
            try {
                response = restTemplate.postForEntity(url, requestEntity, String.class);
                return processResponse(response.getBody());
            } catch (Exception e) {
                // 로그 기록, 에러 처리
                return false;
            }
        });
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private JSONObject createTemplateObject(DefaultMessageDto msgDto) {
        JSONObject linkObj = new JSONObject();
        linkObj.put("web_url", msgDto.webUrl());
        JSONObject templateObj = new JSONObject();
        templateObj.put("object_type", msgDto.objType());
        templateObj.put("text", msgDto.text());
        templateObj.put("link", linkObj);
        templateObj.put("button_title", msgDto.btnTitle());
        return templateObj;
    }

    private boolean processResponse(String responseBody) {
        JSONObject jsonData = new JSONObject(responseBody);
        String resultCode = jsonData.optString("result_code", "");
        if (SUCCESS_CODE.equals(resultCode) || !jsonData.optString("successful_receiver_uuids", "").isEmpty()) {
            log.info(SEND_SUCCESS_MSG);
            return true;
        } else {
            log.debug(SEND_FAIL_MSG);
            return false;
        }
    }
}