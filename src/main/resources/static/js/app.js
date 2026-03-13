/* ===================================================
   app.js — 진입점 (entry point)
   각 모듈을 import해 DOMContentLoaded 시점에 초기화한다.

   구조:
     app.js          ← 진입점, import 및 초기화 조율
     ├── traffic.js  ← /api/* 호출 및 렌더링 (TrafficApiController 연동)
     ├── index.js    ← index.html 전용 UI (메타, 카운트다운, 검색 필터)
     └── utils.js    ← 공통 DOM 유틸 (setText, showEl, hideEl)
   =================================================== */

import { loadSummary, loadRanking, loadHourlyPattern, loadRegionRanking } from './traffic.js';
import { initPageMeta, startCountdown, initSearchFilter } from './index.js';

document.addEventListener('DOMContentLoaded', () => {
    initPageMeta();
    startCountdown();
    initSearchFilter();

    // 각 섹션 독립 비동기 호출 — 한 섹션 실패가 다른 섹션에 영향 없음
    // 모든 섹션 완료 후 페이지 로더 오버레이 제거
    Promise.allSettled([
        loadSummary(),
        loadRanking(),
        loadRegionRanking(),
        loadHourlyPattern(),
    ]).then(() => {
        const loader = document.getElementById('pageLoader');
        if (loader) {
            loader.classList.add('fade-out');
            loader.addEventListener('transitionend', () => loader.remove(), { once: true });
        }
    });
});
