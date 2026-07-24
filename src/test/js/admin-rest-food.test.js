import assert from 'node:assert/strict';
import test from 'node:test';

import { initializeAdminRestFood } from '../../main/resources/static/js/admin-rest-food.js';

function interactiveElement(initial = {}) {
    return {
        disabled: false,
        hidden: false,
        textContent: '',
        value: '',
        className: '',
        dataset: {},
        children: [],
        handlers: {},
        addEventListener(event, handler) {
            this.handlers[event] = handler;
        },
        appendChild(child) {
            this.children.push(child);
            return child;
        },
        replaceChildren(...children) {
            this.children = children;
        },
        ...initial
    };
}

function foodDocument() {
    const csrfInput = { value: 'csrf-token' };
    const addForm = interactiveElement({
        dataset: { csrfHeader: 'X-CSRF-TOKEN' },
        querySelector: () => csrfInput
    });
    const editForm = interactiveElement({
        dataset: { csrfHeader: 'X-CSRF-TOKEN' },
        querySelector: () => csrfInput
    });
    const select = interactiveElement({
        appended: [],
        append(...options) {
            this.appended.push(...options);
        }
    });
    const elements = new Map([
        ['restStopFoodSelect', select],
        ['restStopFoodStatus', interactiveElement()],
        ['restStopFoodListSection', interactiveElement({ hidden: true })],
        ['restStopFoodList', interactiveElement()],
        ['restStopFoodEditSection', interactiveElement({ hidden: true })],
        ['restStopFoodEditForm', editForm],
        ['restStopFoodEditName', interactiveElement()],
        ['restStopFoodEditCost', interactiveElement()],
        ['restStopFoodEditDescription', interactiveElement()],
        ['restStopFoodEditCancel', interactiveElement()],
        ['restStopFoodAddForm', addForm],
        ['restStopFoodAddName', interactiveElement()],
        ['restStopFoodAddCost', interactiveElement()],
        ['restStopFoodAddDescription', interactiveElement()]
    ]);
    return {
        createElement: () => interactiveElement(),
        getElementById: (id) => elements.get(id),
        elements
    };
}

async function flushPromises() {
    await Promise.resolve();
    await Promise.resolve();
}

function restStopListResponse() {
    return {
        ok: true,
        json: async () => ({
            code: 'SUCCESS',
            data: [{ serviceAreaCode: 'A00001', unitName: '서울만남(부산)휴게소' }]
        })
    };
}

function foodsResponse(foods) {
    return { ok: true, json: async () => ({ code: 'SUCCESS', data: foods }) };
}

function syncedFood(overrides = {}) {
    return {
        id: 1,
        foodName: '김치찌개',
        foodCost: '8000',
        description: '얼큰한 맛',
        adminOverridden: false,
        adminCreated: false,
        ...overrides
    };
}

async function selectRestStop(document, fetchImpl, onNotice = () => {}, confirmImpl = () => true) {
    initializeAdminRestFood(document, { fetchImpl, onNotice, confirmImpl });
    await flushPromises();
    const select = document.elements.get('restStopFoodSelect');
    select.value = 'A00001';
    await select.handlers.change();
    return select;
}

test('loads the rest stop picker on init', async () => {
    const document = foodDocument();
    const fetchImpl = async () => restStopListResponse();

    initializeAdminRestFood(document, { fetchImpl, onNotice: () => {} });
    await flushPromises();

    const select = document.elements.get('restStopFoodSelect');
    assert.equal(select.appended.length, 1);
    assert.equal(select.disabled, false);
});

test('selecting a rest stop with no menus shows the empty state', async () => {
    const document = foodDocument();
    const fetchImpl = async (url) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods') {
            return foodsResponse([]);
        }
        throw new Error(`Unexpected request: ${url}`);
    };

    await selectRestStop(document, fetchImpl);

    const list = document.elements.get('restStopFoodList');
    assert.equal(list.children.length, 1);
    assert.equal(list.children[0].className, 'food-list-empty');
    assert.equal(document.elements.get('restStopFoodListSection').hidden, false);
});

