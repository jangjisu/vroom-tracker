import assert from 'node:assert/strict';
import test from 'node:test';

import {
    fetchAdminDashboard,
    renderDashboard,
    renderDashboardError
} from '../../main/resources/static/js/admin.js';

function documentWithDashboardElements() {
    const elements = new Map([
        ['restStopCount', { textContent: '' }],
        ['latestSalesRankingMonth', { textContent: '' }],
        ['lastSyncStatus', { textContent: '' }],
        ['salesRankingMonthTag', { textContent: '' }]
    ]);
    return {
        getElementById: (id) => elements.get(id),
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
