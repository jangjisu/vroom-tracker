import assert from 'node:assert/strict';
import test from 'node:test';

import { createPlaceSearchRequest } from '../../main/resources/static/js/place-search-request.js';

function jsonResponse(status, body) {
    return { status, ok: status >= 200 && status < 300, json: async () => body };
}

function collect() {
    const states = [];
    return { states, onState: (state) => states.push(state) };
}

test('빈 검색어는 error 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createPlaceSearchRequest({ fetchImpl: async () => jsonResponse(200, {}), onState }).load('   ');
    assert.deepEqual(states, [{ status: 'error' }]);
});

test('성공 응답이면 loading 후 success와 candidates 배열을 낸다', async () => {
    const candidates = [{ name: '부산역', latitude: 35.1, longitude: 129.0 }];
    const { states, onState } = collect();
    await createPlaceSearchRequest({
        fetchImpl: async () => jsonResponse(200, { code: 'SUCCESS', data: candidates }),
        onState
    }).load('부산');

    assert.deepEqual(states[0], { status: 'loading' });
    assert.deepEqual(states[1], { status: 'success', candidates });
});

test('빈 배열도 success로 낸다(결과 없음)', async () => {
    const { states, onState } = collect();
    await createPlaceSearchRequest({
        fetchImpl: async () => jsonResponse(200, { code: 'SUCCESS', data: [] }),
        onState
    }).load('없는곳');

    assert.deepEqual(states.at(-1), { status: 'success', candidates: [] });
});

test('EXTERNAL_API_UNAVAILABLE는 external-unavailable 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createPlaceSearchRequest({
        fetchImpl: async () => jsonResponse(200, { code: 'EXTERNAL_API_UNAVAILABLE' }),
        onState
    }).load('부산');

    assert.equal(states.at(-1).status, 'external-unavailable');
});

test('네트워크 실패는 error 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createPlaceSearchRequest({
        fetchImpl: async () => {
            throw new Error('down');
        },
        onState
    }).load('부산');

    assert.equal(states.at(-1).status, 'error');
});

test('AbortError는 상태를 내지 않는다', async () => {
    const { states, onState } = collect();
    await createPlaceSearchRequest({
        fetchImpl: async () => {
            const error = new Error('aborted');
            error.name = 'AbortError';
            throw error;
        },
        onState
    }).load('부산');

    assert.deepEqual(states, [{ status: 'loading' }]);
});
