// ===== 자동 갱신 (5분 = 300초) =====
// 캐시 TTL이 5분이므로 그보다 짧게 갱신해도 새 데이터가 없음
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

// ===== 영업소명 텍스트 검색 필터 =====
function filterByUnitName(keyword) {
    const kw = keyword.trim().toLowerCase();
    document.querySelectorAll('.ranking-row').forEach(row => {
        if (!kw) {
            row.classList.remove('hidden');
            return;
        }
        const unitName = (row.dataset.unitname || '').toLowerCase();
        if (unitName.includes(kw)) {
            row.classList.remove('hidden');
        } else {
            row.classList.add('hidden');
        }
    });
}
