import assert from 'node:assert/strict';
import test from 'node:test';

import {
    createPopupContent,
    routeMapSelectionMessage,
    routePointLabel
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
