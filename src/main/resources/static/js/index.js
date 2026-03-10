/* ===================================================
   index.js — index.html 전용 초기화·UI 제어
   페이지 메타, 카운트다운, 검색 필터
   =================================================== */

import { setText } from './utils.js';

export function initPageMeta() {
    setText('pageLoadTime',
        new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' }));

    const currentHourEl = document.getElementById('currentHourLabel');
    if (currentHourEl) {
        currentHourEl.textContent = new Date().getHours() + '시';
    }
}

export function startCountdown() {
    const REFRESH_SECONDS = 300;
    let remaining = REFRESH_SECONDS;
    const countdownEl = document.getElementById('countdown');

    const timer = setInterval(() => {
        remaining--;
        if (countdownEl) countdownEl.textContent = remaining;
        if (remaining <= 0) {
            clearInterval(timer);
            window.location.reload();
        }
    }, 1000);
}

export function initSearchFilter() {
    const input = document.getElementById('unitSearch');
    if (!input) return;

    input.addEventListener('input', () => {
        const keyword = input.value.trim().toLowerCase();
        document.querySelectorAll('.ranking-row').forEach(row => {
            const unitName = (row.dataset.unitname || '').toLowerCase();
            row.classList.toggle('hidden', !!keyword && !unitName.includes(keyword));
        });
    });
}
