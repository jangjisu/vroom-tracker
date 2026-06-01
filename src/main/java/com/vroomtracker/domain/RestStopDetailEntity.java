package com.vroomtracker.domain;

import com.vroomtracker.client.response.RestStopDetailItem;
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
@Table(name = "rest_stop_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestStopDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String routeName;
    private String serviceAreaCode;
    private String serviceAreaName;
    private String telNo;
    private String brand;
    private String routeCode;
    private String serviceAreaCode2;
    private String svarAddr;
    private String convenience;
    private String maintenanceYn;
    private String truckSaYn;

    private RestStopDetailEntity(RestStopDetailItem item) {
        this.routeName = item.getRouteName();
        this.serviceAreaCode = item.getServiceAreaCode();
        this.serviceAreaName = item.getServiceAreaName();
        this.telNo = item.getTelNo();
        this.brand = item.getBrand();
        this.routeCode = item.getRouteCode();
        this.serviceAreaCode2 = item.getServiceAreaCode2();
        this.svarAddr = item.getSvarAddr();
        this.convenience = item.getConvenience();
        this.maintenanceYn = item.getMaintenanceYn();
        this.truckSaYn = item.getTruckSaYn();
    }

    public static RestStopDetailEntity from(RestStopDetailItem item) {
        return new RestStopDetailEntity(item);
    }
}
