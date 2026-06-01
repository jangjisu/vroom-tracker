import { setText, showApiUnavailableAlert } from './utils.js';

const REST_STOPS_ENDPOINT = '/api/rest-stops';
const KOREA_CENTER = [36.5, 127.8];
const DEFAULT_ZOOM = 7;

let map;

export async function initRestStopMap() {
    const mapElement = document.getElementById('restStopMap');
    if (!mapElement) {
        return;
    }

    map = createMap(mapElement);

    try {
        const restStops = await fetchRestStops();
        renderRestStops(restStops);
    } catch (error) {
        console.error(error);
        showMapError('휴게소 위치를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
    }
}

function createMap(mapElement) {
    const createdMap = L.map(mapElement).setView(KOREA_CENTER, DEFAULT_ZOOM);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(createdMap);

    return createdMap;
}

async function fetchRestStops() {
    const response = await fetch(REST_STOPS_ENDPOINT);
    const body = await response.json();

    if (body.code === 'EXTERNAL_API_UNAVAILABLE') {
        showApiUnavailableAlert();
        return [];
    }

    if (!response.ok || body.code !== 'SUCCESS') {
        throw new Error(`Rest stop API failed: ${body.code}`);
    }

    return Array.isArray(body.data) ? body.data : [];
}

function renderRestStops(restStops) {
    const bounds = [];
    let markerCount = 0;

    restStops.forEach((restStop) => {
        const latitude = Number.parseFloat(restStop.yValue);
        const longitude = Number.parseFloat(restStop.xValue);

        if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
            return;
        }

        const position = [latitude, longitude];
        L.marker(position)
            .addTo(map)
            .bindPopup(createPopupContent(restStop));
        bounds.push(position);
        markerCount += 1;
    });

    if (bounds.length > 0) {
        map.fitBounds(bounds, { padding: [24, 24] });
    }

    setText('restStopMapStatus', `${markerCount.toLocaleString()}개 표시`);
}

function createPopupContent(restStop) {
    return `
        <strong>${escapeHtml(restStop.unitName)}</strong>
        <div>${escapeHtml(restStop.routeName)}</div>
        <div class="text-secondary">${escapeHtml(restStop.serviceAreaCode)}</div>
    `;
}

function showMapError(message) {
    const errorElement = document.getElementById('restStopMapError');
    if (!errorElement) {
        return;
    }

    errorElement.textContent = message;
    errorElement.classList.remove('d-none');
    setText('restStopMapStatus', '불러오기 실패');
}

function escapeHtml(value) {
    const element = document.createElement('span');
    element.textContent = value ?? '';
    return element.innerHTML;
}
