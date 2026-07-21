import { setGlobalLoading, showToast } from './admin-common.js';

const ADMIN_DASHBOARD_API = '/api/admin/dashboard';

function setDashboardValue(document, id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

export function renderDashboard(document, summary) {
    const latestMonth = summary.latestSalesRankingMonth || '준비중';
    setDashboardValue(document, 'restStopCount', summary.restStopCount ?? '확인 불가');
    setDashboardValue(document, 'latestSalesRankingMonth', latestMonth);
    setDashboardValue(document, 'lastSyncStatus', summary.lastSyncStatus || '준비중');
    setDashboardValue(document, 'salesRankingMonthTag', latestMonth === '준비중' ? '기준월 없음' : `${latestMonth} 기준`);
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
    fetchAdminDashboard(fetchImpl)
        .then((summary) => renderDashboard(document, summary))
        .catch((error) => {
            console.error('관리자 대시보드 조회에 실패했습니다.', error);
            renderDashboardError(document);
        });

    const activityButton = document.getElementById('showActivityNotice');
    if (activityButton) {
        activityButton.addEventListener('click', () => {
            window.alert('최근 작업 이력은 준비중입니다.');
        });
    }
}

if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => initializeAdminDashboard(document));
}
