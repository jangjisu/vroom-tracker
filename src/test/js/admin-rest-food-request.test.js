import assert from 'node:assert/strict';
import test from 'node:test';

import {
    clearAdminRestFoodOverride,
    createAdminRestFood,
    deleteAdminRestFood,
    deleteAdminRestFoodImage,
    fetchAdminRestFoods,
    saveAdminRestFoodImage,
    updateAdminRestFood
} from '../../main/resources/static/js/admin-rest-food-request.js';

function jsonResponse(status, body) {
    return { status, ok: status >= 200 && status < 300, json: async () => body };
}

const csrf = { headerName: 'X-CSRF-TOKEN', token: 'csrf-token' };

test('fetchAdminRestFoods는 성공 시 메뉴 배열을 반환한다', async () => {
    const foods = [{ id: 1, foodName: '커스텀메뉴' }];
    let requestedUrl;
    const result = await fetchAdminRestFoods('A00001', async (url) => {
        requestedUrl = url;
        return jsonResponse(200, { code: 'SUCCESS', data: foods });
    });

    assert.equal(requestedUrl, '/api/admin/rest-stops/A00001/foods');
    assert.deepEqual(result, { status: 'success', foods });
});

test('fetchAdminRestFoods는 네트워크 실패 시 error 상태를 낸다', async () => {
    const result = await fetchAdminRestFoods('A00001', async () => {
        throw new Error('down');
    });

    assert.equal(result.status, 'error');
});

test('createAdminRestFood는 POST로 요청하고 CSRF 헤더를 포함한다', async () => {
    const payload = { foodName: '커스텀메뉴', foodCost: '5000', description: '설명' };
    let capturedOptions;
    const result = await createAdminRestFood(
        'A00001',
        payload,
        csrf,
        async (url, options) => {
            capturedOptions = options;
            assert.equal(url, '/api/admin/rest-stops/A00001/foods');
            return jsonResponse(200, {
                code: 'SUCCESS',
                data: { id: 1, foodName: '커스텀메뉴', adminCreated: true }
            });
        }
    );

    assert.equal(capturedOptions.method, 'POST');
    assert.equal(capturedOptions.headers['X-CSRF-TOKEN'], 'csrf-token');
    assert.deepEqual(JSON.parse(capturedOptions.body), payload);
    assert.equal(result.status, 'success');
    assert.equal(result.food.foodName, '커스텀메뉴');
});

test('updateAdminRestFood는 PUT으로 특정 메뉴를 수정한다', async () => {
    const result = await updateAdminRestFood(
        'A00001',
        1,
        { foodName: '수정메뉴', foodCost: '6000', description: '수정설명' },
        csrf,
        async (url, options) => {
            assert.equal(url, '/api/admin/rest-stops/A00001/foods/1');
            assert.equal(options.method, 'PUT');
            return jsonResponse(200, { code: 'SUCCESS', data: { id: 1, foodName: '수정메뉴' } });
        }
    );

    assert.equal(result.status, 'success');
});

test('updateAdminRestFood는 400 INVALID_PARAMETER면 서버 메시지를 그대로 전달한다', async () => {
    const result = await updateAdminRestFood('A00001', 1, {}, csrf, async () =>
        jsonResponse(400, { code: 'INVALID_PARAMETER', message: '메뉴명을 입력해주세요.' })
    );

    assert.deepEqual(result, { status: 'invalid', message: '메뉴명을 입력해주세요.' });
});

test('updateAdminRestFood는 404면 not-found 상태를 낸다', async () => {
    const result = await updateAdminRestFood('A00001', 99, {}, csrf, async () =>
        jsonResponse(404, { code: 'NOT_FOUND' })
    );

    assert.equal(result.status, 'not-found');
});

test('clearAdminRestFoodOverride는 DELETE .../override를 호출한다', async () => {
    const result = await clearAdminRestFoodOverride('A00001', 1, csrf, async (url, options) => {
        assert.equal(url, '/api/admin/rest-stops/A00001/foods/1/override');
        assert.equal(options.method, 'DELETE');
        return jsonResponse(200, { code: 'SUCCESS', data: { id: 1, adminOverridden: false } });
    });

    assert.equal(result.status, 'success');
});

test('deleteAdminRestFood는 DELETE로 메뉴를 삭제한다', async () => {
    const result = await deleteAdminRestFood('A00001', 1, csrf, async (url, options) => {
        assert.equal(url, '/api/admin/rest-stops/A00001/foods/1');
        assert.equal(options.method, 'DELETE');
        return { status: 204, ok: true };
    });

    assert.equal(result.status, 'success');
});

test('deleteAdminRestFood는 400이면 invalid 상태를 낸다(동기화 메뉴 삭제 시도)', async () => {
    const result = await deleteAdminRestFood('A00001', 1, csrf, async () => ({ status: 400, ok: false }));

    assert.equal(result.status, 'invalid');
});

test('saveAdminRestFoodImage는 multipart로 이미지를 업로드한다', async () => {
    const file = { name: 'image.jpg' };
    let capturedOptions;
    const result = await saveAdminRestFoodImage('A00001', 1, file, csrf, async (url, options) => {
        capturedOptions = options;
        assert.equal(url, '/api/admin/rest-stops/A00001/foods/1/image');
        return { status: 204, ok: true };
    });

    assert.equal(capturedOptions.method, 'PUT');
    assert.equal(capturedOptions.headers['X-CSRF-TOKEN'], 'csrf-token');
    assert.equal(result.status, 'success');
});

test('deleteAdminRestFoodImage는 DELETE로 이미지를 삭제한다', async () => {
    const result = await deleteAdminRestFoodImage('A00001', 1, csrf, async (url, options) => {
        assert.equal(url, '/api/admin/rest-stops/A00001/foods/1/image');
        assert.equal(options.method, 'DELETE');
        return { status: 204, ok: true };
    });

    assert.equal(result.status, 'success');
});
