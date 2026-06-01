import { setText, showApiUnavailableAlert } from './utils.js';

const REST_STOPS_ENDPOINT = '/api/rest-stops';
const KOREA_CENTER = [36.5, 127.8];
const DEFAULT_ZOOM = 7;

let map;
let naverMaps;

export async function initRestStopMap() {
    const mapElement = document.getElementById('restStopMap');
    if (!mapElement) {
        return;
    }

    naverMaps = window.naver?.maps;
    if (!naverMaps) {
        showMapError('네이버 지도 API 키 설정이 필요합니다. NAVER_MAPS_NCP_KEY_ID 값을 확인해주세요.', '지도 설정 필요');
        return;
    }

    map = createMap(mapElement, naverMaps);

    try {
        const restStops = await fetchRestStops();
        renderRestStops(restStops);
    } catch (error) {
        console.error(error);
        showMapError('휴게소 위치를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
    }
}

function createMap(mapElement, mapsApi) {
    return new mapsApi.Map(mapElement, {
        center: new mapsApi.LatLng(KOREA_CENTER[0], KOREA_CENTER[1]),
        mapDataControl: false,
        scaleControl: true,
        zoom: DEFAULT_ZOOM,
        zoomControl: true,
        zoomControlOptions: {
            position: mapsApi.Position.TOP_LEFT
        }
    });
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
    const positions = [];
    let markerCount = 0;

    restStops.forEach((restStop) => {
        const latitude = Number.parseFloat(restStop.yValue);
        const longitude = Number.parseFloat(restStop.xValue);

        if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
            return;
        }

        const position = new naverMaps.LatLng(latitude, longitude);
        const marker = new naverMaps.Marker({
            map,
            position
        });
        const infoWindow = new naverMaps.InfoWindow({
            content: createPopupContent(restStop)
        });

        naverMaps.Event.addListener(marker, 'click', () => {
            infoWindow.open(map, marker);
        });

        positions.push(position);
        markerCount += 1;
    });

    if (positions.length === 1) {
        map.setCenter(positions[0]);
        map.setZoom(12);
    } else if (positions.length > 1) {
        map.fitBounds(createBounds(positions));
    }

    setText('restStopMapStatus', `${markerCount.toLocaleString()}개 표시`);
}

function createPopupContent(restStop) {
    return `
        <div class="rest-stop-map-popup">
        <strong>${escapeHtml(restStop.unitName)}</strong>
        <div>${escapeHtml(restStop.routeName)}</div>
        <div class="text-secondary">${escapeHtml(restStop.serviceAreaCode)}</div>
        </div>
    `;
}

function createBounds(positions) {
    const latitudes = positions.map((position) => position.lat());
    const longitudes = positions.map((position) => position.lng());
    const southWest = new naverMaps.LatLng(Math.min(...latitudes), Math.min(...longitudes));
    const northEast = new naverMaps.LatLng(Math.max(...latitudes), Math.max(...longitudes));

    return new naverMaps.LatLngBounds(southWest, northEast);
}

function showMapError(message, status = '불러오기 실패') {
    const errorElement = document.getElementById('restStopMapError');
    if (!errorElement) {
        return;
    }

    errorElement.textContent = message;
    errorElement.classList.remove('d-none');
    setText('restStopMapStatus', status);
}

function escapeHtml(value) {
    const element = document.createElement('span');
    element.textContent = value ?? '';
    return element.innerHTML;
}
