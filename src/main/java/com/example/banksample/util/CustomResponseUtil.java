package com.example.banksample.util;

import com.example.banksample.dto.ResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomResponseUtil {

    private CustomResponseUtil() {
    }

    public static void unAuthenticated(HttpServletResponse response, String message)  {

        /**
         * 파싱 관련 에러가 나면, 여기선 할 수 있는게 없다.
         * */
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ResponseDTO<?> responseDTO = new ResponseDTO<>(-1, message, null);
            String responseBody = objectMapper.writeValueAsString(responseDTO);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(401);
            response.getWriter().println(responseBody);
        }
        // TODO: Logback.xml 파일 설정해서 에러 상황 파일로 남기기
        catch (Exception e) {
            log.error("[에러] -> {}", e.getMessage());
        }

    }

    public static void unAuthorized() {

    }
}