test('renders a synced menu with no delete button and an overridden menu with unlock and delete buttons', async () => {
    const document = foodDocument();
    const fetchImpl = async (url) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods') {
            return foodsResponse([
                syncedFood(),
                syncedFood({ id: 2, foodName: '커스텀메뉴', adminOverridden: true, adminCreated: true })
            ]);
        }
        throw new Error(`Unexpected request: ${url}`);
    };

    await selectRestStop(document, fetchImpl);

    const list = document.elements.get('restStopFoodList');
    assert.equal(list.children.length, 2);

    const syncedItem = list.children[0];
    const syncedHeader = syncedItem.children[0];
    assert.equal(syncedHeader.children[0].textContent, '김치찌개');
    assert.equal(syncedHeader.children[1].dataset.state, 'synced');
    const syncedActions = syncedItem.children[2];
    assert.equal(syncedActions.children.length, 1);

    const overriddenItem = list.children[1];
    const overriddenHeader = overriddenItem.children[0];
    assert.equal(overriddenHeader.children[1].dataset.state, 'overridden');
    const overriddenActions = overriddenItem.children[2];
    assert.equal(overriddenActions.children.length, 3);
});

test('clicking edit populates the edit form and shows the edit section', async () => {
    const document = foodDocument();
    const fetchImpl = async (url) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods') {
            return foodsResponse([syncedFood()]);
        }
        throw new Error(`Unexpected request: ${url}`);
    };

    await selectRestStop(document, fetchImpl);

    const item = document.elements.get('restStopFoodList').children[0];
    const editButton = item.children[2].children[0];
    await editButton.handlers.click();

    assert.equal(document.elements.get('restStopFoodEditName').value, '김치찌개');
    assert.equal(document.elements.get('restStopFoodEditCost').value, '8000');
    assert.equal(document.elements.get('restStopFoodEditDescription').value, '얼큰한 맛');
    assert.equal(document.elements.get('restStopFoodEditSection').hidden, false);
});

test('clicking cancel hides the edit section', async () => {
    const document = foodDocument();
    const fetchImpl = async (url) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods') {
            return foodsResponse([syncedFood()]);
        }
        throw new Error(`Unexpected request: ${url}`);
    };

    await selectRestStop(document, fetchImpl);
    const item = document.elements.get('restStopFoodList').children[0];
    await item.children[2].children[0].handlers.click();

    await document.elements.get('restStopFoodEditCancel').handlers.click();

    assert.equal(document.elements.get('restStopFoodEditSection').hidden, true);
});

test('submitting the edit form saves the menu, hides the edit section, and reloads the list', async () => {
    const document = foodDocument();
    let savedRequest;
    let foods = [syncedFood()];
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse(foods);
        }
        if (url === '/api/admin/rest-stops/A00001/foods/1' && options.method === 'PUT') {
            savedRequest = { url, options };
            foods = [syncedFood({ foodName: '수정된메뉴', adminOverridden: true })];
            return {
                ok: true,
                status: 200,
                json: async () => ({ code: 'SUCCESS', data: foods[0] })
            };
        }
        throw new Error(`Unexpected request: ${url} ${options.method}`);
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }));
    const item = document.elements.get('restStopFoodList').children[0];
    await item.children[2].children[0].handlers.click();
    document.elements.get('restStopFoodEditName').value = '수정된메뉴';

    await document.elements.get('restStopFoodEditForm').handlers.submit({ preventDefault() {} });

    assert.equal(savedRequest.options.headers['X-CSRF-TOKEN'], 'csrf-token');
    assert.equal(JSON.parse(savedRequest.options.body).foodName, '수정된메뉴');
    assert.equal(document.elements.get('restStopFoodEditSection').hidden, true);
    assert.deepEqual(notices.at(-1), { message: '메뉴를 수정했습니다.', type: undefined });
    assert.equal(document.elements.get('restStopFoodList').children[0].children[0].children[0].textContent, '수정된메뉴');
});

