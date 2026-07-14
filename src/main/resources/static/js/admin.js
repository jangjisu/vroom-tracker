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

export async function fetchAdminDashboard(fetchImpl = fetch) {
    const response = await fetchImpl(ADMIN_DASHBOARD_API, { headers: { Accept: 'application/json' } });
    if (!response.ok) {
        throw new Error(`Dashboard request failed: ${response.status}`);
    }
    const payload = await response.json();
    return payload.data || {};
}

export function initializeAdminDashboard(document, fetchImpl = fetch) {
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
