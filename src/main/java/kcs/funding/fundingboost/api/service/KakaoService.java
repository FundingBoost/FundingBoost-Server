package kcs.funding.fundingboost.api.service;

import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpSession;
import kcs.funding.fundingboost.api.common.Const;
import kcs.funding.fundingboost.api.transformer.Trans;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;

@RequiredArgsConstructor
@Service
public class KakaoService {

    private final HttpSession httpSession;
    
    private final HttpCallService httpCallService;

    public static String token;

    @Value("${rest-api-key}")
    private String REST_API_KEY;

    @Value("${redirect-uri}")
    private String REDIRECT_URI;

    @Value("${authorize-uri}")
    private String AUTHORIZE_URI;

    @Value("${token-uri}")
    public String TOKEN_URI;

    @Value("${client-secret}")
    private String CLIENT_SECRET;

    @Value("${kakao-api-host}")
    private String KAKAO_API_HOST;


    public RedirectView goKakaoOAuth() {
        String uri = AUTHORIZE_URI + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + REST_API_KEY;
        if (!"".isEmpty()) {
            uri += "&scope=" + "";
        }
        return new RedirectView(uri);
    }

    public RedirectView loginCallback(String code) {
        String param = "grant_type=authorization_code&client_id=" + REST_API_KEY + "&redirect_uri=" + REDIRECT_URI
                + "&client_secret=" + CLIENT_SECRET + "&code=" + code;
        String rtn = httpCallService.Call(Const.POST, TOKEN_URI, Const.EMPTY, param);
        token = Trans.token(rtn, new JsonParser());
        httpSession.setAttribute("token", token);

        return new RedirectView("/index.html");
    }

    public String getProfile() {
        String uri = KAKAO_API_HOST + "/v2/user/me";
        return httpCallService.CallwithToken(Const.GET, uri, httpSession.getAttribute("token").toString());
    }

    public String getFriends() {
        String uri = KAKAO_API_HOST + "/v1/api/talk/friends";
        return httpCallService.CallwithToken(Const.GET, uri, httpSession.getAttribute("token").toString());
    }

    public String logout() {
        String uri = KAKAO_API_HOST + "/v1/user/logout";
        return httpCallService.CallwithToken(Const.POST, uri, httpSession.getAttribute("token").toString());
    }
}
