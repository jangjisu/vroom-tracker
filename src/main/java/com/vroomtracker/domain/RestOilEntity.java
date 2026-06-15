package com.vroomtracker.domain;

import com.vroomtracker.client.response.RestOilItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "rest_oil",
        indexes = {@Index(name = "idx_rest_oil_route_station", columnList = "route_code, normalized_station_name")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestOilEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String standardRestCode;
    private String standardRestName;
    private String startTime;
    private String endTime;
    private String originalModifierId;
    private String originalModifiedDateTime;
    private String lastModifiedUser;
    private String lastModifiedDateTime;
    private String serviceAreaAddress;

    @Column(name = "route_code")
    private String routeCode;

    private String routeName;
    private String convenienceCode;
    private String convenienceName;

    @Lob
    private String convenienceDescription;

    @Column(name = "normalized_station_name")
    private String normalizedStationName;

    private RestOilEntity(RestOilItem item) {
        this.standardRestCode = item.getStandardRestCode();
        this.standardRestName = item.getStandardRestName();
        this.startTime = item.getStartTime();
        this.endTime = item.getEndTime();
        this.originalModifierId = item.getOriginalModifierId();
        this.originalModifiedDateTime = item.getOriginalModifiedDateTime();
        this.lastModifiedUser = item.getLastModifiedUser();
        this.lastModifiedDateTime = item.getLastModifiedDateTime();
        this.serviceAreaAddress = item.getServiceAreaAddress();
        this.routeCode = item.getRouteCode();
        this.routeName = item.getRouteName();
        this.convenienceCode = item.getConvenienceCode();
        this.convenienceName = item.getConvenienceName();
        this.convenienceDescription = item.getConvenienceDescription();
        this.normalizedStationName = normalizeStationName(item.getStandardRestName());
    }

    public static RestOilEntity from(RestOilItem item) {
        return new RestOilEntity(item);
    }

    public static String normalizeStationName(String stationName) {
        if (stationName == null) {
            return null;
        }

        return stationName.replace("휴게소", "").replace("주유소", "").replaceAll("\\s+", "");
    }
}
