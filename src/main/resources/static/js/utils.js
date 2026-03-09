/* ===== DOM 유틸 ===== */

export function setText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

export function showEl(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = '';
}

export function hideEl(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = 'none';
}

export function showApiUnavailableAlert() {
    const existing = document.getElementById('apiUnavailableAlert');
    if (existing) return; // 이미 표시 중이면 중복 생성 안 함

    const alert = document.createElement('div');
    alert.id = 'apiUnavailableAlert';
    alert.className = 'alert alert-warning alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3';
    alert.style.zIndex = '9999';
    alert.style.minWidth = '360px';
    alert.innerHTML = `
        일시적으로 데이터를 가져오지 못했습니다. 잠시 후 다시 시도해주세요.
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    document.body.appendChild(alert);

    setTimeout(() => {
        if (alert.parentNode) alert.parentNode.removeChild(alert);
    }, 5000);
}
