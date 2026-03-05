package com.vroomtracker.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시간대별 교통량 현황 (trafficFlowByTime API → DB 저장)
 * 연간 통계 데이터이므로 매일 새벽 1회 갱신.
 */
@Entity
@Table(name = "traffic_flow",
        indexes = @Index(name = "idx_traffic_flow_year", columnList = "std_year"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrafficFlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "std_year", nullable = false, length = 4)
    private String stdYear;

    /** 특수일 구분명 (평일, 토요일, 공휴일, 연휴 등) */
    @Column(name = "sphl_dftt_nm", length = 50)
    private String sphlDfttNm;

    @Column(name = "sphl_dftt_code", length = 10)
    private String sphlDfttCode;

    /** 특수일 전후 기간 범위 구분명 */
    @Column(name = "sphl_dftt_scop_type_nm", length = 50)
    private String sphlDfttScopTypeNm;

    @Column(name = "sphl_dftt_scop_type_code", length = 10)
    private String sphlDfttScopTypeCode;

    /** 기준시 (0~23) */
    @Column(name = "std_hour", nullable = false, length = 2)
    private String stdHour;

    /** 교통량 (대) */
    @Column(name = "trfl", length = 20)
    private String trfl;

    /** API 수집 시각 */
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private LocalDateTime fetchedAt;

    @Builder
    private TrafficFlowEntity(String stdYear, String sphlDfttNm, String sphlDfttCode,
                               String sphlDfttScopTypeNm, String sphlDfttScopTypeCode,
                               String stdHour, String trfl, LocalDateTime fetchedAt) {
        this.stdYear = stdYear;
        this.sphlDfttNm = sphlDfttNm;
        this.sphlDfttCode = sphlDfttCode;
        this.sphlDfttScopTypeNm = sphlDfttScopTypeNm;
        this.sphlDfttScopTypeCode = sphlDfttScopTypeCode;
        this.stdHour = stdHour;
        this.trfl = trfl;
        this.fetchedAt = fetchedAt;
    }
}
