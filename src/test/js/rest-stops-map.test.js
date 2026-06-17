import assert from 'node:assert/strict';
import test from 'node:test';

import { createPopupContent } from '../../main/resources/static/js/rest-stops-map.js';

test('createPopupContent renders rest stop popup as a small summary card', () => {
    const content = createPopupContent({
        unitName: '서울만남(부산)휴게소',
        routeName: '경부선'
    });

    assert.match(content, /rest-stop-map-popup-card/);
    assert.match(content, /서울만남\(부산\)휴게소/);
    assert.match(content, /경부선/);
    assert.match(content, /상세 정보는 오른쪽 패널에서 확인/);
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
