import { setText, showApiUnavailableAlert } from './utils.js';

const MAP_CONFIG_ENDPOINT = '/api/map-config';
const REST_STOPS_ENDPOINT = '/api/rest-stops';
const NAVER_MAPS_SCRIPT_ID = 'naverMapsScript';
const NAVER_MAPS_SCRIPT_URL = 'https://oapi.map.naver.com/openapi/v3/maps.js';
const SEOUL_CENTER = {
    latitude: 37.5665,
    longitude: 126.978
};
const DEFAULT_ZOOM = 11;

let map;
let naverMaps;
let selectedInfoWindow;

export async function initRestStopMap() {
    const mapElement = document.getElementById('restStopMap');
    if (!mapElement) {
        return;
    }

    bindDetailPanelClose();

    try {
        const mapConfig = await fetchMapConfig();
        if (!mapConfig.naverMapsNcpKeyId) {
            showMapError('네이버 지도 API 키 설정이 필요합니다. NAVER_MAPS_NCP_KEY_ID 값을 확인해주세요.', '지도 설정 필요');
            return;
        }

        setText('restStopMapStatus', '위치 확인 중');
        await loadNaverMapsScript(mapConfig.naverMapsNcpKeyId);
        naverMaps = window.naver?.maps;
        if (!naverMaps) {
            showMapError('네이버 지도 스크립트를 불러오지 못했습니다. 지도 API 키와 서비스 URL 등록을 확인해주세요.', '지도 로딩 실패');
            return;
        }

        const initialCenter = await resolveInitialCenter();
        map = createMap(mapElement, naverMaps, initialCenter);

        setText('restStopMapStatus', '휴게소 불러오는 중');
        const restStops = await fetchRestStops();
        renderRestStops(restStops);
    } catch (error) {
        console.error(error);
        showMapError('휴게소 위치를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
    }
}

async function fetchMapConfig() {
    const response = await fetch(MAP_CONFIG_ENDPOINT);
    const body = await response.json();

    if (!response.ok || body.code !== 'SUCCESS') {
        throw new Error(`Map config API failed: ${body.code}`);
    }

    return body.data ?? {};
}

function loadNaverMapsScript(ncpKeyId) {
    if (window.naver?.maps) {
        return Promise.resolve();
    }

    document.getElementById(NAVER_MAPS_SCRIPT_ID)?.remove();

    const script = document.createElement('script');
    script.id = NAVER_MAPS_SCRIPT_ID;
    script.src = `${NAVER_MAPS_SCRIPT_URL}?ncpKeyId=${encodeURIComponent(ncpKeyId)}`;
    script.async = true;
    document.head.appendChild(script);

    return waitForScriptLoad(script);
}

function waitForScriptLoad(script) {
    return new Promise((resolve, reject) => {
        script.addEventListener('load', resolve, { once: true });
        script.addEventListener('error', reject, { once: true });
    });
}

function resolveInitialCenter() {
    return new Promise((resolve) => {
        let resolved = false;
        const fallbackTimer = setTimeout(() => resolveOnce(SEOUL_CENTER), 4000);
        const resolveOnce = (center) => {
            if (resolved) {
                return;
            }

            resolved = true;
            clearTimeout(fallbackTimer);
            resolve(center);
        };

        if (!navigator.geolocation) {
            resolveOnce(SEOUL_CENTER);
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                resolveOnce({
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                });
            },
            () => resolveOnce(SEOUL_CENTER),
            {
                enableHighAccuracy: false,
                maximumAge: 300000,
                timeout: 3000
            }
        );
    });
}

function createMap(mapElement, mapsApi, initialCenter) {
    return new mapsApi.Map(mapElement, {
        center: new mapsApi.LatLng(initialCenter.latitude, initialCenter.longitude),
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
            if (selectedInfoWindow) {
                selectedInfoWindow.close();
            }

            infoWindow.open(map, marker);
            selectedInfoWindow = infoWindow;
            openDetailPanel(restStop);
            map.panTo(position);
        });

        markerCount += 1;
    });

    setText('restStopMapStatus', `${markerCount.toLocaleString()}개 표시`);
}

function createPopupContent(restStop) {
    return `
        <div class="rest-stop-map-popup">
            ${escapeHtml(restStop.unitName)}
        </div>
    `;
}

function bindDetailPanelClose() {
    const closeButton = document.getElementById('restStopDetailClose');
    if (!closeButton) {
        return;
    }

    closeButton.addEventListener('click', () => {
        closeDetailPanel();
        if (selectedInfoWindow) {
            selectedInfoWindow.close();
            selectedInfoWindow = undefined;
        }
    });
}

function openDetailPanel(restStop) {
    const panel = document.getElementById('restStopDetailPanel');
    if (!panel) {
        return;
    }

    setText('restStopDetailName', restStop.unitName);
    setText('restStopDetailRoute', restStop.routeName);
    setText('restStopDetailUnitCode', restStop.unitCode);
    setText('restStopDetailStdRestCode', restStop.stdRestCd);
    setText('restStopDetailServiceAreaCode', restStop.serviceAreaCode);
    setText('restStopDetailCoordinates', `${restStop.yValue}, ${restStop.xValue}`);
    panel.classList.remove('d-none');
}

function closeDetailPanel() {
    const panel = document.getElementById('restStopDetailPanel');
    if (panel) {
        panel.classList.add('d-none');
    }
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
