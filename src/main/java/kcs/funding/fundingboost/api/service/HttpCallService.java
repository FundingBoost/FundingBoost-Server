package kcs.funding.fundingboost.api.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpCallService {
    public static String accessToken;
    
    public String CallwithToken(String method, String reqURL, String access_Token) {
        String header = "Bearer " + access_Token;
        accessToken = access_Token;
        return Call(method, reqURL, header, null);
    }

    public String Call(String method, String reqURL, String header, String param) {
        String result = "";
        try {
            String response = "";
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Authorization", header);
            if (param != null) {
                System.out.println("param : " + param);
                conn.setDoOutput(true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                bw.write(param);
                bw.flush();

            }
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            System.out.println("reqURL : " + reqURL);
            System.out.println("method : " + method);
            System.out.println("Authorization : " + header);
            InputStream stream = conn.getErrorStream();
            if (stream != null) {
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    response = scanner.next();
                }
                System.out.println("error response : " + response);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            br.close();
        } catch (IOException e) {
            return e.getMessage();
        }
        return result;
    }

    /**
     * Http 요청 클라이언트 객체 생성 method
     *
     * @ param Map<String,String> header HttpHeader 정보
     * @ param Object params HttpBody 정보
     * @ return HttpEntity 생성된 HttpClient객체 정보 반환
     * @ exception 예외사항
     */
    public HttpEntity<?> httpClientEntity(HttpHeaders header, Object params) {
        HttpHeaders requestHeaders = header;

        if (params == null || "".equals(params)) {
            return new HttpEntity<>(requestHeaders);
        } else {
            return new HttpEntity<>(params, requestHeaders);
        }
    }

    /**
     * Http 요청 method
     *
     * @ param String url 요청 URL 정보
     * @ param HttpMethod method 요청 Method 정보
     * @ param  HttpEntity<?> entity 요청 EntityClient 객체 정보
     * @ return HttpEntity 생성된 HttpClient객체 정보 반환
     */
    public ResponseEntity<String> httpRequest(String url, HttpMethod method, HttpEntity<?> entity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(url, method, entity, String.class);
    }
}
