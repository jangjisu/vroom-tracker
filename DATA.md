# 데이터 방향

이 문서는 외부 데이터의 저장 방식, 테이블 관계와 연결 키를 기록한다.
외부 API별 요청과 응답 실측 명세는 `API.md`에서 관리한다.

## 현재 상태

- 한국도로공사 OpenAPI 응답을 API별 Entity와 테이블에 저장한다.
- 호출 결과 코드, 메시지, 페이지 번호 같은 전송 메타데이터는 영속 데이터에서 제외한다.
- 여러 테이블의 데이터는 조회 시 Repository에서 가져와 응답 DTO에서 조합한다.

## 합의된 목표

- 외부 API의 원본 필드를 가능한 한 유지해 API 응답과 저장 데이터를 쉽게 비교한다.
- 화면 요구에 맞춰 원본 테이블을 성급하게 하나의 내부 모델로 통합하지 않는다.
- 새로운 외부 API는 독립된 응답 객체, Entity와 테이블로 저장한다.
- 문자열 숫자와 `O/X` 같은 원본 표현은 특별한 이유가 없다면 저장 단계에서 임의 변환하지 않는다.
- 합산, null 처리와 화면용 표현 변환은 조회 응답 DTO가 담당한다.
- 데이터 연결은 실제 응답으로 검증된 식별자만 사용한다.

## 데이터 연결 기록

새 API를 추가하거나 조회 데이터를 조합할 때 아래 형식으로 연결 관계를 기록한다.

| 기준 데이터 | 연결 데이터 | 연결 조건 | 검증 상태 |
|---|---|---|---|
| `rest_stop` | `rest_stop_detail` | `service_area_code = service_area_code` | 코드 적용, 실측 기록 필요 |
| `rest_stop` | `highway_service_area_info` | `service_area_code = business_facility_code` | 코드 적용, 실측 기록 필요 |
| `rest_stop` | `rest_oil` | `route_no = route_code` + 시설명에서 `휴게소`/`주유소`와 공백을 제거한 정규화 이름 일치 | 2026-06-15 실측 |
| `rest_oil` | `rest_oil_price` | `standard_rest_code = service_area_code2` | 2026-06-16 실측 |
| `rest_stop` | `rest_food` | `std_rest_cd = std_rest_cd` | 2026-06-16 실측 |

`rest_stop.std_rest_cd`와 `rest_oil.standard_rest_code`는 같은 장소의 휴게소와 주유소에도
서로 다른 시설 코드가 발급된다. 서울만남(부산)의 경우 각각 `000001`, `000002`이며,
실측한 휴게소 203건과 주유소 데이터 사이에 동일 코드 매칭은 0건이었다.

`rest_oil`은 동기화 시 `normalized_station_name`을 계산해 저장하고
`(route_code, normalized_station_name)` 일반 복합 인덱스로 상세 조회한다.
한 주유소에 여러 편의시설 행이 존재하므로 이 인덱스는 unique가 아니다.

`rest_oil_price.service_area_code2`는 주유소 코드이며 `rest_oil.standard_rest_code`와 연결된다.
서울만남(부산)주유소는 두 API에서 모두 `000002`로 내려오는 것을 확인했다.
가격 데이터는 상세 조회마다 외부 API를 호출하지 않고 3시간마다 `curStateStation` 1~3페이지를
동기화해 저장한다.

휴게소 상세 응답은 `rest_oil`에서 조회한 첫 번째 `standard_rest_code`로 `rest_oil_price`를
조회해 `oilInfo`에 가격 정보를 포함한다. `oilInfo.oilStationConveniences`는 같은 `rest_oil`
조회 결과를 변환하며, 가격 데이터가 없어도 `oilInfo`와 편의시설 배열은 유지한다.
단건 가격 갱신 API도 같은 연결 키를 사용하며, upstream에서 단건 결과가 내려오면
`rest_oil_price.service_area_code2` 기준으로 update 또는 insert한다.
`rest_oil_price.last_refreshed_at`은 가격 row가 마지막으로 갱신된 시각이다. 단건 가격 갱신 API는
이 값이 현재 시각 기준 10분 이내면 외부 API 호출 없이 저장값을 반환한다.

오피넷 전국 평균 유가는 `national_oil_price` 테이블에 일별·유종별로 저장한다.
연결 키는 `trade_date + product_code`이며, 같은 거래일의 데이터는 재조회 시 교체 저장한다.
경로 결과 조회에서 오늘 평균가가 없으면 오피넷 `avgAllPrice.do`를 호출하고, 필수 유종
휘발유(`B027`), 자동차용경유(`D047`), 자동차용부탄(`K015`)이 모두 있을 때만
`nationalOilPriceSummary`와 휴게소별 평균 대비 차이값을 응답에 포함한다.
오피넷 평균가는 휴게소 상세 응답에는 연결하지 않는다.

휴게소 음식 메뉴(`restBestfoodList`) 연결은 2026-06-16 실측에서 `restBestfoodList.stdRestCd`가
`rest_stop.std_rest_cd`와 직접 일치함을 확인했다. 서울만남(부산)휴게소가 양쪽 모두 `000001`이며,
주유소(`rest_oil`)와 달리 정규화 이름 매칭 없이 `std_rest_cd`로 바로 조인한다.
음식 API 전용 코드 `restCd`(`S000001`)는 연결에 사용하지 않는다.

`rest_stop_detail`과 `highway_service_area_info` 연결은 현재 상세 조회에 적용되어 있지만,
대표 표본과 전체 일치율을 확인한 실측 기록은 아직 없다. 연결 조건을 변경하기 전에 실제 응답으로 검증한다.

경로 탐색 결과는 별도 테이블에 저장하지 않는다. 저장된 `rest_stop` 좌표를 카카오 길찾기 경로와
비교하고, 기본 반경 1km 안의 휴게소를 경로 순서대로 계산해 응답한다.

## 갱신 원칙

- 정기 갱신 데이터는 API별 동기화 책임을 분리한다.
- 외부 API 호출과 DB 쓰기 트랜잭션을 한 범위에 묶지 않는다.
- 전체 교체, 부분 갱신과 실패 시 기존 데이터 보존 여부는 API별 스펙에서 결정한다.
- 전체 교체 방식의 동기화는 모든 페이지 조회가 끝난 뒤 트랜잭션 안에서 기존 데이터를 교체한다.
- 휴게소 위치·상세·영업시설·주유소 편의시설·먹거리는 매일, 주유 가격은 3시간마다 갱신한다.
- 서버 시작 시 주요 테이블이 비어 있을 때만 초기 동기화를 실행한다.

## 미결정 사항

- 외부 API별 데이터 보존 기간
- API 간 식별자가 일치하지 않을 때 사용할 별도 매핑 테이블
- 원본 응답 자체를 별도 보관할 필요가 있는지 여부
