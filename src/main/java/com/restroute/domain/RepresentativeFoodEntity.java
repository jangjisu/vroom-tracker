package com.restroute.domain;

import com.restroute.client.response.RepresentativeFoodItem;
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
        name = "rest_representative_food",
        indexes = {
            @Index(name = "idx_representative_food_service_area_code", columnList = "service_area_code"),
            @Index(name = "idx_representative_food_rest_stop_code", columnList = "rest_stop_service_area_code")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepresentativeFoodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceAreaCode;
    private String serviceAreaCode2;
    private String serviceAreaName;
    private String routeCode;
    private String routeName;
    private String direction;
    private String batchMenu;
    private String salePrice;
    private String restStopServiceAreaCode;

    private RepresentativeFoodEntity(RepresentativeFoodItem item, String restStopServiceAreaCode) {
        this.serviceAreaCode = item.getServiceAreaCode();
        this.serviceAreaCode2 = item.getServiceAreaCode2();
        this.serviceAreaName = item.getServiceAreaName();
        this.routeCode = item.getRouteCode();
        this.routeName = item.getRouteName();
        this.direction = item.getDirection();
        this.batchMenu = item.getBatchMenu();
        this.salePrice = item.getSalePrice();
        this.restStopServiceAreaCode = restStopServiceAreaCode;
    }

    public static RepresentativeFoodEntity from(RepresentativeFoodItem item, String restStopServiceAreaCode) {
        return new RepresentativeFoodEntity(item, restStopServiceAreaCode);
    }
}
