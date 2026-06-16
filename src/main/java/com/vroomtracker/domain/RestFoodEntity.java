package com.vroomtracker.domain;

import com.vroomtracker.client.response.RestBestfoodItem;
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
        name = "rest_food",
        indexes = {@Index(name = "idx_rest_food_std_rest_cd", columnList = "std_rest_cd")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestFoodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stdRestCd;
    private String stdRestName;
    private String restCode;
    private String routeCode;
    private String routeName;
    private String serviceAreaAddress;
    private String seq;
    private String foodName;
    private String foodCost;

    @Lob
    private String description;

    @Lob
    private String foodMaterial;

    private String recommendYn;
    private String bestFoodYn;
    private String premiumYn;
    private String seasonMenu;
    private String appExposeYn;

    private RestFoodEntity(RestBestfoodItem item) {
        this.stdRestCd = item.getStdRestCd();
        this.stdRestName = item.getStdRestNm();
        this.restCode = item.getRestCd();
        this.routeCode = item.getRouteCd();
        this.routeName = item.getRouteNm();
        this.serviceAreaAddress = item.getServiceAreaAddress();
        this.seq = item.getSeq();
        this.foodName = item.getFoodNm();
        this.foodCost = item.getFoodCost();
        this.description = item.getEtc();
        this.foodMaterial = item.getFoodMaterial();
        this.recommendYn = item.getRecommendyn();
        this.bestFoodYn = item.getBestfoodyn();
        this.premiumYn = item.getPremiumyn();
        this.seasonMenu = item.getSeasonMenu();
        this.appExposeYn = item.getApp();
    }

    public static RestFoodEntity from(RestBestfoodItem item) {
        return new RestFoodEntity(item);
    }
}
