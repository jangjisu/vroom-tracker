import assert from 'node:assert/strict';
import test from 'node:test';

import {
    createRouteRestStopImage,
    renderDetailImage
} from '../../main/resources/static/js/rest-stop-images.js';

function classList(initial = []) {
    const values = new Set(initial);
    return {
        contains: (value) => values.has(value),
        toggle: (value, force) => {
            const enabled = force === undefined ? !values.has(value) : force;
            if (enabled) {
                values.add(value);
            } else {
                values.delete(value);
            }
        }
    };
}

function imageElement() {
    return {
        alt: '',
        className: '',
        loading: '',
        src: '',
        removeAttribute(name) {
            if (name === 'src') {
                this.src = '';
            }
        }
    };
}

test('detail image is shown with its URL and accessible rest stop name', () => {
    const wrapper = { classList: classList(['d-none']) };
    const image = imageElement();
    const document = {
        getElementById: (id) => ({
            restStopDetailImageWrapper: wrapper,
            restStopDetailImage: image
        }[id])
    };

    renderDetailImage(document, {
        unitName: '의왕(청계)휴게소',
        detailImageUrl: '/api/rest-stops/A00001/images/detail'
    });

    assert.equal(wrapper.classList.contains('d-none'), false);
    assert.equal(image.src, '/api/rest-stops/A00001/images/detail');
    assert.equal(image.alt, '의왕(청계)휴게소 전경');
});

test('detail image leaves no reserved layout when URL is null', () => {
    const wrapper = { classList: classList() };
    const image = imageElement();
    image.src = '/old-image.webp';
    const document = {
        getElementById: (id) => ({
            restStopDetailImageWrapper: wrapper,
            restStopDetailImage: image
        }[id])
    };

    renderDetailImage(document, { unitName: '의왕휴게소', detailImageUrl: null });

    assert.equal(wrapper.classList.contains('d-none'), true);
    assert.equal(image.src, '');
    assert.equal(image.alt, '');
});

test('route list image is created only when listImageUrl exists', () => {
    const document = { createElement: () => imageElement() };

    const image = createRouteRestStopImage(document, {
        unitName: '함안휴게소',
        listImageUrl: '/api/rest-stops/B00001/images/list'
    });

    assert.equal(image.src, '/api/rest-stops/B00001/images/list');
    assert.equal(image.alt, '함안휴게소 전경');
    assert.equal(image.loading, 'lazy');
    assert.equal(image.className, 'route-result-image');
    assert.equal(createRouteRestStopImage(document, { unitName: '함안휴게소', listImageUrl: null }), null);
    assert.equal(createRouteRestStopImage(document, { unitName: '함안휴게소', listImageUrl: '   ' }), null);
});
