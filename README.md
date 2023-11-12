## SecurityConfig 설정
```java
@Configuration
@Slf4j
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.info("[디버그] BCryptPasswordEncoder 빈을 등록합니다.");
        return new BCryptPasswordEncoder();
    }
    /**
     * JWT 필터 등록
     * */

    /**
     * Session 을 사용하지 않고 JWT 를 사용한다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector).servletPath("/");

        // iframe 을 허용하지 않는다.
        httpSecurity.headers(authz -> authz.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        // csrf 가 작동하면 postman api 테스트를 할 수 없다.
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        //
        httpSecurity.cors(authz -> authz.configurationSource(configurationSource()));
        // JSessionID 를 서버에서 관리하지 않는다.
        httpSecurity.sessionManagement(authz -> authz.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // React 와 같은 클라이언트로 요청한다.
        httpSecurity.formLogin(AbstractHttpConfigurer::disable);
        // httpBasic 은 브라우저가 팝업창을 이용해서 사용자 인증을 진행하는데, 허용하지 않는다.
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable);
        // 서버로 요청을 받는 URL 패턴을 검사한다.
        httpSecurity.authorizeHttpRequests((authz) ->
            authz
                .requestMatchers(mvcMatcherBuilder.pattern("/api/**")).authenticated()
                // 더 이상 ROLE_ prefix 를 사용하지 않는다..
                .requestMatchers(mvcMatcherBuilder.pattern("/api/admin/**")).hasRole(String.valueOf(UserEnum.ADMIN))
                .anyRequest().permitAll()
        );
        return httpSecurity.build();
    }

    public CorsConfigurationSource configurationSource() {
        log.info("[디버그] CorsConfigurationSource 가 SecurityFilterChain 에 등록합니다.");
        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 HTTP 헤더를 허용한다.
        configuration.addAllowedHeader("*");
        // GET, POST, PUT, DELETE, OPTIONS 를 허용한다.
        configuration.addAllowedMethod("*");
        // 모든 IP 주소를 허용한다.
        // TODO: 배포 시, 프론트엔드 IP 만 추가하는 걸로 수정
        configuration.addAllowedOriginPattern("*");
        // 클라이언트에서 쿠키 요청 허용
        configuration.setAllowCredentials(true);

        /**
         * 모든 엔드포인트는 도메인 + '/' 로 시작하기 때문에,
         * '/**' 에 cors 관련 설정을 추가한다.
         * */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

```
## SecurityConfig 테스트
```java
// Mock 환경에서의 통합 테스트
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityConfigTest {

    @Test
    void authenticate_test() throws Exception {
        // given
        // when
        // then
    }

    @Test
    void authorize_test() throws Exception {
        // given
        // when
        // then
    }

}
```