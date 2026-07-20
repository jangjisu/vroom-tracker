import assert from 'node:assert/strict';
import test from 'node:test';

import {
    deleteRestStopImage,
    fetchAdminRestStops,
    fetchRestStopImage,
    initializeAdminRestStopImage,
    uploadRestStopImage,
    validateRestStopImageFile
} from '../../main/resources/static/js/admin-rest-stop-image.js';

function interactiveElement(initial = {}) {
    return {
        disabled: false,
        hidden: false,
        textContent: '',
        value: '',
        handlers: {},
        addEventListener(event, handler) {
            this.handlers[event] = handler;
        },
        removeAttribute(name) {
            if (name === 'src') {
                this.src = '';
            }
        },
        setAttribute(name, value) {
            this[name] = value;
        },
        ...initial
    };
}

function adminImageDocument() {
    const csrfInput = { value: 'csrf-token' };
    const form = interactiveElement({
        dataset: { csrfHeader: 'X-CSRF-TOKEN' },
        querySelector: () => csrfInput
    });
    const select = interactiveElement({
        appended: [],
        selectedOptions: [],
        append(...options) {
            this.appended.push(...options);
        }
    });
    const elements = new Map([
        ['restStopImageForm', form],
        ['restStopImageSelect', select],
        ['restStopImageFile', interactiveElement({ files: [] })],
        ['restStopImagePreviewFrame', interactiveElement({ hidden: true })],
        ['restStopImagePreview', interactiveElement({ alt: '', src: '' })],
        ['restStopImageEmpty', interactiveElement()],
        ['restStopImageStatus', interactiveElement()],
        ['restStopImageSubmit', interactiveElement()],
        ['restStopImageDelete', interactiveElement()]
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

test('admin rest stop image validates JPEG and PNG files up to 20MB', () => {
    assert.equal(validateRestStopImageFile({ type: 'image/jpeg', size: 20 * 1024 * 1024 }), null);
    assert.equal(validateRestStopImageFile({ type: 'image/png', size: 1024 }), null);
    assert.equal(validateRestStopImageFile({ type: 'image/webp', size: 1024 }), 'JPEG 또는 PNG 파일만 업로드할 수 있습니다.');
    assert.equal(validateRestStopImageFile({ type: 'image/jpeg', size: 20 * 1024 * 1024 + 1 }), '파일 크기는 20MB 이하여야 합니다.');
});

test('admin rest stop image loads and sorts selectable rest stops', async () => {
    const result = await fetchAdminRestStops(async (url, options) => {
        assert.equal(url, '/api/rest-stops');
        assert.deepEqual(options, { headers: { Accept: 'application/json' } });
        return {
            ok: true,
            json: async () => ({
                code: 'SUCCESS',
                data: [
                    { serviceAreaCode: 'B', unitName: '함안휴게소' },
                    { serviceAreaCode: 'A', unitName: '의왕휴게소' },
                    { serviceAreaCode: null, unitName: '코드없음' }
                ]
            })
        };
    });

    assert.deepEqual(result, [
        { serviceAreaCode: 'A', unitName: '의왕휴게소' },
        { serviceAreaCode: 'B', unitName: '함안휴게소' }
    ]);
});

test('admin rest stop image distinguishes stored, empty and missing rest stops', async () => {
    const stored = await fetchRestStopImage('A 1', async (url, options) => {
        assert.equal(url, '/api/rest-stops/A%201/images/detail');
        assert.deepEqual(options, { cache: 'no-cache' });
        return { ok: true, status: 200, blob: async () => ({ type: 'image/webp' }) };
    });
    const empty = await fetchRestStopImage('A', async () => ({ ok: true, status: 204 }));
    const missing = await fetchRestStopImage('A', async () => ({ ok: false, status: 404 }));

    assert.deepEqual(stored, { status: 'success', blob: { type: 'image/webp' } });
    assert.deepEqual(empty, { status: 'empty' });
    assert.deepEqual(missing, { status: 'not-found' });
});

test('admin rest stop image uploads multipart file with CSRF header', async () => {
    const file = new Blob(['jpeg'], { type: 'image/jpeg' });
    let request;

    await uploadRestStopImage('A00001', file, { headerName: 'X-CSRF-TOKEN', token: 'csrf-token' }, async (url, options) => {
        request = { url, options };
        return { ok: true, status: 204 };
    });

    assert.equal(request.url, '/api/admin/rest-stops/A00001/image');
    assert.equal(request.options.method, 'PUT');
    assert.equal(request.options.headers['X-CSRF-TOKEN'], 'csrf-token');
    assert.equal(request.options.body.get('file').type, 'image/jpeg');
    assert.equal(await request.options.body.get('file').text(), 'jpeg');
});

test('admin rest stop image deletes with CSRF header and reports request failure', async () => {
    let request;
    await deleteRestStopImage('A00001', { headerName: 'X-CSRF-TOKEN', token: 'csrf-token' }, async (url, options) => {
        request = { url, options };
        return { ok: true, status: 204 };
    });

    assert.equal(request.url, '/api/admin/rest-stops/A00001/image');
    assert.equal(request.options.method, 'DELETE');
    assert.equal(request.options.headers['X-CSRF-TOKEN'], 'csrf-token');

    await assert.rejects(
        () => deleteRestStopImage('UNKNOWN', { headerName: 'X-CSRF-TOKEN', token: 'csrf-token' }, async () => ({ ok: false, status: 404 })),
        /404/
    );
});

test('admin rest stop image UI previews, registers and deletes the selected image', async () => {
    const document = adminImageDocument();
    const select = document.elements.get('restStopImageSelect');
    const fileInput = document.elements.get('restStopImageFile');
    const previewFrame = document.elements.get('restStopImagePreviewFrame');
    const empty = document.elements.get('restStopImageEmpty');
    const submit = document.elements.get('restStopImageSubmit');
    const remove = document.elements.get('restStopImageDelete');
    let stored = false;
    const requests = [];
    const notices = [];
    const fetchImpl = async (url, options = {}) => {
        requests.push({ url, options });
        if (url === '/api/rest-stops') {
            return {
                ok: true,
                json: async () => ({
                    code: 'SUCCESS',
                    data: [{ serviceAreaCode: 'A00001', unitName: '의왕휴게소' }]
                })
            };
        }
        if (url === '/api/rest-stops/A00001/images/detail') {
            return stored
                ? { ok: true, status: 200, blob: async () => new Blob(['webp'], { type: 'image/webp' }) }
                : { ok: true, status: 204 };
        }
        if (options.method === 'PUT') {
            stored = true;
            return { ok: true, status: 204 };
        }
        if (options.method === 'DELETE') {
            stored = false;
            return { ok: true, status: 204 };
        }
        throw new Error(`Unexpected request: ${url}`);
    };
    const urlApi = {
        nextId: 0,
        createObjectURL() {
            this.nextId += 1;
            return `blob:preview-${this.nextId}`;
        },
        revokeObjectURL() {}
    };

    initializeAdminRestStopImage(document, {
        fetchImpl,
        urlApi,
        confirmImpl: () => true,
        onNotice: (message, type) => notices.push({ message, type })
    });
    await flushPromises();

    assert.equal(select.appended.length, 1);
    assert.equal(select.disabled, false);
    select.value = 'A00001';
    select.selectedOptions = [{ textContent: '의왕휴게소 · A00001' }];
    await select.handlers.change();
    assert.equal(empty.textContent, '등록된 이미지가 없습니다.');
    assert.equal(submit.textContent, '이미지 등록');
    assert.equal(remove.disabled, true);

    fileInput.files = [new Blob(['jpeg'], { type: 'image/jpeg' })];
    fileInput.handlers.change();
    assert.equal(previewFrame.hidden, false);
    assert.equal(empty.hidden, true);
    assert.equal(document.elements.get('restStopImageStatus').textContent, '업로드 전 미리보기입니다.');

    await document.elements.get('restStopImageForm').handlers.submit({ preventDefault() {} });
    assert.equal(stored, true);
    assert.equal(submit.textContent, '이미지 교체');
    assert.equal(remove.disabled, false);
    assert.deepEqual(notices.at(-1), { message: '휴게소 이미지를 등록했습니다.', type: undefined });
    assert.equal(requests.find((request) => request.options.method === 'PUT').options.headers['X-CSRF-TOKEN'], 'csrf-token');

    await remove.handlers.click();
    assert.equal(stored, false);
    assert.equal(previewFrame.hidden, true);
    assert.equal(empty.textContent, '등록된 이미지가 없습니다.');
    assert.equal(remove.disabled, true);
    assert.deepEqual(notices.at(-1), { message: '휴게소 이미지를 삭제했습니다.', type: undefined });
});
