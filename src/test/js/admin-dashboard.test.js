import assert from 'node:assert/strict';
import test from 'node:test';

import {
    attachAdminForms,
    bindActivityModal,
    fetchAdminDashboard,
    handleRedirectNotice,
    renderDashboard,
    renderDashboardError
} from '../../main/resources/static/js/admin-dashboard.js';

function fakeElement(initial = {}) {
    return {
        textContent: '',
        children: [],
        handlers: {},
        open: false,
        appendChild(child) {
            this.children.push(child);
        },
        replaceChildren(...children) {
            this.children = children;
        },
        addEventListener(event, handler) {
            this.handlers[event] = handler;
        },
        showModal() {
            this.open = true;
        },
        close() {
            this.open = false;
        },
        ...initial
    };
}

function documentWithDashboardElements() {
    const elements = new Map([
        ['restStopCount', fakeElement()],
        ['latestSalesRankingMonth', fakeElement()],
        ['lastSyncStatus', fakeElement()],
        ['salesRankingMonthTag', fakeElement()],
        ['adminActivityList', fakeElement()],
        ['adminActivityModalList', fakeElement()],
        ['adminActivityModal', fakeElement()],
        ['showActivityNotice', fakeElement()],
        ['adminActivityModalClose', fakeElement()]
    ]);
    return {
        getElementById: (id) => elements.get(id),
        createElement: () => fakeElement(),
        querySelectorAll: () => [],
        elements
    };
}

test('fetches the admin dashboard API and returns its data', async () => {
    let requestedUrl;
    const summary = { restStopCount: 203, latestSalesRankingMonth: '2026-06', lastSyncStatus: '준비중' };

    const result = await fetchAdminDashboard(async (url) => {
        requestedUrl = url;
        return { ok: true, json: async () => ({ code: 'SUCCESS', data: summary }) };
    });

    assert.equal(requestedUrl, '/api/admin/dashboard');
    assert.deepEqual(result, summary);
});

test('renders dashboard summary and uses 준비중 when month is missing', () => {
    const document = documentWithDashboardElements();

    renderDashboard(document, { restStopCount: 203, latestSalesRankingMonth: null, lastSyncStatus: '준비중' });

    assert.equal(document.elements.get('restStopCount').textContent, 203);
    assert.equal(document.elements.get('latestSalesRankingMonth').textContent, '준비중');
    assert.equal(document.elements.get('lastSyncStatus').textContent, '준비중');
    assert.equal(document.elements.get('salesRankingMonthTag').textContent, '기준월 없음');
});

test('renders a stable error state when dashboard loading fails', () => {
    const document = documentWithDashboardElements();

    renderDashboardError(document);

    assert.equal(document.elements.get('restStopCount').textContent, '확인 불가');
    assert.equal(document.elements.get('latestSalesRankingMonth').textContent, '확인 불가');
    assert.equal(document.elements.get('lastSyncStatus').textContent, '준비중');
    assert.equal(document.elements.get('salesRankingMonthTag').textContent, '조회 실패');
});

test('shows a product upload success toast and removes redirect parameters', () => {
    const toast = { textContent: '', className: '' };
    const document = {
        title: '관리자',
        getElementById: (id) => id === 'adminToast' ? toast : undefined
    };
    const location = { pathname: '/admin', search: '?upload=success&type=product', hash: '' };
    let replacedUrl;
    const history = { replaceState: (_state, _title, url) => { replacedUrl = url; } };

    handleRedirectNotice(document, location, history);

    assert.equal(toast.textContent, '상품 판매순위 업로드가 완료되었습니다.');
    assert.match(toast.className, /is-success/);
    assert.equal(replacedUrl, '/admin');
});

test('renders up to 5 recent activity logs inline and clears the empty state', () => {
    const document = documentWithDashboardElements();
    const logs = Array.from({ length: 7 }, (_, index) => ({
        actor: 'admin',
        message: `작업 ${index + 1}`,
        occurredAt: '2026-07-21 15:32'
    }));

    renderDashboard(document, { restStopCount: 1, latestSalesRankingMonth: '2026-06', recentActivityLogs: logs });

    const list = document.elements.get('adminActivityList');
    assert.equal(list.children.length, 5);
});

test('restores the empty-state message when there are no activity logs', () => {
    const document = documentWithDashboardElements();

    renderDashboard(document, { restStopCount: 0, latestSalesRankingMonth: null, recentActivityLogs: [] });

    const list = document.elements.get('adminActivityList');
    assert.equal(list.children.length, 1);
    assert.equal(list.children[0].className, 'activity-empty');
});

test('전체 보기 클릭 시 모달에 전체 활동 로그를 렌더링하고 연다', async () => {
    const document = documentWithDashboardElements();
    const logs = Array.from({ length: 7 }, (_, index) => ({
        actor: 'admin',
        message: `작업 ${index + 1}`,
        occurredAt: '2026-07-21 15:32'
    }));

    renderDashboard(document, { restStopCount: 1, recentActivityLogs: logs });
    bindActivityModal(document);
    document.elements.get('showActivityNotice').handlers.click();

    const modal = document.elements.get('adminActivityModal');
    const modalList = document.elements.get('adminActivityModalList');
    assert.equal(modal.open, true);
    assert.equal(modalList.children.length, 7);
});

test('shows the backfill loading overlay and locks the page while submitting', () => {
    const overlay = { classList: { toggle: (_name, value) => { overlay.visible = value; } }, setAttribute: () => {} };
    const message = { textContent: '' };
    const button = { disabled: false, textContent: '' };
    const form = {
        action: '/admin/sales-rankings/backfill',
        dataset: {},
        querySelector: () => button,
        addEventListener: (_event, handler) => { form.submitHandler = handler; }
    };
    const document = {
        getElementById: (id) => ({ adminLoadingOverlay: overlay, adminLoadingMessage: message }[id]),
        querySelectorAll: () => [form]
    };

    attachAdminForms(document);
    form.submitHandler();

    assert.equal(form.dataset.submitting, 'true');
    assert.equal(button.disabled, true);
    assert.equal(button.textContent, '매핑 실행 중...');
    assert.equal(overlay.visible, true);
    assert.equal(message.textContent, '휴게소명 매핑을 실행하고 있습니다.');
});