test('edit save failure with invalid input shows the server message as an error notice', async () => {
    const document = foodDocument();
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse([syncedFood()]);
        }
        if (options.method === 'PUT') {
            return {
                ok: false,
                status: 400,
                json: async () => ({ code: 'INVALID_PARAMETER', message: '메뉴명을 입력해주세요.' })
            };
        }
        throw new Error(`Unexpected request: ${url}`);
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }));
    const item = document.elements.get('restStopFoodList').children[0];
    await item.children[2].children[0].handlers.click();

    await document.elements.get('restStopFoodEditForm').handlers.submit({ preventDefault() {} });

    assert.deepEqual(notices.at(-1), { message: '메뉴명을 입력해주세요.', type: 'error' });
});

test('submitting the add form creates a new menu, clears the fields, and reloads the list', async () => {
    const document = foodDocument();
    let createdRequest;
    let foods = [];
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse(foods);
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && options.method === 'POST') {
            createdRequest = { url, options };
            foods = [syncedFood({ id: 2, foodName: '새메뉴', adminCreated: true, adminOverridden: true })];
            return {
                ok: true,
                status: 200,
                json: async () => ({ code: 'SUCCESS', data: foods[0] })
            };
        }
        throw new Error(`Unexpected request: ${url} ${options.method}`);
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }));
    document.elements.get('restStopFoodAddName').value = '새메뉴';
    document.elements.get('restStopFoodAddCost').value = '5000';

    await document.elements.get('restStopFoodAddForm').handlers.submit({ preventDefault() {} });

    assert.equal(createdRequest.options.headers['X-CSRF-TOKEN'], 'csrf-token');
    assert.equal(JSON.parse(createdRequest.options.body).foodName, '새메뉴');
    assert.equal(document.elements.get('restStopFoodAddName').value, '');
    assert.equal(document.elements.get('restStopFoodAddCost').value, '');
    assert.deepEqual(notices.at(-1), { message: '메뉴를 추가했습니다.', type: undefined });
    assert.equal(document.elements.get('restStopFoodList').children.length, 1);
});

test('clicking delete on an admin-created menu confirms then deletes and reloads the list', async () => {
    const document = foodDocument();
    let deleteCalled = false;
    let foods = [syncedFood({ id: 2, foodName: '커스텀메뉴', adminCreated: true, adminOverridden: true })];
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse(foods);
        }
        if (url === '/api/admin/rest-stops/A00001/foods/2' && options.method === 'DELETE') {
            deleteCalled = true;
            foods = [];
            return { ok: true, status: 204, json: async () => ({}) };
        }
        throw new Error(`Unexpected request: ${url} ${options.method}`);
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }), () => true);
    const item = document.elements.get('restStopFoodList').children[0];
    const deleteButton = item.children[2].children[2];
    await deleteButton.handlers.click();

    assert.equal(deleteCalled, true);
    assert.deepEqual(notices.at(-1), { message: '메뉴를 삭제했습니다.', type: undefined });
    assert.equal(document.elements.get('restStopFoodList').children[0].className, 'food-list-empty');
});

test('clicking delete cancels when the confirmation dialog is declined', async () => {
    const document = foodDocument();
    let deleteCalled = false;
    const foods = [syncedFood({ id: 2, foodName: '커스텀메뉴', adminCreated: true, adminOverridden: true })];
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse(foods);
        }
        if (options.method === 'DELETE') {
            deleteCalled = true;
            return { ok: true, status: 204, json: async () => ({}) };
        }
        throw new Error(`Unexpected request: ${url} ${options.method}`);
    };

    await selectRestStop(document, fetchImpl, () => {}, () => false);
    const item = document.elements.get('restStopFoodList').children[0];
    const deleteButton = item.children[2].children[2];
    await deleteButton.handlers.click();

    assert.equal(deleteCalled, false);
});

