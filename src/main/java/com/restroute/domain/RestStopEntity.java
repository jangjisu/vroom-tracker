package com.restroute.domain;

import com.restroute.client.response.RestStopItem;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "rest_stop")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestStopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unitCode;
    private String unitName;
    private String routeNo;
    private String routeName;
    private String xValue;
    private String yValue;
    private String stdRestCd;
    private String serviceAreaCode;

    private RestStopEntity(RestStopItem item) {
        this.unitCode = item.getUnitCode();
        this.unitName = item.getUnitName();
        this.routeNo = item.getRouteNo();
        this.routeName = item.getRouteName();
        this.xValue = item.getXValue();
        this.yValue = item.getYValue();
        this.stdRestCd = item.getStdRestCd();
        this.serviceAreaCode = item.getServiceAreaCode();
    }

    public static RestStopEntity from(RestStopItem item) {
        return new RestStopEntity(item);
    }
}
