package com.vroomtracker.domain;

import com.vroomtracker.client.response.RestOilPriceItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "rest_oil_price",
        indexes = {@Index(name = "idx_rest_oil_price_service_area_code2", columnList = "service_area_code2")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestOilPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String routeCode;
    private String serviceAreaCode;
    private String routeName;
    private String direction;
    private String oilCompany;
    private String lpgYn;
    private String serviceAreaName;
    private String telNo;
    private String gasolinePrice;
    private String dieselPrice;
    private String lpgPrice;
    private String numOfRows;
    private String pageNo;

    @Column(name = "service_area_code2")
    private String serviceAreaCode2;

    private String serviceAreaAddress;

    private RestOilPriceEntity(RestOilPriceItem item) {
        this.routeCode = item.getRouteCode();
        this.serviceAreaCode = item.getServiceAreaCode();
        this.routeName = item.getRouteName();
        this.direction = item.getDirection();
        this.oilCompany = item.getOilCompany();
        this.lpgYn = item.getLpgYn();
        this.serviceAreaName = item.getServiceAreaName();
        this.telNo = item.getTelNo();
        this.gasolinePrice = item.getGasolinePrice();
        this.dieselPrice = item.getDieselPrice();
        this.lpgPrice = item.getLpgPrice();
        this.numOfRows = item.getNumOfRows();
        this.pageNo = item.getPageNo();
        this.serviceAreaCode2 = item.getServiceAreaCode2();
        this.serviceAreaAddress = item.getServiceAreaAddress();
    }

    public static RestOilPriceEntity from(RestOilPriceItem item) {
        return new RestOilPriceEntity(item);
    }
}