test('clicking unlock on an overridden menu clears the override and reloads the list', async () => {
    const document = foodDocument();
    let overrideCleared = false;
    let foods = [syncedFood({ adminOverridden: true })];
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse(foods);
        }
        if (url === '/api/admin/rest-stops/A00001/foods/1/override' && options.method === 'DELETE') {
            overrideCleared = true;
            foods = [syncedFood({ adminOverridden: false })];
            return { ok: true, status: 200, json: async () => ({ code: 'SUCCESS', data: foods[0] }) };
        }
        throw new Error(`Unexpected request: ${url} ${options.method}`);
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }));
    const item = document.elements.get('restStopFoodList').children[0];
    const unlockButton = item.children[2].children[1];
    await unlockButton.handlers.click();

    assert.equal(overrideCleared, true);
    assert.deepEqual(notices.at(-1), { message: '동기화 잠금을 해제했습니다.', type: undefined });
    assert.equal(document.elements.get('restStopFoodList').children[0].children[0].children[1].dataset.state, 'synced');
});

test('uploading a valid image on a menu row saves it', async () => {
    const document = foodDocument();
    let savedImageRequest;
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse([syncedFood()]);
        }
        if (url === '/api/admin/rest-stops/A00001/foods/1/image' && options.method === 'PUT') {
            savedImageRequest = { url, options };
            return { ok: true, status: 204, json: async () => ({}) };
        }
        throw new Error(`Unexpected request: ${url} ${options.method}`);
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }));
    const item = document.elements.get('restStopFoodList').children[0];
    const imageInput = item.children[3].children[0];
    imageInput.files = [{ type: 'image/png', size: 1024 }];
    await imageInput.handlers.change();

    assert.equal(savedImageRequest.options.headers['X-CSRF-TOKEN'], 'csrf-token');
    assert.deepEqual(notices.at(-1), { message: '메뉴 이미지를 저장했습니다.', type: undefined });
});

test('uploading an invalid image type shows an error notice without calling the API', async () => {
    const document = foodDocument();
    let imageRequestCalled = false;
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse([syncedFood()]);
        }
        if (url.endsWith('/image')) {
            imageRequestCalled = true;
        }
        return { ok: true, status: 204, json: async () => ({}) };
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }));
    const item = document.elements.get('restStopFoodList').children[0];
    const imageInput = item.children[3].children[0];
    imageInput.files = [{ type: 'application/pdf', size: 1024 }];
    await imageInput.handlers.change();

    assert.equal(imageRequestCalled, false);
    assert.equal(notices.at(-1).type, 'error');
});

test('deleting a menu image confirms then deletes it', async () => {
    const document = foodDocument();
    let deleteImageCalled = false;
    const fetchImpl = async (url, options = {}) => {
        if (url === '/api/rest-stops') {
            return restStopListResponse();
        }
        if (url === '/api/admin/rest-stops/A00001/foods' && !options.method) {
            return foodsResponse([syncedFood()]);
        }
        if (url === '/api/admin/rest-stops/A00001/foods/1/image' && options.method === 'DELETE') {
            deleteImageCalled = true;
            return { ok: true, status: 204, json: async () => ({}) };
        }
        throw new Error(`Unexpected request: ${url} ${options.method}`);
    };
    const notices = [];

    await selectRestStop(document, fetchImpl, (message, type) => notices.push({ message, type }), () => true);
    const item = document.elements.get('restStopFoodList').children[0];
    const imageDeleteButton = item.children[3].children[1];
    await imageDeleteButton.handlers.click();

    assert.equal(deleteImageCalled, true);
    assert.deepEqual(notices.at(-1), { message: '메뉴 이미지를 삭제했습니다.', type: undefined });
});
