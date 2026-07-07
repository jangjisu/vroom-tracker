import assert from 'node:assert/strict';
import test from 'node:test';

import {
    canRequestRouteAutomatically,
    createPopupContent,
    formatOilPriceDelta,
    formatNationalOilPriceSummary,
    formatOilPriceComparison,
    formatRouteComparisonSummary,
    isRouteGlobalLoadingState,
    routeMapSelectionMessage,
    routePointLabel,
    routeRecommendationLabels,
    shouldRequestRouteAutomatically,
    shouldShowRouteSearchInline
} from '../../main/resources/static/js/rest-stops-map.js';

test('createPopupContent renders rest stop popup as a small summary card', () => {
    const content = createPopupContent({
        unitName: '서울만남(부산)휴게소',
        routeName: '경부선'
    });

    assert.match(content, /rest-stop-map-popup-card/);
    assert.match(content, /서울만남\(부산\)휴게소/);
    assert.match(content, /경부선/);
    assert.match(content, /정보를 불러오는 중/);
});

test('createPopupContent renders availability tags on success', () => {
    const content = createPopupContent(
        { unitName: '안성(부산)휴게소', routeName: '경부선' },
        { status: 'success', tags: [{ key: 'food', label: '먹거리' }, { key: 'oil', label: '주유' }] }
    );

    assert.match(content, /rest-stop-map-popup-tag/);
    assert.match(content, /먹거리/);
    assert.match(content, /주유/);
    assert.doesNotMatch(content, /오른쪽 패널/);
});

