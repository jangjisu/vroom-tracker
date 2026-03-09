/* ===================================================
   traffic.js — TrafficApiController 호출 및 렌더링
   /api/summary · /api/ranking · /api/hourly-pattern
   =================================================== */

import { setText, showEl, hideEl } from './utils.js';

// ===== 요약 카드 =====

export async function loadSummary() {
    try {
        const res = await fetch('/api/summary');
        if (!res.ok) throw new Error(res.status);
        const d = await res.json();

        setText('totalVolume', d.totalVolume ?? '-');
        setText('sumTm', d.sumTm ? '기준: ' + d.sumTm : '');
        setText('congestedSections', d.congestedSections != null ? d.congestedSections + ' 곳' : '-');
        setText('busiestPlace', d.busiestPlace ?? '-');
        setText('busiestVolume', d.busiestVolume ?? '');
    } catch {
        ['totalVolume', 'congestedSections', 'busiestPlace'].forEach(id => setText(id, '-'));
    }
}

// ===== 랭킹 테이블 =====

export async function loadRanking() {
    try {
        const res = await fetch('/api/ranking');
        if (!res.ok) throw new Error(res.status);
        const items = await res.json();

        if (!items || items.length === 0) {
            hideEl('rankingLoading');
            showEl('rankingError');
            return;
        }

        document.getElementById('rankingBody').innerHTML =
            items.map(item => buildRankingRow(item)).join('');

        hideEl('rankingLoading');
        showEl('rankingTableWrap');
    } catch {
        hideEl('rankingLoading');
        showEl('rankingError');
    }
}

function buildRankingRow(item) {
    const rankBadge = item.rank <= 3
        ? `<span class="rank-badge ${rankClass(item.rank)}">${item.rank}</span>`
        : `<span class="text-muted fw-bold">${item.rank}</span>`;

    const congestionBadgeClass = item.congestionLevel === 'HIGH'
        ? 'bg-danger'
        : item.congestionLevel === 'MEDIUM' ? 'bg-warning text-dark' : 'bg-success';

    const barClass = item.congestionLevel === 'HIGH'
        ? 'bar-high'
        : item.congestionLevel === 'MEDIUM' ? 'bar-medium' : 'bar-low';

    return `<tr data-unitname="${item.unitName ?? ''}" class="ranking-row">
        <td class="text-center align-middle">${rankBadge}</td>
        <td class="align-middle fw-semibold">${item.unitName ?? '-'}</td>
        <td class="align-middle"><span class="div-tag">${item.exDivName ?? '-'}</span></td>
        <td class="align-middle text-end fw-bold">${item.formattedVolume ?? '-'}</td>
        <td class="align-middle text-center">
            <span class="badge ${congestionBadgeClass}">${item.congestionLabel ?? '-'}</span>
        </td>
        <td class="align-middle">
            <div class="traffic-bar-bg">
                <div class="traffic-bar ${barClass}" style="width:${item.barWidth ?? 0}%"></div>
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

// ===== 시간대별 패턴 =====

export async function loadHourlyPattern() {
    const currentHour = new Date().getHours();

    try {
        const res = await fetch('/api/hourly-pattern');
        if (!res.ok) throw new Error(res.status);
        const items = await res.json();

        if (!items || items.length === 0) {
            hideEl('hourlyLoading');
            showEl('hourlyError');
            return;
        }

        document.getElementById('hourlyBody').innerHTML = items.map(item => {
            const isCurrentHour = String(item.hour) === String(currentHour);
            return `<tr class="${isCurrentHour ? 'table-warning fw-semibold' : ''}">
                <td>${item.dayType ?? '-'}</td>
                <td>${item.periodRange ?? '-'}</td>
                <td class="text-center">${item.hour != null ? item.hour + '시' : '-'}</td>
                <td class="text-end">${item.formattedVehicleCount ?? '-'}</td>
            </tr>`;
        }).join('');

        hideEl('hourlyLoading');
        showEl('hourlyTableWrap');
    } catch {
        hideEl('hourlyLoading');
        showEl('hourlyError');
    }
}
