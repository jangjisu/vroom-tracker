/* ===================================================
   index.js — index.html 전용 초기화
   =================================================== */

export function startCountdown() {
    const REFRESH_SECONDS = 3600;
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
