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
