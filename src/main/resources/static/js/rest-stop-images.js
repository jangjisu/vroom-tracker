function normalizedImageUrl(value) {
    return typeof value === 'string' ? value.trim() : '';
}

function imageAlt(unitName) {
    const normalizedName = typeof unitName === 'string' ? unitName.trim() : '';
    return normalizedName === '' ? '휴게소 전경' : `${normalizedName} 전경`;
}

export function renderDetailImage(document, detail) {
    const wrapper = document.getElementById('restStopDetailImageWrapper');
    const image = document.getElementById('restStopDetailImage');
    if (!wrapper || !image) {
        return;
    }

    const imageUrl = normalizedImageUrl(detail?.detailImageUrl);
    wrapper.classList.toggle('d-none', imageUrl === '');
    if (imageUrl === '') {
        image.removeAttribute('src');
        image.alt = '';
        return;
    }

    image.src = imageUrl;
    image.alt = imageAlt(detail?.unitName);
}

export function createRouteRestStopImage(document, restStop) {
    const imageUrl = normalizedImageUrl(restStop?.listImageUrl);
    if (imageUrl === '') {
        return null;
    }

    const image = document.createElement('img');
    image.className = 'route-result-image';
    image.src = imageUrl;
    image.alt = imageAlt(restStop?.unitName);
    image.loading = 'lazy';
    return image;
}
