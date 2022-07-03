package nextstep.subway.path.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.acceptance.LineAcceptanceTest;
import nextstep.subway.line.acceptance.LineSectionAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {
    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private LineResponse 오호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private StationResponse 미사역;
    private StationResponse 군자역;
    private StationResponse 왕십리역;

    /**
     * 교대역    --- *2호선* (20) ---   강남역
     * |                              |
     * *3호선*                      *신분당선*
     * (5)                           (10)
     * |                              |
     * 남부터미널역  --- *3호선* (30)---   양재
     *
     * 미사역 ----- *5호선* (55) ------- 군자역----- (30)---- 왕십리역
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = StationAcceptanceTest.지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = StationAcceptanceTest.지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = StationAcceptanceTest.지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);
        미사역 = StationAcceptanceTest.지하철역_등록되어_있음("미사역").as(StationResponse.class);
        군자역 = StationAcceptanceTest.지하철역_등록되어_있음("군자역").as(StationResponse.class);
        왕십리역 = StationAcceptanceTest.지하철역_등록되어_있음("왕십리역").as(StationResponse.class);

        신분당선 = LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 10, 500)).as(LineResponse.class);
        이호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest("이호선", "bg-red-600", 교대역.getId(), 강남역.getId(), 20, 200)).as(LineResponse.class);
        삼호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest("삼호선", "bg-red-600", 교대역.getId(), 양재역.getId(), 35, 0)).as(LineResponse.class);
        오호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest("오호선", "bg-red-600", 미사역.getId(), 군자역.getId(), 55, 0)).as(LineResponse.class);

        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 5);
        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(오호선, 군자역, 왕십리역, 30);
    }

    @Test
    void 최단_경로_조회() {
        //when
        ExtractableResponse<Response> findPathResponse = 최단_경로_조회_요청(교대역.getId(), 양재역.getId());

        //then
        최단_경로_거리_비교(findPathResponse, 30);
        //기본요금 1250 + 신분당선의 라인 요금 500원 추가 + 10km 초과 요금 600
        경로_별_요금_비교(findPathResponse, 2350);
    }

    @Test
    void 노선에_등록되지_않은_역_최단_경로_조회시_에러_발생() {
        //given : 노선에 등록되지 않은 구로역을 출발역으로 지정
        StationResponse 구로역 = StationAcceptanceTest.지하철역_등록되어_있음("구로역").as(StationResponse.class);

        //when 최단 경로 조회
        ExtractableResponse<Response> findPathResponse = 최단_경로_조회_요청(구로역.getId(), 교대역.getId());

        //then
        최단_경로_조회_404_실패(findPathResponse);
    }

    @Test
    void 출발지와_도착지가_같은_경로_조회시_에러_발생() {
        //when
        ExtractableResponse<Response> findPathResponse = 최단_경로_조회_요청(강남역.getId(), 강남역.getId());

        //then
        최단_경로_조회_400_실패(findPathResponse);

    }

    @Test
    void 거리_기준_요금_정보_조회_기본운임() {
        ExtractableResponse<Response> findPathResponse = 최단_경로_조회_요청(교대역.getId(), 남부터미널역.getId());

        경로_별_요금_비교(findPathResponse, 1250);
    }

    @Test
    void 거리_기준_요금_정보_조회_10km_초과() {
        //군자역 - 왕십리역 distance 30
        ExtractableResponse<Response> findPathResponse = 최단_경로_조회_요청(군자역.getId(), 왕십리역.getId());

        경로_별_요금_비교(findPathResponse, 1850);
    }

    @Test
    void 거리_기준_요금_정보_조회_50km_초과() {
        ExtractableResponse<Response> findPathResponse = 최단_경로_조회_요청(미사역.getId(), 군자역.getId());

        경로_별_요금_비교(findPathResponse, 1950);

    }

    private static ExtractableResponse<Response> getPath(String path, Map<String, ?> queryParams) {
        return RestAssured.given().log().all()
                .queryParams(queryParams)
                .when().get(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 최단_경로_조회_요청(Long sourceId, Long targetId) {
        Map<String, Long> queryParam = new HashMap<>();
        queryParam.put("source", sourceId);
        queryParam.put("target", targetId);

        return getPath("paths", queryParam);
    }

    private void 최단_경로_거리_비교(ExtractableResponse<Response> response, int expectedDistance) {
        PathResponse pathResponse = response.as(PathResponse.class);

        assertThat(pathResponse.getDistance()).isEqualTo(expectedDistance);
    }

    private void 경로_별_요금_비교(ExtractableResponse<Response> response, int expectedFare) {
        PathResponse pathResponse = response.as(PathResponse.class);

        assertThat(pathResponse.getFare()).isEqualTo(expectedFare);
    }

    private void 최단_경로_조회_404_실패(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private void 최단_경로_조회_400_실패(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
