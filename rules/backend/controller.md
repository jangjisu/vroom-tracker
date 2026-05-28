# Controller 수정 시 확인 규칙

## ApiResponse 구조

```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": { ... }
}
```

## ResponseCode 정의

| code | HTTP Status | 의미 |
|------|-------------|------|
| `SUCCESS` | 200 | 정상 처리 |
| `INVALID_PARAMETER` | 400 | 요청 파라미터 검증 실패 |
| `NOT_FOUND` | 404 | 리소스 없음 |
| `EXTERNAL_API_UNAVAILABLE` | 200 | upstream API 일시 불가, 서버 자체는 정상 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |

> `EXTERNAL_API_UNAVAILABLE` 은 HTTP 200을 반환한다.
> 서버는 정상이고, 데이터 조회 실패는 `code` 필드로 표현한다.

## GlobalExceptionHandler

`@RestControllerAdvice` (GlobalExceptionHandler) 가 모든 미처리 예외를 잡아 `INTERNAL_ERROR` 로 응답한다.
