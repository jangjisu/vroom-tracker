package com.vroomtracker.support;

import com.vroomtracker.client.response.RestStopDetailItem;
import com.vroomtracker.client.response.RestStopDetailResponse;
import com.vroomtracker.client.response.RestStopItem;
import com.vroomtracker.client.response.RestStopResponse;
import java.lang.reflect.Constructor;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public final class RestStopTestFixtures {

    private RestStopTestFixtures() {}

    public static RestStopItem restStopItem(String unitCode, String unitName) {
        RestStopItem item = instantiate(RestStopItem.class);
        ReflectionTestUtils.setField(item, "unitCode", unitCode);
        ReflectionTestUtils.setField(item, "unitName", unitName);
        ReflectionTestUtils.setField(item, "routeNo", "0010");
        ReflectionTestUtils.setField(item, "routeName", "경부선");
        ReflectionTestUtils.setField(item, "xValue", "127.042514");
        ReflectionTestUtils.setField(item, "yValue", "37.459939");
        ReflectionTestUtils.setField(item, "stdRestCd", "000001");
        ReflectionTestUtils.setField(item, "serviceAreaCode", "A00001");
        return item;
    }

    public static RestStopResponse restStopResponse(String code, String pageSize, List<RestStopItem> items) {
        RestStopResponse response = instantiate(RestStopResponse.class);
        ReflectionTestUtils.setField(response, "code", code);
        ReflectionTestUtils.setField(response, "message", "인증키가 유효합니다.");
        ReflectionTestUtils.setField(response, "count", String.valueOf(items.size()));
        ReflectionTestUtils.setField(response, "pageSize", pageSize);
        ReflectionTestUtils.setField(response, "list", items);
        return response;
    }

    public static RestStopDetailItem restStopDetailItem(String serviceAreaCode, String serviceAreaName) {
        RestStopDetailItem item = instantiate(RestStopDetailItem.class);
        ReflectionTestUtils.setField(item, "routeName", "경부선");
        ReflectionTestUtils.setField(item, "serviceAreaCode", serviceAreaCode);
        ReflectionTestUtils.setField(item, "serviceAreaName", serviceAreaName);
        ReflectionTestUtils.setField(item, "telNo", "054-751-6890");
        ReflectionTestUtils.setField(item, "brand", "투썸플레이스");
        ReflectionTestUtils.setField(item, "routeCode", "0010");
        ReflectionTestUtils.setField(item, "serviceAreaCode2", "000054");
        ReflectionTestUtils.setField(item, "svarAddr", "경북 경주시 건천읍방내리 14");
        ReflectionTestUtils.setField(item, "convenience", "수유실");
        ReflectionTestUtils.setField(item, "maintenanceYn", "X");
        ReflectionTestUtils.setField(item, "truckSaYn", "X");
        return item;
    }

    public static RestStopDetailResponse restStopDetailResponse(
            String code, String pageSize, List<RestStopDetailItem> items) {
        RestStopDetailResponse response = instantiate(RestStopDetailResponse.class);
        ReflectionTestUtils.setField(response, "code", code);
        ReflectionTestUtils.setField(response, "message", "인증키가 유효합니다.");
        ReflectionTestUtils.setField(response, "count", String.valueOf(items.size()));
        ReflectionTestUtils.setField(response, "pageSize", pageSize);
        ReflectionTestUtils.setField(response, "list", items);
        return response;
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate test fixture: " + type.getName(), e);
        }
    }
}
