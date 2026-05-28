# 프론트엔드 수정 시 확인 규칙

## JS 파일 구조

JS는 역할에 따라 파일을 분리하고 ES module (`import`/`export`) 로 연결한다.

```
app.js          ← 진입점. import만 하고 DOMContentLoaded에서 초기화 조율
├── {feature}.js ← 특정 Controller(/api/*)와 1:1 대응하는 fetch·render 로직
├── {page}.js   ← 해당 HTML 페이지 전용 UI 로직
└── utils.js    ← setText, showEl 등 공통 DOM 유틸
```

- `app.js` 는 직접 DOM을 다루지 않는다. import 후 초기화 함수 호출만 한다
- HTML에 인라인 `<script>` 또는 `onclick`·`oninput` 이벤트 속성을 작성하지 않는다
- `<script>` 태그는 `type="module"` 을 사용한다
- 이벤트 핸들러는 `addEventListener` 로 등록한다

## 비동기 API 처리 원칙

- 각 섹션이 독립적으로 로딩·에러 상태를 가진다
- 특정 섹션의 실패가 다른 섹션 렌더링을 막지 않는다

## EXTERNAL_API_UNAVAILABLE 처리

서버가 `code: "EXTERNAL_API_UNAVAILABLE"` 을 반환하면 해당 섹션에서 공통 alert를 표시한다.

```js
const body = await res.json();
if (body.code === 'EXTERNAL_API_UNAVAILABLE') {
    showApiUnavailableAlert();
    return;
}
const data = body.data;
```

- `utils.js` 에 `showApiUnavailableAlert()` 함수를 정의한다
- 메시지: "일시적으로 데이터를 가져오지 못했습니다. 잠시 후 다시 시도해주세요."

## Properties 파일 주석

`.properties` 파일의 주석은 영어로만 작성한다.
한글은 인코딩 설정에 따라 깨질 수 있다.