test('createPopupContent shows empty notice when no data tags exist', () => {
    const content = createPopupContent(
        { unitName: '안성(부산)휴게소', routeName: '경부선' },
        { status: 'success', tags: [] }
    );

    assert.match(content, /등록된 정보 없음/);
    assert.doesNotMatch(content, /rest-stop-map-popup-tag"/);
});

test('createPopupContent escapes rest stop text before rendering HTML', () => {
    const content = createPopupContent({
        unitName: '<script>alert(1)</script>',
        routeName: '<b>경부선</b>'
    });

    assert.match(content, /&lt;script&gt;alert\(1\)&lt;\/script&gt;/);
    assert.match(content, /&lt;b&gt;경부선&lt;\/b&gt;/);
    assert.doesNotMatch(content, /<script>/);
    assert.doesNotMatch(content, /<b>경부선<\/b>/);
});

test('routePointLabel shows a selected point without repeating missing text', () => {
    assert.equal(
        routePointLabel({ name: '구로역', address: '서울 구로구 구로동' }, '미설정'),
        '구로역 · 서울 구로구 구로동'
    );
    assert.equal(
        routePointLabel({ name: '지도에서 선택한 위치', address: '' }, '미설정'),
        '지도에서 선택한 위치'
    );
    assert.equal(routePointLabel(undefined, '출발지를 설정하세요'), '출발지를 설정하세요');
});

test('routeMapSelectionMessage distinguishes target and confirmation state', () => {
    assert.equal(routeMapSelectionMessage('origin', false), '지도에서 출발 위치를 선택하세요.');
    assert.equal(routeMapSelectionMessage('origin', true), '선택한 출발 위치를 확정해주세요.');
    assert.equal(routeMapSelectionMessage('destination', false), '지도에서 도착 위치를 선택하세요.');
    assert.equal(routeMapSelectionMessage('destination', true), '선택한 도착 위치를 확정해주세요.');
});

test('canRequestRouteAutomatically requires selected origin and destination coordinates', () => {
    const origin = { latitude: 37.5, longitude: 126.9 };
    const destination = { latitude: 35.1, longitude: 129.0 };

    assert.equal(canRequestRouteAutomatically(origin, destination), true);
    assert.equal(canRequestRouteAutomatically(undefined, destination), false);
    assert.equal(canRequestRouteAutomatically(origin, undefined), false);
    assert.equal(canRequestRouteAutomatically(origin, { latitude: Number.NaN, longitude: 129.0 }), false);
});

test('shouldRequestRouteAutomatically keeps desktop route search manual', () => {
    const origin = { latitude: 37.5, longitude: 126.9 };
    const destination = { latitude: 35.1, longitude: 129.0 };

    assert.equal(shouldRequestRouteAutomatically(origin, destination, true), true);
    assert.equal(shouldRequestRouteAutomatically(origin, destination, false), false);
});

test('shouldShowRouteSearchInline requires both selected route points', () => {
    const origin = { latitude: 37.5, longitude: 126.9 };
    const destination = { latitude: 35.1, longitude: 129.0 };

    assert.equal(shouldShowRouteSearchInline(origin, destination), true);
    assert.equal(shouldShowRouteSearchInline(origin, undefined), false);
    assert.equal(shouldShowRouteSearchInline(undefined, destination), false);
});

test('isRouteGlobalLoadingState is true only while route search is loading', () => {
    assert.equal(isRouteGlobalLoadingState({ status: 'loading' }), true);
    assert.equal(isRouteGlobalLoadingState({ status: 'success' }), false);
    assert.equal(isRouteGlobalLoadingState({ status: 'not-found' }), false);
    assert.equal(isRouteGlobalLoadingState({ status: 'external-unavailable' }), false);
    assert.equal(isRouteGlobalLoadingState({ status: 'error' }), false);
    assert.equal(isRouteGlobalLoadingState(undefined), false);
});

test('routeRecommendationLabels returns comparison badge labels in response order', () => {
    assert.deepEqual(routeRecommendationLabels({
        recommendationTags: [
            { key: 'lowest-gasoline', label: '휘발유 최저가' },
            { key: 'largest-parking', label: '주차장 큼' }
        ]
    }), ['휘발유 최저가', '주차장 큼']);
    assert.deepEqual(routeRecommendationLabels({ recommendationTags: [] }), []);
    assert.deepEqual(routeRecommendationLabels({}), []);
});

test('formatRouteComparisonSummary renders prices, parking, food and facility counts compactly', () => {
    assert.deepEqual(formatRouteComparisonSummary({
        comparisonSummary: {
            gasolinePrice: '1,650원',
            dieselPrice: '1,550원',
            lpgPrice: '1,100원',
            gasolinePriceDiffFromAverage: -43,
            dieselPriceDiffFromAverage: 20,
            lpgPriceDiffFromAverage: 0,
            totalParkingCount: 63,
            foodMenuCount: 2,
            facilityCount: 3
        }
    }), [
        '휘발유 1,650원 (-43) · 경유 1,550원 (+20) · LPG 1,100원 (0)',
        '주차 63대 · 먹거리 2개 · 시설 3개'
    ]);

    assert.deepEqual(formatRouteComparisonSummary({
        comparisonSummary: {
            gasolinePrice: null,
            dieselPrice: '1,550원',
            lpgPrice: null,
            totalParkingCount: null,
            foodMenuCount: 0,
            facilityCount: 1
        }
    }), ['경유 1,550원', '시설 1개']);
});

test('formatOilPriceComparison renders average diff only when it exists', () => {
    assert.equal(formatOilPriceComparison('1,849원', -44), '1,849원 (-44)');
    assert.equal(formatOilPriceComparison('1,920원', 27), '1,920원 (+27)');
    assert.equal(formatOilPriceComparison('1,892원', 0), '1,892원 (0)');
    assert.equal(formatOilPriceComparison('1,849원', null), '1,849원');
    assert.equal(formatOilPriceComparison(null, -44), '');
});

test('formatOilPriceDelta marks cheaper and expensive average differences', () => {
    assert.deepEqual(formatOilPriceDelta(-44), { text: '(-44)', tone: 'cheap' });
    assert.deepEqual(formatOilPriceDelta(27), { text: '(+27)', tone: 'expensive' });
    assert.deepEqual(formatOilPriceDelta(0), { text: '(0)', tone: 'same' });
    assert.equal(formatOilPriceDelta(null), null);
});

test('formatNationalOilPriceSummary renders gasoline diesel and lpg averages', () => {
    assert.deepEqual(formatNationalOilPriceSummary({
        tradeDate: '2026.07.07',
        gasoline: {
            productName: '휘발유',
            price: '1,893원',
            dailyDiff: '-4.19'
        },
        diesel: {
            productName: '경유',
            price: '1,880원',
            dailyDiff: '+3'
        },
        lpg: {
            productName: '자동차용부탄',
            price: '1,135원',
            dailyDiff: '0'
        }
    }), {
        tradeDate: '2026.07.07',
        items: [
            { label: '휘발유', price: '1,893원', dailyDiff: '↓ 4.19원', dailyDiffTone: 'down' },
            { label: '경유', price: '1,880원', dailyDiff: '↑ 3원', dailyDiffTone: 'up' },
            { label: 'LPG', price: '1,135원', dailyDiff: '0원', dailyDiffTone: 'same' }
        ]
    });

    assert.equal(formatNationalOilPriceSummary(null), null);
});
