package com.vroomtracker.client.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestStopItem {

    private String unitCode;
    private String unitName;
    private String routeNo;
    private String routeName;
    private String xValue;
    private String yValue;
    private String stdRestCd;
    private String serviceAreaCode;
}
