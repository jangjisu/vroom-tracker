import assert from 'node:assert/strict';
import test from 'node:test';

import { createRouteRestStopRequest } from '../../main/resources/static/js/route-rest-stop-request.js';

function jsonResponse(status, body) {
    return { status, ok: status >= 200 && status < 300, json: async () => body };
}

function collect() {
    const states = [];
    return { states, onState: (state) => states.push(state) };
}

test('빈 목적지나 잘못된 출발지는 error 상태를 낸다', async () => {
    const a = collect();
    await createRouteRestStopRequest({ fetchImpl: async () => jsonResponse(200, {}), onState: a.onState })
        .load(37.0, 127.0, '   ');
    assert.deepEqual(a.states, [{ status: 'error' }]);

    const b = collect();
    await createRouteRestStopRequest({ fetchImpl: async () => jsonResponse(200, {}), onState: b.onState })
        .load(NaN, 127.0, '부산');
    assert.deepEqual(b.states, [{ status: 'error' }]);
});

test('성공 응답이면 loading 후 success와 data를 낸다', async () => {
    const data = { destination: { name: '부산역' }, route: { distanceMeters: 100 }, restStops: [] };
    const { states, onState } = collect();
    await createRouteRestStopRequest({
        fetchImpl: async () => jsonResponse(200, { code: 'SUCCESS', data }),
        onState
    }).load(37.0, 127.0, '부산');

    assert.deepEqual(states[0], { status: 'loading' });
    assert.deepEqual(states[1], { status: 'success', data });
});

test('404 NOT_FOUND는 not-found 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createRouteRestStopRequest({
        fetchImpl: async () => jsonResponse(404, { code: 'NOT_FOUND' }),
        onState
    }).load(37.0, 127.0, '없는곳');

    assert.equal(states.at(-1).status, 'not-found');
});

test('EXTERNAL_API_UNAVAILABLE는 external-unavailable 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createRouteRestStopRequest({
        fetchImpl: async () => jsonResponse(200, { code: 'EXTERNAL_API_UNAVAILABLE' }),
        onState
    }).load(37.0, 127.0, '부산');

    assert.equal(states.at(-1).status, 'external-unavailable');
});

test('네트워크 실패는 error 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createRouteRestStopRequest({
        fetchImpl: async () => {
            throw new Error('network down');
        },
        onState
    }).load(37.0, 127.0, '부산');

    assert.equal(states.at(-1).status, 'error');
});

test('AbortError는 상태를 내지 않는다', async () => {
    const { states, onState } = collect();
    await createRouteRestStopRequest({
        fetchImpl: async () => {
            const error = new Error('aborted');
            error.name = 'AbortError';
            throw error;
        },
        onState
    }).load(37.0, 127.0, '부산');

    assert.deepEqual(states, [{ status: 'loading' }]);
});
