import assert from 'node:assert/strict';
import test from 'node:test';

import { createRestStopDetailRequest } from '../../main/resources/static/js/rest-stop-detail-request.js';

function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((promiseResolve, promiseReject) => {
        resolve = promiseResolve;
        reject = promiseReject;
    });
    return { promise, reject, resolve };
}

function response({ ok = true, status = 200, body, jsonError } = {}) {
    return {
        ok,
        status,
        json: jsonError
            ? async () => {
                throw jsonError;
            }
            : async () => body
    };
}

test('a later selection ignores an earlier successful response', async () => {
    const first = deferred();
    const second = deferred();
    const states = [];
    const signals = [];
    const fetchImpl = (url, { signal }) => {
        signals.push(signal);
        return url.endsWith('/FIRST') ? first.promise : second.promise;
    };
    const request = createRestStopDetailRequest({ fetchImpl, onState: (state) => states.push(state) });

    const firstLoad = request.load('FIRST');
    const secondLoad = request.load('SECOND');
    second.resolve(response({ body: { code: 'SUCCESS', data: { unitName: '두 번째 상세' } } }));
    await secondLoad;
    first.resolve(response({ body: { code: 'SUCCESS', data: { unitName: '첫 번째 상세' } } }));
    await firstLoad;

    assert.deepEqual(states, [
        { status: 'loading' },
        { status: 'loading' },
        { status: 'success', data: { unitName: '두 번째 상세' } }
    ]);
    assert.equal(signals[0].aborted, true);
    assert.equal(signals[1].aborted, false);
});

test('a later selection ignores an earlier request error', async () => {
    const first = deferred();
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: (url) => url.endsWith('/FIRST')
            ? first.promise
            : Promise.resolve(response({ body: { code: 'SUCCESS', data: { unitName: '두 번째 상세' } } })),
        onState: (state) => states.push(state)
    });

    const firstLoad = request.load('FIRST');
    await request.load('SECOND');
    first.reject(new Error('network failed'));
    await firstLoad;

    assert.deepEqual(states.at(-1), {
        status: 'success',
        data: { unitName: '두 번째 상세' }
    });
    assert.equal(states.some((state) => state.status === 'error'), false);
});

test('invalidate ignores a response that completes after the panel closes', async () => {
    const pending = deferred();
    const states = [];
    let requestSignal;
    const request = createRestStopDetailRequest({
        fetchImpl: (url, { signal }) => {
            requestSignal = signal;
            return pending.promise;
        },
        onState: (state) => states.push(state)
    });

    const load = request.load('A00001');
    request.invalidate();
    pending.resolve(response({ body: { code: 'SUCCESS', data: { unitName: '상세' } } }));
    await load;

    assert.deepEqual(states, [
        { status: 'loading' }
    ]);
    assert.equal(requestSignal.aborted, true);
});

test('404 with NOT_FOUND emits the not-found state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({
            ok: false,
            status: 404,
            body: { code: 'NOT_FOUND', data: null }
        }),
        onState: (state) => states.push(state)
    });

    await request.load('UNKNOWN');

    assert.deepEqual(states.at(-1), { status: 'not-found' });
});

test('200 with NOT_FOUND emits the error state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({
            body: { code: 'NOT_FOUND', data: null }
        }),
        onState: (state) => states.push(state)
    });

    await request.load('UNKNOWN');

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('404 with SUCCESS emits the error state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({
            ok: false,
            status: 404,
            body: { code: 'SUCCESS', data: { restStopName: '휴게소' } }
        }),
        onState: (state) => states.push(state)
    });

    await request.load('A00001');

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('404 with an unexpected code emits the error state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({
            ok: false,
            status: 404,
            body: { code: 'INVALID_REQUEST', data: null }
        }),
        onState: (state) => states.push(state)
    });

    await request.load('A00001');

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('a 500 response emits the error state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({
            ok: false,
            status: 500,
            body: { code: 'INTERNAL_SERVER_ERROR', data: null }
        }),
        onState: (state) => states.push(state)
    });

    await request.load('A00001');

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('EXTERNAL_API_UNAVAILABLE emits a dedicated state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({
            ok: false,
            status: 503,
            body: { code: 'EXTERNAL_API_UNAVAILABLE', data: null }
        }),
        onState: (state) => states.push(state)
    });

    await request.load('A00001');

    assert.deepEqual(states.at(-1), { status: 'external-unavailable' });
});

test('a fetch failure emits the error state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => {
            throw new Error('network failed');
        },
        onState: (state) => states.push(state)
    });

    await request.load('A00001');

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('invalid JSON emits the error state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({ jsonError: new SyntaxError('invalid JSON') }),
        onState: (state) => states.push(state)
    });

    await request.load('A00001');

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('SUCCESS with null data emits the error state', async () => {
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => response({ body: { code: 'SUCCESS', data: null } }),
        onState: (state) => states.push(state)
    });

    await request.load('A00001');

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('an empty serviceAreaCode does not fetch and emits the error state', async () => {
    let fetchCount = 0;
    const states = [];
    const request = createRestStopDetailRequest({
        fetchImpl: async () => {
            fetchCount += 1;
            return response();
        },
        onState: (state) => states.push(state)
    });

    await request.load('   ');

    assert.equal(fetchCount, 0);
    assert.deepEqual(states, [{ status: 'error' }]);
});

test('serviceAreaCode is trimmed before building the request URL', async () => {
    let requestedUrl;
    const request = createRestStopDetailRequest({
        fetchImpl: async (url) => {
            requestedUrl = url;
            return response({ body: { code: 'SUCCESS', data: { restStopName: '휴게소' } } });
        }
    });

    await request.load('  A00001  ');

    assert.equal(requestedUrl, '/api/rest-stops/A00001');
});
