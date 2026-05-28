package com.vroomtracker.support;

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
