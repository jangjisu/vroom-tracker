/* ===================================================
   app.js — 진입점 (entry point)
   =================================================== */

import { loadRegionRanking } from './traffic.js';
import { startCountdown } from './index.js';

document.addEventListener('DOMContentLoaded', () => {
    startCountdown();

    loadRegionRanking().then(() => {
        const loader = document.getElementById('pageLoader');
        if (loader) {
            loader.classList.add('fade-out');
            loader.addEventListener('transitionend', () => loader.remove(), { once: true });
        }
    });
});
