import assert from 'node:assert/strict';
import test from 'node:test';

import {
    ROUTE_POINT_TARGET,
    createRoutePointSelection
} from '../../main/resources/static/js/route-point-selection.js';

test('origin and destination are selected independently', () => {
    const selection = createRoutePointSelection();

    selection.select(ROUTE_POINT_TARGET.ORIGIN, {
        name: '현재 위치',
        latitude: 37.5,
        longitude: 127.0
    });
    selection.select(ROUTE_POINT_TARGET.DESTINATION, {
        name: '부산역',
        address: '부산 동구',
        latitude: 35.1,
        longitude: 129.0
    });

    assert.deepEqual(selection.getOrigin(), {
        name: '현재 위치',
        address: '',
        latitude: 37.5,
        longitude: 127.0
    });
    assert.equal(selection.getDestination().name, '부산역');
    assert.equal(selection.canRequestRoute(), true);
});

test('route cannot be requested until both points are selected', () => {
    const selection = createRoutePointSelection();

    assert.equal(selection.canRequestRoute(), false);
    selection.select(ROUTE_POINT_TARGET.ORIGIN, {
        name: '서울역',
        latitude: 37.55,
        longitude: 126.97
    });
    assert.equal(selection.canRequestRoute(), false);
});

test('clearing edited destination keeps origin and disables route request', () => {
    const selection = createRoutePointSelection();
    selection.select(ROUTE_POINT_TARGET.ORIGIN, {
        name: '서울역', latitude: 37.55, longitude: 126.97
    });
    selection.select(ROUTE_POINT_TARGET.DESTINATION, {
        name: '부산역', latitude: 35.1, longitude: 129.0
    });

    selection.clear(ROUTE_POINT_TARGET.DESTINATION);

    assert.equal(selection.getOrigin().name, '서울역');
    assert.equal(selection.getDestination(), undefined);
    assert.equal(selection.canRequestRoute(), false);
});

test('search target accepts only origin or destination', () => {
    const selection = createRoutePointSelection();

    selection.setSearchTarget(ROUTE_POINT_TARGET.ORIGIN);
    assert.equal(selection.getSearchTarget(), ROUTE_POINT_TARGET.ORIGIN);
    assert.throws(() => selection.setSearchTarget('unknown'), /Unknown route point target/);
});

test('canceling a map draft preserves the selected point', () => {
    const selection = createRoutePointSelection();
    selection.select(ROUTE_POINT_TARGET.ORIGIN, {
        name: '서울역',
        latitude: 37.55,
        longitude: 126.97
    });

    selection.beginMapSelection(ROUTE_POINT_TARGET.ORIGIN);
    selection.updateMapDraft({ latitude: 37.6, longitude: 127.1 });
    selection.cancelMapSelection();

    assert.equal(selection.getOrigin().name, '서울역');
    assert.equal(selection.getMapTarget(), undefined);
    assert.equal(selection.getMapDraft(), undefined);
});

test('confirming a map draft updates only its target with the map fallback name', () => {
    const selection = createRoutePointSelection();
    selection.select(ROUTE_POINT_TARGET.ORIGIN, {
        name: '서울역',
        latitude: 37.55,
        longitude: 126.97
    });

    selection.beginMapSelection(ROUTE_POINT_TARGET.DESTINATION);
    selection.updateMapDraft({ latitude: 35.2, longitude: 129.1 });
    const selected = selection.confirmMapSelection();

    assert.equal(selected.name, '지도에서 선택한 위치');
    assert.equal(selection.getDestination().latitude, 35.2);
    assert.equal(selection.getOrigin().name, '서울역');
    assert.equal(selection.getMapDraft(), undefined);
});

test('map selection cannot confirm before a coordinate is chosen', () => {
    const selection = createRoutePointSelection();

    selection.beginMapSelection(ROUTE_POINT_TARGET.ORIGIN);

    assert.equal(selection.confirmMapSelection(), undefined);
    assert.equal(selection.getMapTarget(), ROUTE_POINT_TARGET.ORIGIN);
});

test('invalid coordinates are rejected', () => {
    const selection = createRoutePointSelection();

    assert.throws(
        () => selection.select(ROUTE_POINT_TARGET.ORIGIN, { latitude: NaN, longitude: 127.0 }),
        /finite numbers/
    );
});
