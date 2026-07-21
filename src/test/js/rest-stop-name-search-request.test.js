import assert from 'node:assert/strict';
import test from 'node:test';

import { createRestStopNameSearchRequest } from '../../main/resources/static/js/rest-stop-name-search-request.js';

function jsonResponse(status, body) {
    return { status, ok: status >= 200 && status < 300, json: async () => body };
}

function collect() {
    const states = [];
    return { states, onState: (state) => states.push(state) };
}

test('빈 검색어는 error 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createRestStopNameSearchRequest({ fetchImpl: async () => jsonResponse(200, {}), onState }).load('   ');
    assert.deepEqual(states, [{ status: 'error' }]);
});

test('성공 응답이면 loading 후 success와 restStops 배열을 낸다', async () => {
    const restStops = [{ unitName: '서울만남(부산)휴게소', serviceAreaCode: 'A00001' }];
    const { states, onState } = collect();
    await createRestStopNameSearchRequest({
        fetchImpl: async (url) => {
            assert.equal(url, '/api/rest-stops/search?name=%EC%84%9C%EC%9A%B8%EB%A7%8C%EB%82%A8');
            return jsonResponse(200, { code: 'SUCCESS', data: restStops });
        },
        onState
    }).load('서울만남');

    assert.deepEqual(states[0], { status: 'loading' });
    assert.deepEqual(states[1], { status: 'success', restStops });
});

test('빈 배열도 success로 낸다(결과 없음)', async () => {
    const { states, onState } = collect();
    await createRestStopNameSearchRequest({
        fetchImpl: async () => jsonResponse(200, { code: 'SUCCESS', data: [] }),
        onState
    }).load('없는이름');

    assert.deepEqual(states.at(-1), { status: 'success', restStops: [] });
});

test('네트워크 실패는 error 상태를 낸다', async () => {
    const { states, onState } = collect();
    await createRestStopNameSearchRequest({
        fetchImpl: async () => {
            throw new Error('down');
        },
        onState
    }).load('부산');

    assert.equal(states.at(-1).status, 'error');
});

test('AbortError는 상태를 내지 않는다', async () => {
    const { states, onState } = collect();
    await createRestStopNameSearchRequest({
        fetchImpl: async () => {
            const error = new Error('aborted');
            error.name = 'AbortError';
            throw error;
        },
        onState
    }).load('부산');

    assert.deepEqual(states, [{ status: 'loading' }]);
});

test('이전 요청은 취소되고 마지막 요청 상태만 반영된다', async () => {
    const { states, onState } = collect();
    const request = createRestStopNameSearchRequest({
        fetchImpl: async (_url, { signal } = {}) => new Promise((resolve, reject) => {
            signal?.addEventListener('abort', () => {
                const error = new Error('aborted');
                error.name = 'AbortError';
                reject(error);
            });
            resolve(jsonResponse(200, { code: 'SUCCESS', data: [] }));
        }),
        onState
    });

    const first = request.load('부산');
    const second = request.load('서울');
    await Promise.all([first, second]);

    assert.deepEqual(states, [
        { status: 'loading' },
        { status: 'loading' },
        { status: 'success', restStops: [] }
    ]);
});
