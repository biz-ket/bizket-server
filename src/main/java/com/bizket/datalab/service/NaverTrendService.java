package com.bizket.datalab.service;

import com.bizket.datalab.dto.forecastInterest.ForecastInterestResponseDto;
import com.bizket.datalab.dto.forecastInterest.ForecastUserFriendlyResponseDto;
import com.bizket.datalab.dto.monthlyInterest.MonthlyInterestDataPointDto;
import com.bizket.datalab.dto.monthlyInterest.MonthlyInterestResponseDto;
import com.bizket.datalab.dto.monthlyInterest.MonthlyInterestDto;
import com.bizket.datalab.dto.relatedInterest.NaverACResponse;
import com.bizket.datalab.dto.relatedInterest.RelatedSuggestDto;
import com.bizket.datalab.dto.saturation.CombinedSaturationResponseDto;
import com.bizket.datalab.dto.saturation.SaturationResponseDto;
import com.bizket.datalab.dto.weekday.WeekdayRatioResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NaverTrendService {

    private static final String API_URL = "https://openapi.naver.com/v1/datalab/search";
    private static final String AC_URL = "https://ac.search.naver.com/nx/ac";
    private static final String BLOG_API = "https://openapi.naver.com/v1/search/blog.json";
    private static final String NEWS_API = "https://openapi.naver.com/v1/search/news.json";

    private final RestTemplate acRest = new RestTemplate();
    private final RestTemplate rt;
    private final String clientId;
    private final String clientSecret;

    public NaverTrendService(
            @Qualifier("datalabRestTemplate") RestTemplate rt,
            @Value("${naver.client.id}") String clientId,
            @Value("${naver.client.secret}") String clientSecret
    ) {
        this.rt = rt;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // 월별 관심사 반환 - 검색 절대량 반환 불가
    public List<MonthlyInterestResponseDto> getMonthlyTrends(String keyword, String startDate, String endDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "timeUnit", "month",
                "keywordGroups", List.of(
                        Map.of(
                                "groupName", keyword,
                                "keywords", List.of(keyword)
                        )
                )
        );

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<MonthlyInterestDto> resp = rt.exchange(API_URL, HttpMethod.POST, req, MonthlyInterestDto.class);

        var result = resp.getBody().results().get(0);
        return result.data().stream()
                .map(dp -> {
                    LocalDate date = LocalDate.parse(dp.period());
                    YearMonth ym = YearMonth.from(date);
                    return new MonthlyInterestResponseDto(
                            keyword,
                            ym,
                            dp.ratio()
                    );
                })
                .collect(Collectors.toList());
    }


    // 자동완성 API 기반 연관검색어 Top10 조회
    public RelatedSuggestDto getRelatedSuggestions(String keyword) {
        String url = UriComponentsBuilder
                .fromUriString(AC_URL)
                .queryParam("q", keyword)
                .queryParam("con", "2")
                .queryParam("frm", "nx")
                .queryParam("ans", "2")
                .queryParam("r_format", "json")
                .queryParam("r_enc", "UTF-8")
                .queryParam("q_enc", "UTF-8")
                .queryParam("rev", "4")
                .queryParam("st", "100")
                .build()
                .toUriString();

        NaverACResponse resp = acRest.getForObject(url, NaverACResponse.class);
        if (resp == null || resp.items() == null || resp.items().isEmpty()) {
            return new RelatedSuggestDto(keyword, List.of());
        }

        Object first = resp.items().get(0);
        if (!(first instanceof List<?> rawList)) {
            return new RelatedSuggestDto(keyword, List.of());
        }

        List<String> suggestions = rawList.stream()
                .filter(e -> e instanceof List<?>)
                .map(e -> (List<?>) e)
                .map(list -> list.isEmpty() ? "" : list.get(0).toString())
                .limit(10)
                .collect(Collectors.toList());

        return new RelatedSuggestDto(keyword, suggestions);
    }


    // 블로그 전체 검색량 조회
    public long fetchBlogTotalCount(String keyword) {
        return fetchTotalCount(BLOG_API, keyword);
    }

    // 뉴스 전체 검색량 조회
    public long fetchNewsTotalCount(String keyword) {
        return fetchTotalCount(NEWS_API, keyword);
    }

    // 공통 로직: display=1 로 total 만 가져오기
    private long fetchTotalCount(String apiUrl, String keyword) {
        String url = UriComponentsBuilder
                .fromUriString(apiUrl)
                .queryParam("query", keyword)
                .queryParam("display", 1)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = rt.exchange(url, HttpMethod.GET, req, Map.class).getBody();
        Object total = body.get("total");
        return total instanceof Number
                ? ((Number) total).longValue()
                : Long.parseLong(total.toString());
    }

    // 블로그 기반 포화지수 (블로그 글 수 / 전체 웹문서 검색량)
    public SaturationResponseDto calculateBlogSaturation(String keyword) {
        long contentCount = fetchBlogTotalCount(keyword);
        long searchCount = fetchWebTotalCount(keyword);
        double index = searchCount > 0
                ? contentCount * 100.0 / searchCount
                : 0;
        return new SaturationResponseDto(keyword, contentCount, searchCount, index);
    }

    // 뉴스 기반 포화지수 (뉴스 기사 수 / 전체 웹문서 검색량)
    public SaturationResponseDto calculateNewsSaturation(String keyword) {
        long contentCount = fetchNewsTotalCount(keyword);
        long searchCount = fetchWebTotalCount(keyword);
        double index = searchCount > 0
                ? contentCount * 100.0 / searchCount
                : 0;
        return new SaturationResponseDto(keyword, contentCount, searchCount, index);
    }

    // 포화도 합침
    public CombinedSaturationResponseDto calculateCombinedSaturation(String keyword) {
        SaturationResponseDto blog = calculateBlogSaturation(keyword);
        SaturationResponseDto news = calculateNewsSaturation(keyword);
        return new CombinedSaturationResponseDto(keyword, blog, news);
    }

    // 전체 웹문서 검색량 조회
    public long fetchWebTotalCount(String keyword) {
        return fetchTotalCount("https://openapi.naver.com/v1/search/webkr.json", keyword);
    }

    //다음 달 관심도 비율 예측
    public ForecastInterestResponseDto forecastNextMonthInterest(
            String keyword,
            String startDate,
            String endDate
    ) {
        List<MonthlyInterestResponseDto> history =
                getMonthlyTrends(keyword, startDate, endDate);

        YearMonth endYm = YearMonth.parse(endDate.substring(0, 7));

        List<MonthlyInterestResponseDto> fullMonths = history.stream()
                .filter(dp -> dp.yearMonth().isBefore(endYm))
                .collect(Collectors.toList());
        if (!fullMonths.isEmpty()) {
            history = fullMonths;
        }

        double forecastRatio;
        int n = history.size();
        if (n < 2) {
            double last = n == 0 ? 0.0 : history.get(n - 1).searchVolume();
            forecastRatio = last;
        } else {
            double sumDelta = 0;
            for (int i = 1; i < n; i++) {
                sumDelta += (history.get(i).searchVolume()
                        - history.get(i - 1).searchVolume());
            }
            double avgDelta = sumDelta / (n - 1);
            double last = history.get(n - 1).searchVolume();
            forecastRatio = Math.max(last + avgDelta, 0);
        }

        YearMonth nextYm = endYm.plusMonths(1);

        return new ForecastInterestResponseDto(keyword, nextYm, forecastRatio);
    }

    // 요일별 검색 비율 반환
    public WeekdayRatioResponseDto getWeekdayRatio(String keyword, String startDate, String endDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "timeUnit", "date",
                "keywordGroups", List.of(
                        Map.of(
                                "groupName", keyword,
                                "keywords", List.of(keyword)
                        )
                )
        );
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<MonthlyInterestDto> resp =
                rt.exchange(API_URL, HttpMethod.POST, req, MonthlyInterestDto.class);

        List<MonthlyInterestDataPointDto> dataPoints =
                resp.getBody().results().get(0).data();

        Map<DayOfWeek, Double> avgByDow = dataPoints.stream()
                .collect(Collectors.groupingBy(
                        dp -> LocalDate.parse(dp.period()).getDayOfWeek(),
                        Collectors.averagingDouble(MonthlyInterestDataPointDto::ratio)
                ));

        Map<String, Double> avgRatioByDay = new LinkedHashMap<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            avgRatioByDay.put(dow.name(), avgByDow.getOrDefault(dow, 0.0));
        }

        return new WeekdayRatioResponseDto(keyword, avgRatioByDay);
    }


    public ForecastUserFriendlyResponseDto forecastNextMonthInterestFriendly(
            String keyword, String startDate, String endDate
    ) {
        ForecastInterestResponseDto raw = forecastNextMonthInterest(keyword, startDate, endDate);

        List<MonthlyInterestResponseDto> history = getMonthlyTrends(keyword, startDate, endDate).stream()
                .filter(dp -> dp.yearMonth().isBefore(raw.forecastMonth()))
                .collect(Collectors.toList());

        double lastRatio = history.isEmpty() ? raw.forecastRatio()
                : history.get(history.size() - 1).searchVolume();
        double denom = history.stream()
                .mapToDouble(MonthlyInterestResponseDto::searchVolume)
                .average()
                .orElse(lastRatio);

        // 4) 변화율 계산
        double changePct = denom > 0
                ? (raw.forecastRatio() - denom) / denom * 100
                : 0;
        changePct = Math.round(changePct * 100.0) / 100.0;

        // 5) 라벨 구간
        double abs = Math.abs(changePct);
        String label;
        if (abs < 5) {
            label = "변동 없음";
        } else if (changePct > 0) {
            if (abs < 20)      label = "소폭 상승 예상";
            else if (abs < 50) label = "상승 예상";
            else               label = "급상승 예상";
        } else {
            if (abs < 20)      label = "소폭 감소 예상";
            else if (abs < 50) label = "감소 예상";
            else               label = "급감 예상";
        }

        return new ForecastUserFriendlyResponseDto(
                keyword,
                raw.forecastMonth(),
                changePct,
                label
        );
    }
}
