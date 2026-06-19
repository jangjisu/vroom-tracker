package com.vroomtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vroomtracker.client.KakaoMapClient;
import com.vroomtracker.client.response.KakaoDirectionsResponse;
import com.vroomtracker.client.response.KakaoDirectionsResponse.Road;
import com.vroomtracker.client.response.KakaoDirectionsResponse.Route;
import com.vroomtracker.client.response.KakaoDirectionsResponse.Section;
import com.vroomtracker.client.response.KakaoDirectionsResponse.Summary;
import com.vroomtracker.client.response.KakaoLocalSearchResponse;
import com.vroomtracker.client.response.KakaoLocalSearchResponse.Document;
import com.vroomtracker.controller.response.RouteRestStopResponse;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.RestStopRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RouteRestStopServiceTest {

    private static final List<Double> VERTEXES = List.of(127.0, 37.0, 127.5, 37.5, 128.0, 38.0);

    @Mock
    private KakaoMapClient kakaoMapClient;

    @Mock
    private RestStopRepository restStopRepository;

    private RouteRestStopService service;

    @BeforeEach
    void setUp() {
        service = new RouteRestStopService(kakaoMapClient, restStopRepository);
    }

    private KakaoLocalSearchResponse searchResult(String x, String y, String placeName, String addressName) {
        return new KakaoLocalSearchResponse(List.of(new Document(x, y, placeName, addressName)));
    }

    private KakaoDirectionsResponse directions(int code, Summary summary, List<Double> vertexes) {
        Route route = new Route(code, summary, List.of(new Section(List.of(new Road(vertexes)))));
        return new KakaoDirectionsResponse(List.of(route));
    }

    private RestStopEntity restStop(String code, String name, String route, String lng, String lat) {
        RestStopEntity entity = mock(RestStopEntity.class);
        lenient().when(entity.getServiceAreaCode()).thenReturn(code);
        lenient().when(entity.getUnitName()).thenReturn(name);
        lenient().when(entity.getRouteName()).thenReturn(route);
        lenient().when(entity.getXValue()).thenReturn(lng);
        lenient().when(entity.getYValue()).thenReturn(lat);
        return entity;
    }

    @Test
    @DisplayName("목적지 검색 결과가 없으면 NotFound (빈 리스트/ null 모두)")
    void emptySearch_throwsNotFound() {
        when(kakaoMapClient.searchKeyword("없는곳")).thenReturn(new KakaoLocalSearchResponse(List.of()));
        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "없는곳", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);

        when(kakaoMapClient.searchKeyword("널")).thenReturn(new KakaoLocalSearchResponse(null));
        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "널", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);
    }

    @Test
    @DisplayName("목적지 좌표를 해석하지 못하면 NotFound")
    void unparsableDestination_throwsNotFound() {
        when(kakaoMapClient.searchKeyword("경도없음")).thenReturn(searchResult(null, "35.0", "곳", null));
        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "경도없음", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);

        when(kakaoMapClient.searchKeyword("위도없음")).thenReturn(searchResult("129.0", null, "곳", null));
        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "위도없음", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);
    }

    @Test
    @DisplayName("길찾기에 성공 경로가 없으면 NotFound (result_code!=0, routes 비어있음/null)")
    void noSuccessfulRoute_throwsNotFound() {
        when(kakaoMapClient.searchKeyword(anyString())).thenReturn(searchResult("129.0", "35.0", "부산", null));

        when(kakaoMapClient.getDirections(anyString(), anyString())).thenReturn(directions(104, null, VERTEXES));
        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);

        when(kakaoMapClient.getDirections(anyString(), anyString())).thenReturn(new KakaoDirectionsResponse(List.of()));
        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);

        when(kakaoMapClient.getDirections(anyString(), anyString())).thenReturn(new KakaoDirectionsResponse(null));
        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);
    }

    @Test
    @DisplayName("경로 좌표가 없으면 NotFound")
    void emptyPolyline_throwsNotFound() {
        when(kakaoMapClient.searchKeyword(anyString())).thenReturn(searchResult("129.0", "35.0", "부산", null));
        when(kakaoMapClient.getDirections(anyString(), anyString())).thenReturn(directions(0, null, List.of()));

        assertThatThrownBy(() -> service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000))
                .isInstanceOf(RouteRestStopNotFoundException.class);
    }

    @Test
    @DisplayName("경로 1km 이내 휴게소만 경로 순서대로 반환하고, 잘못된 좌표는 건너뛴다")
    void success_filtersAndOrders() {
        when(kakaoMapClient.searchKeyword("부산")).thenReturn(searchResult("129.0", "35.0", "부산역", null));
        when(kakaoMapClient.getDirections("127.0,37.0", "129.0,35.0"))
                .thenReturn(directions(0, new Summary(100L, 200L), VERTEXES));

        RestStopEntity near0 = restStop("A", "A휴게소", "경부선", "127.0001", "37.0001");
        RestStopEntity near1 = restStop("B", "B휴게소", "경부선", "127.5001", "37.5001");
        RestStopEntity far = restStop("C", "C휴게소", "중부선", "130.0", "40.0");
        RestStopEntity blank = restStop("D", "D", "x", "127.0", "   ");
        RestStopEntity nonNumeric = restStop("E", "E", "x", "127.0", "abc");
        when(restStopRepository.findAll()).thenReturn(List.of(near1, near0, far, blank, nonNumeric));

        RouteRestStopResponse response = service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000);

        assertThat(response.destination().name()).isEqualTo("부산역");
        assertThat(response.destination().latitude()).isEqualTo(35.0);
        assertThat(response.route().distanceMeters()).isEqualTo(100L);
        assertThat(response.route().durationSeconds()).isEqualTo(200L);
        assertThat(response.route().path()).hasSize(3);
        assertThat(response.restStops())
                .extracting(RouteRestStopResponse.RouteRestStopItem::serviceAreaCode)
                .containsExactly("A", "B");
        assertThat(response.restStops().get(0).distanceFromRouteMeters()).isLessThan(50L);
    }

    @Test
    @DisplayName("위도 또는 경도 좌표가 없으면 결과에서 제외한다")
    void invalidRestStopCoordinates_excluded() {
        when(kakaoMapClient.searchKeyword("부산")).thenReturn(searchResult("129.0", "35.0", "부산", null));
        when(kakaoMapClient.getDirections(anyString(), anyString()))
                .thenReturn(directions(0, new Summary(1L, 1L), VERTEXES));
        RestStopEntity nullLatitude = restStop("A", "A", "x", "127.0", null);
        RestStopEntity nullLongitude = restStop("B", "B", "x", null, "37.0");
        when(restStopRepository.findAll()).thenReturn(List.of(nullLatitude, nullLongitude));

        RouteRestStopResponse response = service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000);

        assertThat(response.restStops()).isEmpty();
    }

    @Test
    @DisplayName("summary가 null이면 거리/시간은 0, placeName이 비면 주소명을 이름으로 쓴다")
    void summaryNullAndAddressFallback() {
        when(kakaoMapClient.searchKeyword("부산")).thenReturn(searchResult("129.0", "35.0", "", "부산 우동"));
        when(kakaoMapClient.getDirections(anyString(), anyString())).thenReturn(directions(0, null, VERTEXES));
        when(restStopRepository.findAll()).thenReturn(List.of());

        RouteRestStopResponse response = service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000);

        assertThat(response.destination().name()).isEqualTo("부산 우동");
        assertThat(response.route().distanceMeters()).isZero();
        assertThat(response.route().durationSeconds()).isZero();
        assertThat(response.restStops()).isEmpty();
    }

    @Test
    @DisplayName("목적지 좌표가 주어지면 지오코딩 없이 그 좌표로 경로를 계산한다")
    void destinationCoordinates_skipGeocoding() {
        when(kakaoMapClient.getDirections("127.0,37.0", "129.0,35.0"))
                .thenReturn(directions(0, new Summary(10L, 20L), VERTEXES));
        when(restStopRepository.findAll()).thenReturn(List.of());

        RouteRestStopResponse response = service.findRouteRestStops(37.0, 127.0, null, 35.0, 129.0, "부산항", 1000);

        assertThat(response.destination().name()).isEqualTo("부산항");
        assertThat(response.destination().latitude()).isEqualTo(35.0);
        assertThat(response.destination().longitude()).isEqualTo(129.0);
        verify(kakaoMapClient, never()).searchKeyword(anyString());
    }

    @Test
    @DisplayName("목적지 좌표가 일부(경도만)면 query 지오코딩으로 폴백한다")
    void partialDestinationCoordinates_fallbackToQuery() {
        when(kakaoMapClient.searchKeyword("부산")).thenReturn(searchResult("129.0", "35.0", "부산역", null));
        when(kakaoMapClient.getDirections(anyString(), anyString())).thenReturn(directions(0, null, VERTEXES));
        when(restStopRepository.findAll()).thenReturn(List.of());

        RouteRestStopResponse response = service.findRouteRestStops(37.0, 127.0, "부산", 35.0, null, "이름", 1000);

        assertThat(response.destination().name()).isEqualTo("부산역");
    }

    @Test
    @DisplayName("목적지 좌표만 있고 이름이 없거나 비면 기본 이름을 쓴다")
    void destinationCoordinates_defaultName() {
        when(kakaoMapClient.getDirections(anyString(), anyString())).thenReturn(directions(0, null, VERTEXES));
        when(restStopRepository.findAll()).thenReturn(List.of());

        assertThat(service.findRouteRestStops(37.0, 127.0, null, 35.0, 129.0, null, 1000)
                        .destination()
                        .name())
                .isEqualTo("목적지");
        assertThat(service.findRouteRestStops(37.0, 127.0, null, 35.0, 129.0, "  ", 1000)
                        .destination()
                        .name())
                .isEqualTo("목적지");
    }

    @Test
    @DisplayName("summary 값이 null이면 거리/시간 0으로 처리한다")
    void summaryWithNullValues() {
        when(kakaoMapClient.searchKeyword("부산")).thenReturn(searchResult("129.0", "35.0", "부산", null));
        when(kakaoMapClient.getDirections(anyString(), anyString()))
                .thenReturn(directions(0, new Summary(null, null), VERTEXES));
        when(restStopRepository.findAll()).thenReturn(List.of());

        RouteRestStopResponse response = service.findRouteRestStops(37.0, 127.0, "부산", null, null, null, 1000);

        assertThat(response.route().distanceMeters()).isZero();
        assertThat(response.route().durationSeconds()).isZero();
    }
}
