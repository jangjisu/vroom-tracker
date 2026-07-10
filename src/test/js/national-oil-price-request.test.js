import assert from 'node:assert/strict';
import test from 'node:test';

import { createNationalOilPriceRequest } from '../../main/resources/static/js/national-oil-price-request.js';

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

test('load fetches the national oil summary API and emits success data', async () => {
    const states = [];
    let requestedUrl;
    const request = createNationalOilPriceRequest({
        fetchImpl: async (url) => {
            requestedUrl = url;
            return response({
                body: {
                    code: 'SUCCESS',
                    data: {
                        tradeDate: '2026.07.09',
                        gasoline: { price: '1,700원' }
                    }
                }
            });
        },
        onState: (state) => states.push(state)
    });

    await request.load();

    assert.equal(requestedUrl, '/api/national-oil-prices/summary');
    assert.deepEqual(states, [
        { status: 'loading' },
        {
            status: 'success',
            data: {
                tradeDate: '2026.07.09',
                gasoline: { price: '1,700원' }
            }
        }
    ]);
});

test('EXTERNAL_API_UNAVAILABLE emits a dedicated state', async () => {
    const states = [];
    const request = createNationalOilPriceRequest({
        fetchImpl: async () => response({
            ok: false,
            status: 503,
            body: { code: 'EXTERNAL_API_UNAVAILABLE', data: null }
        }),
        onState: (state) => states.push(state)
    });

    await request.load();

    assert.deepEqual(states.at(-1), { status: 'external-unavailable' });
});

test('invalid responses emit the error state', async () => {
    const states = [];
    const request = createNationalOilPriceRequest({
        fetchImpl: async () => response({ body: { code: 'SUCCESS', data: null } }),
        onState: (state) => states.push(state)
    });

    await request.load();

    assert.deepEqual(states.at(-1), { status: 'error' });
});

test('a later request ignores an earlier response', async () => {
    const first = deferred();
    const second = deferred();
    const states = [];
    const signals = [];
    const request = createNationalOilPriceRequest({
        fetchImpl: (url, { signal }) => {
            signals.push(signal);
            return signals.length === 1 ? first.promise : second.promise;
        },
        onState: (state) => states.push(state)
    });

    const firstLoad = request.load();
    const secondLoad = request.load();
    second.resolve(response({
        body: { code: 'SUCCESS', data: { tradeDate: '2026.07.10', gasoline: { price: '1,800원' } } }
    }));
    await secondLoad;
    first.resolve(response({
        body: { code: 'SUCCESS', data: { tradeDate: '2026.07.09', gasoline: { price: '1,700원' } } }
    }));
    await firstLoad;

    assert.deepEqual(states, [
        { status: 'loading' },
        { status: 'loading' },
        { status: 'success', data: { tradeDate: '2026.07.10', gasoline: { price: '1,800원' } } }
    ]);
    assert.equal(signals[0].aborted, true);
    assert.equal(signals[1].aborted, false);
});

test('invalidate ignores a pending response and emits idle', async () => {
    const pending = deferred();
    const states = [];
    let requestSignal;
    const request = createNationalOilPriceRequest({
        fetchImpl: (url, { signal }) => {
            requestSignal = signal;
            return pending.promise;
        },
        onState: (state) => states.push(state)
    });

    const load = request.load();
    request.invalidate();
    pending.resolve(response({
        body: { code: 'SUCCESS', data: { tradeDate: '2026.07.09', gasoline: { price: '1,700원' } } }
    }));
    await load;

    assert.deepEqual(states, [{ status: 'loading' }, { status: 'idle' }]);
    assert.equal(requestSignal.aborted, true);
});
