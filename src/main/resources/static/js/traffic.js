/* ===================================================
   traffic.js — TrafficApiController 호출 및 렌더링
   /api/region-ranking
   =================================================== */

import { showEl, hideEl, showApiUnavailableAlert } from './utils.js';

// ===== 권역별 교통량 순위 =====

export async function loadRegionRanking() {
    try {
        const res = await fetch('/api/region-ranking');
        if (!res.ok) throw new Error(res.status);
        const body = await res.json();
        if (body.code === 'EXTERNAL_API_UNAVAILABLE') {
            showApiUnavailableAlert();
            hideEl('regionLoading');
            showEl('regionError');
            return;
        }
        const items = body.data;

        if (!items || items.length === 0) {
            hideEl('regionLoading');
            showEl('regionError');
            return;
        }

        document.getElementById('regionBody').innerHTML =
            items.map(item => buildRegionRow(item)).join('');

        hideEl('regionLoading');
        showEl('regionTableWrap');
    } catch {
        hideEl('regionLoading');
        showEl('regionError');
    }
}

function buildRegionRow(item) {
    const rankBadge = item.rank <= 3
        ? `<span class="rank-badge ${rankClass(item.rank)}">${item.rank}</span>`
        : `<span class="text-muted fw-bold">${item.rank}</span>`;

    return `<tr>
        <td class="text-center align-middle">${rankBadge}</td>
        <td class="align-middle fw-semibold">${item.regionName ?? '-'}</td>
        <td class="align-middle text-end text-secondary">${item.formattedEntranceVolume ?? '-'}</td>
        <td class="align-middle text-end fw-bold">${item.formattedExitVolume ?? '-'}</td>
        <td class="align-middle">
            <div class="traffic-bar-bg">
                <div class="traffic-bar bar-medium" style="width:${item.barWidth ?? 0}%"></div>
            </div>
        </td>
        <td class="align-middle text-muted small">${item.sumTm ?? '-'}</td>
    </tr>`;
}

function rankClass(rank) {
    if (rank === 1) return 'rank-gold';
    if (rank === 2) return 'rank-silver';
    return 'rank-bronze';
}
