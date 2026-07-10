package com.restroute.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepresentativeFoodItem {

    private String serviceAreaCode;
    private String serviceAreaCode2;
    private String serviceAreaName;
    private String routeCode;
    private String routeName;
    private String direction;
    private String batchMenu;
    private String salePrice;
}
