import { setGlobalLoading, showToast } from './admin-common.js';

const ADMIN_DASHBOARD_API = '/api/admin/dashboard';
const ACTIVITY_LIST_INLINE_LIMIT = 5;
const ACTIVITY_EMPTY_MESSAGE = '최근 실행한 작업이 없습니다.';

let latestActivityLogs = [];

function setDashboardValue(document, id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

function createActivityItem(document, log) {
    const item = document.createElement('div');
    item.className = 'activity-item';

    const main = document.createElement('div');
    main.className = 'activity-item-main';
    const actor = document.createElement('span');
    actor.className = 'activity-item-actor';
    actor.textContent = log.actor;
    const message = document.createElement('span');
    message.className = 'activity-item-message';
    message.textContent = log.message;
    main.appendChild(actor);
    main.appendChild(message);

    const time = document.createElement('span');
    time.className = 'activity-item-time';
    time.textContent = log.occurredAt;

    item.appendChild(main);
    item.appendChild(time);
    return item;
}

function renderActivityList(document, elementId, logs) {
    const container = document.getElementById(elementId);
    if (!container) {
        return;
    }

    if (logs.length === 0) {
        const empty = document.createElement('div');
        empty.className = 'activity-empty';
        empty.textContent = ACTIVITY_EMPTY_MESSAGE;
        container.replaceChildren(empty);
        return;
    }

    container.replaceChildren(...logs.map((log) => createActivityItem(document, log)));
}

export function openActivityModal(document) {
    renderActivityList(document, 'adminActivityModalList', latestActivityLogs);
    const modal = document.getElementById('adminActivityModal');
    if (modal && !modal.open) {
        modal.showModal();
    }
}

export function bindActivityModal(document) {
    document.getElementById('showActivityNotice')?.addEventListener('click', () => openActivityModal(document));
    document.getElementById('adminActivityModalClose')?.addEventListener('click', () => {
        const modal = document.getElementById('adminActivityModal');
        if (modal?.open) {
            modal.close();
        }
    });
}

export function renderDashboard(document, summary) {
    const latestMonth = summary.latestSalesRankingMonth || '준비중';
    setDashboardValue(document, 'restStopCount', summary.restStopCount ?? '확인 불가');
    setDashboardValue(document, 'latestSalesRankingMonth', latestMonth);
    setDashboardValue(document, 'lastSyncStatus', summary.lastSyncStatus || '준비중');
    setDashboardValue(document, 'salesRankingMonthTag', latestMonth === '준비중' ? '기준월 없음' : `${latestMonth} 기준`);

    latestActivityLogs = summary.recentActivityLogs || [];
    renderActivityList(document, 'adminActivityList', latestActivityLogs.slice(0, ACTIVITY_LIST_INLINE_LIMIT));
}

export function renderDashboardError(document) {
    setDashboardValue(document, 'restStopCount', '확인 불가');
    setDashboardValue(document, 'latestSalesRankingMonth', '확인 불가');
    setDashboardValue(document, 'lastSyncStatus', '준비중');
    setDashboardValue(document, 'salesRankingMonthTag', '조회 실패');
}

function submitButton(form) {
    return form.querySelector('button[type="submit"]');
}

function loadingMessage(form) {
    return form.action.includes('/backfill') ? '휴게소명 매핑을 실행하고 있습니다.' : '파일을 업로드하고 있습니다.';
}

export function attachAdminForms(document) {
    const forms = document.querySelectorAll('form[action*="/admin/sales-rankings/"]');
    forms.forEach((form) => {
        form.addEventListener('submit', () => {
            if (form.dataset.submitting === 'true') {
                return;
            }

            form.dataset.submitting = 'true';
            const button = submitButton(form);
            if (button) {
                button.disabled = true;
                button.textContent = form.action.includes('/backfill') ? '매핑 실행 중...' : '업로드 중...';
            }
            setGlobalLoading(document, true, loadingMessage(form));
        });
    });
}

function redirectNotice(location) {
    const params = new globalThis.URLSearchParams(location.search);
    if (params.get('upload') === 'success') {
        const formType = params.get('type');
        return formType === 'store'
            ? '매장 판매순위 업로드가 완료되었습니다.'
            : '상품 판매순위 업로드가 완료되었습니다.';
    }
    if (params.get('backfill') === 'success') {
        return '전체 휴게소명 매핑이 완료되었습니다.';
    }
    if (params.get('upload') === 'error') {
        return '판매순위 업로드에 실패했습니다.';
    }
    if (params.get('backfill') === 'error') {
        return '전체 휴게소명 매핑에 실패했습니다.';
    }
    return null;
}

export function handleRedirectNotice(document, location = window.location, history = window.history) {
    const message = redirectNotice(location);
    if (!message) {
        return;
    }

    const type = location.search.includes('=error') ? 'error' : 'success';
    showToast(document, message, type);
    const cleanUrl = `${location.pathname}${location.hash || ''}`;
    history.replaceState({}, document.title, cleanUrl);
}

export async function fetchAdminDashboard(fetchImpl = fetch) {
    const response = await fetchImpl(ADMIN_DASHBOARD_API, { headers: { Accept: 'application/json' } });
    if (!response.ok) {
        throw new Error(`Dashboard request failed: ${response.status}`);
    }
    const payload = await response.json();
    return payload.data || {};
}

export function initializeAdminDashboard(document, fetchImpl = fetch) {
    handleRedirectNotice(document);
    attachAdminForms(document);
    bindActivityModal(document);
    fetchAdminDashboard(fetchImpl)
        .then((summary) => renderDashboard(document, summary))
        .catch((error) => {
            console.error('관리자 대시보드 조회에 실패했습니다.', error);
            renderDashboardError(document);
        });
}

if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => initializeAdminDashboard(document));
}
