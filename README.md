
# 프로젝트 목적
- `JUnit5` 를 이용한 은행 거래 로직 단위, 통합 테스트
- 인프런 강의를 기초로 기능과 로직을 추가해나가는 것이 목적

# 깃허브 URL

[깃허브 URL](https://github.com/valorjj/junit_bank.git)

## H2 데이터베이스 설정

- {% post_url 2023-11-11-h2 %} 참고

해당 프로젝트에서는 테스트 환경으로 간단하게 다음과 같이 설정한다. H2 데이터베이스를 `Server` 가 아니라 `Memory` 로 실행한다.

```yaml
spring:
  # h2 데이터베이스 연결
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:test;MODE=MySQL
    username: sa
    password:
  h2:
    console:
      enabled: true
```

## Model 설정

### BaseTime

```java
/**
 * {@code @MappedSuperclass}
 * - JPA Entity 클래스들이 해당 클래스에 선언된 필드를 인식하게 한다.
 * {@code @EntityListeners}
 * - AuditingEntityListener.class 가 콜백 리스너로 지정된다.
 * - Entity 에서 어떤 이벤트가 발생할 때 특정 로직을 수행한다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTime implements Serializable {

    @CreatedDate
    @Column(name="createdAt", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updatedAt", nullable = false)
    private LocalDateTime modifiedAt;
    
}
```

### User

```java
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_user")
@Getter
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    // TODO: 패스워드 인코딩
    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 20)
    private String email;

    @Column(nullable = false, length = 20)
    private String fullname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserEnum UserEnum;

    @Builder
    public User(Long id, String username, String password, String email, String fullname, com.example.banksample.domain.user.UserEnum userEnum) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        UserEnum = userEnum;
    }
}

```

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
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
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
        httpSecurity.authorizeHttpRequests(authz ->
            authz
                .requestMatchers(mvcMatcherBuilder.pattern("/api/**")).authenticated()
                // .requestMatchers("/api/**").authenticated()
                // 더 이상 ROLE_ prefix 를 사용하지 않는다..
                // .requestMatchers(mvcMatcherBuilder.pattern("/api/admin/**")).hasRole(String.valueOf(UserEnum.ADMIN))
                .anyRequest().permitAll()
        );
        /**
         * '인증' 과정 중 에러가 발생하는 상황을 인터셉트 한다.
         * -> 에러 발생 상황을 컨트롤 할 수 있다.
         * */
        httpSecurity.exceptionHandling(authz -> authz.authenticationEntryPoint((request, response, authException) -> CustomResponseUtil.unAuthenticated(response, "로그인이 필요합니다.")));
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
### 응답 유틸

인증, 인가 과정에서 에러가 발생하는 경우 사용할 에러 객체 생성


```java
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
}
```

## SecurityConfig Test 작성

`requestMacthers` 에 지정한 값에 인증, 인가 과정이 제대로 동작하는지 테스트
- `MockMvc` 사용

```java
/**
 * {@code @AutoConfigureMockMvc} 를 사용해야 {@code MockMvc} 를 주입받아서 사용할 수 있다.
 */
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void authenticate_test() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc.perform(get("/api/hello"));
        String body = resultActions.andReturn().getResponse().getContentAsString();
        int status = resultActions.andReturn().getResponse().getStatus();
        log.info("body -> {}", body);
        // then
        // 401 에러가 발생한 건지 비교
        Assertions.assertThat(status).isEqualTo(401);
    }

    @Test
    void authorize_test() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc.perform(get("/api/admin/hello"));
        String body = resultActions.andReturn().getResponse().getContentAsString();
        int status = resultActions.andReturn().getResponse().getStatus();
        log.info("body -> {}", body);
        // then
        Assertions.assertThat(status).isEqualTo(401);
    }

}

```

<img width="280" alt="image" src="https://user-images.githubusercontent.com/30681841/282288372-fe03c74f-6bbf-46da-bf1a-5bd70f732390.png">

## 회원가입 서비스 및 테스트

- `JpaRepository` 생성
- 상속받는 `CrudRepository` 에 이미 단순 CRUD 작업은 정의되어 있기에 테스트 필요 X

회원가입하는 매우 단순한 로직을 작성한다.
- DTO 를 Entity 로 변환하고
- 에러 객체를 생성했다.

```java
// 1. UserServiceImplV1.class
@Override
@Transactional
public JoinResponseDTO signUp(JoinRequestDTO joinRequestDTO) {
    // 1. 동일한 사용자 이름이 존재하는지 검사
    Optional<User> userOptional = userRepository.findByUseranme(joinRequestDTO.getUsername());
    // 사용자 이름이 중복된 경우
    if (userOptional.isPresent()) {
        throw new CustomApiException("동일한 사용자 이름이 존재합니다.");
    }
    // 2. 패스워드 인코딩 + 회원가입 진행
    User userPS = userRepository.save(joinRequestDTO.toEntity(bCryptPasswordEncoder));
    // 3. DTO 응답
    return new JoinResponseDTO(userPS);
}

// 2.
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<?> apiException(CustomApiException ex) {
        log.error("error -> {}", ex.getMessage());
        return new ResponseEntity<>(new ResponseDTO<>(-1, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

}

```

위 코드를 테스트해본다.

```java
/**
 * Mockito 환경에서 테스트 진행
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
class UserServiceTest {

    @InjectMocks // 가짜 환경을 주입받을 대상을 지정한다.
    private UserServiceImplV1 userServiceV1;

    @Mock // 메모리에 띄울 가짜 환경
    private UserRepository userRepository;

    // @Spy: 가짜 객체를 생성하는 @Mock 과 달리 스프링 컨테이너에 있는 실제 빈을 주입한다.
    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    @DisplayName("회원가입_테스트")
    void signUp_test() throws Exception {
        // given
        JoinRequestDTO joinRequestDTO = new JoinRequestDTO();
        joinRequestDTO.setUsername("jeongjin");
        joinRequestDTO.setFullname("kim jeongjin");
        joinRequestDTO.setEmail("admin@gmail.");
        joinRequestDTO.setPassword("1234");

        // stub_1
        // repository 에 대한 테스트가 아니므로, any() 를 인자로 넣는다.
        when(userRepository.findByUseranme(any())).thenReturn(Optional.empty());

        // stub_2
        // User 객체가 리턴되도록 한다.
        User user = User.builder()
            .id(1L)
            .username("jeongjin")
            .password("1234")
            .fullname("kim jeongjin")
            .role(UserEnum.CUSTOMER)
            .build();
        when(userRepository.save(any())).thenReturn(user);

        // when
        JoinResponseDTO joinResponseDTO = userServiceV1.signUp(joinRequestDTO);
        log.info("[*] joinResponseDTO -> {}", joinResponseDTO.toString());

        // then
        Assertions.assertThat(joinResponseDTO.getId()).isEqualTo(1L);
        Assertions.assertThat(joinResponseDTO.getUsername()).isEqualTo("jeongjin");
    }
}
```

<img width="815" alt="image" src="https://user-images.githubusercontent.com/30681841/282308138-2c6572b5-45be-4a91-a278-c2c952317766.png">


### @Data 무분별한 사용 주의

1. 무분별한 `@Setter` 남용 방지
2. `@ToString` 으로 인한 양방향 연관관계 시, 무한 순환 참조 예방
3. `@EqualsAndHashCode` 을 `Mutable` 한 객체에 사용하는 경우 문제 발생
    1. 동일한 객체의 필드 값을 변경시키면 `hashcode` 값이 바뀐다.

## UserService 리팩토링

매번 테스트 코드에 가짜 User 객체를 만드는 것은 비효율적이다. `Dummy` 로 사용할 다음 객체를 생성해서, 상속받아서 사용한다.

```java
public class DummyObject {

  /**
   * 엔티티를 데이터베이스에 저장할 때 사용한다.
   *
   * @param username
   * @param fullname
   * @return
   */
  protected User newUser(String username, String fullname) {
      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      String encPassword = passwordEncoder.encode("1234");
      return User.builder()
          .username(username)
          .email("mockuser@nate.com")
          .password(encPassword)
          .fullname(fullname)
          .role(UserEnum.CUSTOMER)
          .build();
  }

  /**
   * 테스트 객체에서 stub 용도로 사용한다.
   *
   * @param id
   * @param username
   * @param fullname
   * @return
   */
  protected User newMockUser(Long id, String username, String fullname) {
      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      String encPassword = passwordEncoder.encode("1234");
      return User.builder()
          .id(id)
          .username(username)
          .email("mockuser@nate.com")
          .password(encPassword)
          .fullname(fullname)
          .role(UserEnum.CUSTOMER)
          .build();
  }

}

```

테스트 코드 환경에서는 `stub` 을 위해 `newMockUser` 를 생성해서 사용하면 된다.

```java
// stub_2
User user = newMockUser(1L, "jeongjin", "kim jeongjin");
when(userRepository.save(any())).thenReturn(user);
```

## User컨트롤러

```java
@PostMapping("/signUp")
@ResponseStatus(HttpStatus.CREATED)
public ResponseEntity<?> signUp(@RequestBody JoinRequestDTO joinRequestDTO) {
    JoinResponseDTO joinResponseDTO = userServiceV1.signUp(joinRequestDTO);
    return new ResponseEntity<>(new ResponseDTO<>(1, "회원가입 완료", joinResponseDTO), HttpStatus.CREATED);
}
```
### 유효성 검사

`AOP` 를 적용한다.
- 컨트롤러에서 파라미터로 받는 DTO 에 springboot-validation 이 지원하는 유효성 검사를 위한 어노테이션을 작성한다.
- 컨트롤러 파라미터에 @Valid 어노테이션, 그리고 BindingResult 을 HashMap 에 담는다.
- `@Aspect` 클래스를 생성한다.

```java
// CustomValidationAdvice.java
/**
 * 유효성 검사는 body 가 존재하는 곳에서만 진행한다.
 */
@Component
@Aspect
public class CustomValidationAdvice {

    // PostMapping 에 관한 조인포인트
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }

    // PutMapping 에 관한 조인포인트
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {
    }


    /**
     * Advice 에 사용할 수 있는 어노테이션은 다음과 같다.
     *
     * @Before: JoinPoint 이전에 호출
     * @After: JoinPoint 이후 호출
     * @Around: JoinPoint 이전, 이후 호출
     * @AfterThrowing: JoinPoint 가 예외를 던지는 경우 호출
     * @AfterReturing: 메서드가 성공적으로 실행되는 경우 호출
     */
    @Around("postMapping() || putMapping()")
    public Object validationAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();

        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;
                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();
                    for (FieldError fieldError : bindingResult.getFieldErrors()) {
                        errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                    }
                    throw new CustomValidationException("유효성 검사에 실패했습니다.", errorMap);
                }
            }
        }
        return pjp.proceed();
    }
}

```

### CustomValidationException

`Validation` 관련 에러를 담당할 객체를 생성한다.

```java
@Getter
public class CustomValidationException extends RuntimeException {

    private Map<String, String> errorMap;

    public CustomValidationException(String message, Map<String, String> errorMap) {
        super(message);
        this.errorMap = errorMap;
    }
}
```

### CustomExceptionHandler

`CustomValidationException` 를 핸들러에 등록한다.

```java
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<?> apiException(CustomApiException ex) {
        log.error("error -> {}", ex.getMessage());
        return new ResponseEntity<>(new ResponseDTO<>(-1, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<?> apiException(CustomValidationException ex) {
        log.error("error -> {}", ex.getMessage());
        return new ResponseEntity<>(new ResponseDTO<>(-1, ex.getLocalizedMessage(), ex.getErrorMap()), HttpStatus.BAD_REQUEST);
    }

}
```

### postman 테스트

<img width="469" alt="image" src="https://user-images.githubusercontent.com/30681841/282312193-dba2a75b-2e61-48cb-8057-6ea5046e8995.png">


<img width="468" alt="image" src="https://user-images.githubusercontent.com/30681841/282312173-c2f554ef-24b2-4acc-9100-b033bfb2cac5.png">

### 정규식 추가

***정규식***을 사용해서 좀 더 세밀하게 유효성 검사를 할 수 있다. 일단, 테스트 코드를 작성해보자.

```java
@Test
@DisplayName("한글만 통과")
void only_korean_test() throws Exception {
    String value = "I am 신뢰에요";
    boolean result = Pattern.matches("/^[가-힣]+$/g", value);
    Assertions.assertThat(result).isTrue();
}
```
<img width="706" alt="image" src="https://user-images.githubusercontent.com/30681841/282312738-5f46658c-6488-4787-9db8-d85860bd6913.png">
아래와 같이 다양한 케이스를 등록해서 테스트하여, 특정 로직의 신뢰도를 증가시킬 수 있다.

```java
@Slf4j
class RegexTest {

  String value = "I am 신뢰에요 100%";

  @Test
  @DisplayName("한글만 통과")
  void only_korean_test() throws Exception {
      boolean result = Pattern.matches("^[가-힣]+$", value);
      Assertions.assertThat(result).isTrue();
  }

  @Test
  @DisplayName("한글 있으면 실패")
  void never_korean_test() throws Exception {
      boolean result = Pattern.matches("^[^가-힣]+$", value);
      Assertions.assertThat(result).isTrue();
  }

  @Test
  @DisplayName("영어만 통과")
  void only_english_test() throws Exception {
      boolean result = Pattern.matches("^[a-zA-Z]+$", value);
      Assertions.assertThat(result).isTrue();
  }

  @Test
  @DisplayName("영어 있으면 실패")
  void never_english_test() throws Exception {
      boolean result = Pattern.matches("^[^a-zA-Z]+$", value);
      Assertions.assertThat(result).isTrue();
  }

  @Test
  @DisplayName("영어와 숫자만 통과")
  void only_english_and_numbers_test() throws Exception {
      boolean result = Pattern.matches("^[a-zA-Z0-9]+$", value);
      Assertions.assertThat(result).isTrue();
  }

  @Test
  @DisplayName("영어만 통과, 단 길이는 최소2 최소4")
  void only_english_length_test() throws Exception {
      boolean result = Pattern.matches("^[a-zA-Z]{2,4}$", value);
      Assertions.assertThat(result).isTrue();
  }

  @Test
  void username_test() {
      String username = "jeongjin";
      boolean result = Pattern.matches("^\\w{2,20}$", username);
      log.info("[*] -> {}", result);
  }

  @Test
  void fullname_test() {
      String fullname = "최강jeongjin";
      boolean result = Pattern.matches("^[a-zA-Z가-힣]{2,20}$", fullname);
      log.info("[*] -> {}", result);
  }

  @Test
  void email_test() {
      String email = "admin@nate.com";
      boolean result = Pattern.matches("^\\w{2,6}@\\w{2,10}\\.[a-zA-z]{2,3}$", email);
      log.info("[*] -> {}", result);
  }
}
```

#### 정규 표현식 분리

RequestDTO 에 적용하는 정규식을 분리해서 문제가 발생할 때만 확인할 수 있도록 했다.

```java
public abstract class RegexCollection {

    private RegexCollection() {
    }

    public static final String USER_FULL_NAME = "^[a-zA-Z가-힣]{1,10}\\s[a-zA-Z가-힣]{2,20}$";
    public static final String USER_NAME = "^[a-zA-Z0-9가-힣]{1,10}$";
    public static final String USER_EMAIL = "^[a-zA-Z0-9]{2,6}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}$";

}
```
컨트롤러에서 @RequestBody 로 받는 DTO 에 유효성 검사를 위한 어노테이션을 선언한다.
- `package jakarta.validation.constraints` 에 속한 어노테이션

```java
@NotEmpty
@Pattern(regexp = RegexCollection.USER_NAME,
  message = "한글, 영문, 숫자 1~10자 이내로 작성해주세요"
)
private String username;

@NotEmpty
@Size(min = 4, max = 20)
private String password;

@NotEmpty
@Pattern(regexp = RegexCollection.USER_EMAIL,
  message = "이메일 형식이 맞지 않습니다."
)
private String email;

@NotEmpty
@Pattern(regexp = RegexCollection.USER_FULL_NAME,
  message = "한글과 영문 2~20자 이내로 작성해주세요"
)
private String fullname;
```



## User 컨트롤러 테스트

> 회원가입이 정상적으로 이루어지는 지 테스트
> 실패케이스: 이름이 중복된 경우

### 에러

```bash
Unique index or primary key violation: "PUBLIC.CONSTRAINT_INDEX_40 ON PUBLIC.TBL_USER(USERNAME NULLS FIRST) VALUES ( /* 1 */ 'bori' )"; SQL statement:
```

#### @BeforeEach 에러 발생

컨트롤러 테스트에서, 서비스를 호출하지 않고 리포지토리를 바로 호출하기 때문에 문제 발생
- 데이터 주입 전, 리포지토리 초기화

```java
// 1. before
@BeforeEach
void init() {
  inputTestData();
}

void inputTestData(){
  // Unique index or primary key violation 에러 발생
  userRepository.save(newUser("bori", "kim bori"));
}


// 2. after
void inputTestData() {
  // [해결] Unique index or primary key violation 에러 발생 
  userRepository.deleteAll();
  userRepository.save(newUser("bori", "kim bori"));
}
```

### 코드

```java
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
class UserControllerTest extends DummyObject {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper om;
	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void init() {
		inputTestData();
	}

	@Test
	@DisplayName("회원가입_성공")
	void join_success_test() throws Exception {
		// given
		JoinRequestDTO joinRequestDTO = JoinRequestDTO.builder()
			.username("jeongjin")
			.password("1234")
			.email("admin@nate.com")
			.fullname("kim jeongjin")
			.build();

		String requestBody = om.writeValueAsString(joinRequestDTO);

		// when
		ResultActions resultActions
			= mockMvc.perform(MockMvcRequestBuilders
			.post("/api/signUp")
			.content(requestBody)
			.contentType(MediaType.APPLICATION_JSON)
		);
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		log.info("[*] responseBody -> {}", responseBody);

		// then
		resultActions.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("회원가입_실패")
	void join_fail_test() throws Exception {
		// given
		JoinRequestDTO joinRequestDTO = JoinRequestDTO.builder()
			.username("bori")
			.fullname("kim bori")
			.password("1234")
			.email("admin@nate.com")
			.build();

		String requestBody = om.writeValueAsString(joinRequestDTO);

		// when
		ResultActions resultActions
			= mockMvc.perform(MockMvcRequestBuilders
			.post("/api/signUp")
			.content(requestBody)
			.contentType(MediaType.APPLICATION_JSON)
		);
		String responseBody = resultActions.andReturn().getResponse().getContentAsString();
		log.info("[*] responseBody -> {}", responseBody);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	void inputTestData() {
		// [해결] Unique index or primary key violation 에러 발생
		userRepository.deleteAll();
		userRepository.save(newUser("bori", "kim bori"));
	}
}
```

<img width="816" alt="image" src="https://user-images.githubusercontent.com/30681841/282336446-795863de-1191-4443-885a-f5d74dc9b78d.png">


## JWT 토큰 세팅

`com.auth0:java-jwt` 라이브러리를 사용한다.

```groovy
implementation 'com.auth0:java-jwt:4.4.0'
```

```xml
<dependency>
  <groupId>com.auth0</groupId>
  <artifactId>java-jwt</artifactId>
  <version>4.4.0</version>
</dependency>
```

### JwtAuthenticationFilter

```java
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
		// default 로 지정되어 있는 POST '/login' 의 url 을 변경한다.
		setFilterProcessesUrl("/api/login");
		this.authenticationManager = authenticationManager;
	}

	// POST /api/login
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		log.info("[*] attemptAuthentication 호출되었습니다.");
		try {
			ObjectMapper om = new ObjectMapper();
			LoginRequestDTO loginRequestDTO = om.readValue(request.getInputStream(), LoginRequestDTO.class);

			// 강제 로그인
			UsernamePasswordAuthenticationToken authenticationToken
				= new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());

			/*
			 * 아래 authenticate 메서드는 UserDetailsService 의 loadUserByUsername 을 호출한다.
			 * 세션을 강제 생성하는 이유는 jwt 를 사용하는 경우에도, 컨트롤러 진입 시점에
			 * 시큐리티 설정의 authorizeHttpRequest 의 도움을 받는 것이 편하기 때문이다.
			 * 강제 로그인으로 인한 세션의 생명 주기는 짧기 때문에 걱정 할 필요가 없다.
			 * request 시 생성되며, response 시 사라진다.
			 * */
			return authenticationManager.authenticate(authenticationToken);
		}
		// 시큐리티 로그인 과정 중 에러가 발생한 경우
		catch (Exception e) {
			log.error("error -> {}", e.getMessage());
			// 해당 에러 발생 시,
			// unsuccessfulAuthentication 메서드가 실행된다.
			throw new InternalAuthenticationServiceException(e.getMessage());
		}
	}

	/**
	 * InternalAuthenticationServiceException 에러 발생 시,
	 * 해당 메서드가 실행된다.
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
		CustomResponseUtil.authFailed(response, "로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED);
	}

	/**
	 * 인증 과정 중, {@code attemptAuthentication} 메서드를 통과하면
	 * 해당 메서드가 호출된다.
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		log.info("[*] successfulAuthentication 호출되었습니다.");

		// 로그인 처리 된 유저 객체를 가져온다.
		LoginUser loginUser = (LoginUser) authResult.getPrincipal();
		// 유저 객체의 정보를 통해 jwt 토큰을 생성한다.
		String jwtToken = JwtProcess.createToken(loginUser);
		// 생성한 토큰을 응답 헤더에 추가한다.
		response.addHeader(JwtTokenVO.TOKEN_HEADER, jwtToken);
		LoginResponseDTO loginResponseDTO = new LoginResponseDTO(loginUser.getUser());

		CustomResponseUtil.loginSuccess(response, loginResponseDTO);
	}

}
```

### JwtAuthorizationFilter

```java
/**
 * 토큰을 검증하는 역할을 맡는다.
 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		// 토큰이 존재하는 경우
		if (isHeaderValid(request, response)) {
			// 헤더에서 토큰을 추출한다.
			String token = request.getHeader(JwtTokenVO.TOKEN_HEADER).replace(JwtTokenVO.TOKEN_PREFIX, "");
			LoginUser loginUser = JwtProcess.verifyToken(token);
			/*
			 * 여기까지 온 경우, 해당 유저는 인증이 된 상태이다.
			 * 임시로 세션을 생성하기 위해 UsernamePasswordAuthenticationToken 객체를 생성한다.
			 * 단, 생성자마다 파라미터가 다르기 때문에 주의가 필요하다.
			 * 그리고 중요한 것은 authorities 이다. authorizeHttpRequest 에 설정한
			 * 여러 조건들을 통과하는지 여부가 중요하기 때문이다.
			 * */
			Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
			// 생성한 세션을 컨텍스트에 주입한다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		// doFilter 를 조건문 안에 넣는 실수를 조심하자. 해당 메서드는 반드시 실행되어야 한다.
		chain.doFilter(request, response);
	}

	/**
	 * 토큰 헤더가 {@code Authorization: Bearer ...} 형식이 맞는지 검사한다.
	 */
	public boolean isHeaderValid(HttpServletRequest request, HttpServletResponse response) {
		String header = request.getHeader(JwtTokenVO.TOKEN_HEADER);
		return header != null && header.startsWith(JwtTokenVO.TOKEN_PREFIX);
	}
}
```

### JwtProcess

`com.auth0:java-jwt` 라이브러리는 jwt 생성, 검증을 손쉽게 하기 위한 강력한 기능을 제공한다. `io.jsonwebtoken` 라이브러리 사용 시, 검증을 위해서 추가해야 했던 여러 검증 로직이 내장되어 있다.

```java
@Slf4j
public class JwtProcess {

	private JwtProcess() {

	}

	public static String createToken(LoginUser loginUser) {
		String jwtToken = JWT.create()
			// 토큰의 이름
			.withSubject("junit-bank-jwt")
			.withIssuer("local")
			.withExpiresAt(new Date(System.currentTimeMillis() + JwtTokenVO.TOKEN_EXP_TIME))
			.withClaim("id", loginUser.getUser().getId())
			.withClaim("role", String.valueOf(loginUser.getUser().getRole()))
			.sign(Algorithm.HMAC512(JwtTokenVO.TOKEN_SECRET));

		log.info("[*] jwtToken -> {}", jwtToken);
		return JwtTokenVO.TOKEN_PREFIX + jwtToken;
	}

	/**
	 * 토큰을 검증한다.
	 * 생성과 검증을 한 곳에서 하기 때문에 대칭키 알고리즘으로 간단하게 구현한다.
	 * 토큰 검증에 성공 시 LoginUser 객체를 반환하고, 해당 객체를
	 * 시큐리티 세션에 직접 주입시킨다.
	 */
	public static LoginUser verifyToken(String token) {
		DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtTokenVO.TOKEN_SECRET))
			.withIssuer("local")
			.build()
			.verify(token);

		Long id = decodedJWT.getClaim("id").asLong();
		String role = decodedJWT.getClaim("role").asString();
		User user = User.builder().id(id).role(UserEnum.valueOf(role)).build();
		return new LoginUser(user);
	}

}
```
JWT 뿐만 아니라 민감한 정보 관리를 위해서 `resources/env/env.yml` 에서 환경변수를 관리해보자.

```yaml
jwt:
  secret: "JWT-SECRET"
  exp_time: 90000000
  token_prefix: "Bearer "
  header: "Authorization"
```

위 정보를 읽기 위해서는 설정을 해주어야 한다.
```java
// 1. PropertySourceFactory 이용해 빈으로 등록
public class EnvConfig implements PropertySourceFactory {

	@Override
	public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
		YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
		factoryBean.setResources(resource.getResource());
		Properties properties = factoryBean.getObject();
		assert properties != null;
		return new PropertiesPropertySource(Objects.requireNonNull(resource.getResource().getFilename()), properties);
	}
	
}

// 2. 컴파일 시점에 설정 파일 클래스패스에 등록하기
@SpringBootApplication
@EnableJpaAuditing
@Slf4j
@PropertySource(value = {
  "classpath:env/env.yml"
  }, factory = EnvConfig.class)
public class BankSampleApplication {
  // ...
}
```

자주 사용되는 변수는 static 하게 선언해주어야 하는데, 아래와 같이 사용하면 ***null*** 값이 들어간다.

```java
@Value("${jwt.token}")
private static String TOKEN;
```

따라서, 조금 번거롭지만 static 변수로 선언하기 위해서는 `@Component` 로 등록하는 과정이 필요하다.

```java
@Component
public class JwtTokenVO {

	public static Integer TOKEN_EXP_TIME;
	public static String TOKEN_SECRET;
	public static String TOKEN_PREFIX;
	public static String TOKEN_HEADER;

	@Value("${jwt.exp_time}")
	public void setTokenExpTime(Integer TOKEN_EXP_TIME) {
		this.TOKEN_EXP_TIME = TOKEN_EXP_TIME;
	}

	@Value("${jwt.secret}")
	public void setTokenSecret(String TOKEN_SECRET) {
		this.TOKEN_SECRET = TOKEN_SECRET;
	}

	@Value("${jwt.token_prefix}")
	public void setTokenPrefix(String TOKEN_PREFIX) {
		this.TOKEN_PREFIX = TOKEN_PREFIX;
	}

	@Value("${jwt.header}")
	public void setTokenHeader(String TOKEN_HEADER) {
		this.TOKEN_HEADER = TOKEN_HEADER;
	}


}

```

### Security Config 에 필터 등록

`AbstractHttpConfigurer` 를 사용해서 필터를 등록하는 객체를 생성 후, 필터 체인에 등록한다.

```java
// 1. 필터를 등록하는 클래스
public class SecurityFilterManager extends AbstractHttpConfigurer<SecurityFilterManager, HttpSecurity> {

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
		builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
		builder.addFilter(new JwtAuthorizationFilter(authenticationManager));
		super.configure(builder);
	}

}

// 2.
// 사용자가 정의한 커스텀 필터를 등록한다.
httpSecurity.apply(new SecurityFilterManager());
```

### 에러 핸들링

인증, 인가 과정에 문제가 발생하는 경우 각각에 해당하는 에러 객체를 생성해서 항상 동일한 형태의 응답이 전달되도록 한다.

```java
/*
* 인증 및 인과 과정 중 에러가 발생하는 상황에 커스텀 에러 객체를 리턴시킨다.
* */
httpSecurity.exceptionHandling(authz -> authz
  .authenticationEntryPoint((req, res, ex) -> CustomResponseUtil.authFailed(res, "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED))
  .accessDeniedHandler((req, res, ex) -> CustomResponseUtil.authFailed(res, "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN))
);
```

관련 util 클래스를 만들어 재사용성을 높인다 😁

```java
public static void authFailed(HttpServletResponse response, String message, HttpStatus statusCode) {

  /**
   * 파싱 관련 에러가 나면, 여기선 할 수 있는게 없다.
   * */
  try {
    ObjectMapper objectMapper = new ObjectMapper();
    ResponseDTO<?> responseDTO = new ResponseDTO<>(-1, message, null);
    String responseBody = objectMapper.writeValueAsString(responseDTO);

    response.setContentType("application/json; charset=utf-8");
    response.setStatus(statusCode.value());
    response.getWriter().println(responseBody);
  } catch (Exception e) {
    log.error("[에러] -> {}", e.getMessage());
  }

}
```

### postman 테스트

<img width="644" alt="image" src="https://user-images.githubusercontent.com/30681841/282413316-ace702bd-af9c-4d5d-9870-1052064f4509.png">

<img width="501" alt="image" src="https://user-images.githubusercontent.com/30681841/282413443-c4f3c814-9f87-44f7-aca9-b2ca5c0d8e54.png">

생성된 토큰을 살펴보자.

```java
// 토큰 생성
String jwtToken = JWT.create()
  // 토큰의 이름
  .withSubject("junit-bank-jwt")
  .withIssuer("local")
  .withExpiresAt(new Date(System.currentTimeMillis() + JwtTokenVO.TOKEN_EXP_TIME))
  .withClaim("id", loginUser.getUser().getId())
  .withClaim("role", String.valueOf(loginUser.getUser().getRole()))
  .sign(Algorithm.HMAC512(JwtTokenVO.TOKEN_SECRET));

log.info("[*] jwtToken -> {}", jwtToken);
```

`jwt.io` 사이트에서 토큰 확인

<img width="903" alt="image" src="https://user-images.githubusercontent.com/30681841/282413939-4c9946a2-1d67-4d21-8195-4c682e8b3e31.png">



---

## 출처
1. [https://www.nowwatersblog.com/springboot/springstudy/lombok](https://www.nowwatersblog.com/springboot/springstudy/lombok)