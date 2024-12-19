# Auth Repository
Dev_Bridge 인증 서버  
Java 17
### 1. 개요
- Dev_Bridge 프로젝트의 모든 인증 과정을 담당한다.
- JWT 발급 및 검증, 사용자 정보 관리, 인증 및 권한 부여 등의 기능을 수행한다.



### 2. 기본 구조
- 설정 파일 `application.yml` 에 각 서비스의 라우팅 정보와 해당 라우팅에 활용할 Custom Filter를 정의한다.


### 3. Filter 정보
- GlobalFilter: 모든 요청에 대해 실행되며 모든 요청에 대한 모니터링을 수행한다.
    - PreFilter: 요청이 서비스로 전달되기 전에 실행되며 요청에 대한 로깅을 수행한다.
    - PostFilter: 서비스로부터 응답이 클라이언트로 전달되기 전에 실행되며 응답에 대한 로깅을 수행한다.
- AuthorizationFilter: 인증이 필요한 Route에게 부여한다.
    - 해당 필터가 적용된 Route는 요청 전송 이전에, Auth Service로 요청이 프록시되어 요청 헤더의 토큰 검증 과정을 거친다.
    - 그 과정에서 요청 Header에 토큰에 담긴 Username, Id 등의 정보를 추가해 활용한다.
- CustomFilter: 필요한 서비스에 추가적인 필터를 적용할 수 있다.

| 수정자 | 수정 날짜    | 수정내용  |
|-----|----------|-------|
| 김승원 | 24-12-19 | 최초 작성 |  
