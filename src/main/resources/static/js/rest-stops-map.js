import { setText, showApiUnavailableAlert } from './utils.js';
import {
    CONVENIENCE_FALLBACK,
    formatAvailability,
    formatFreightOperation,
    formatParkingCount,
    formatText,
    isMissingValue,
    parseConvenience
} from './rest-stop-detail-formatters.js';
import { createRestStopDetailRequest } from './rest-stop-detail-request.js';

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
let selectedRestStopName = '';
let detailRequest;
let detailPanelEventController;
let mapInitializationId = 0;

export async function initRestStopMap() {
    const mapElement = document.getElementById('restStopMap');
    if (!mapElement) {
        return;
    }

    const initializationId = ++mapInitializationId;
    detailRequest?.invalidate();
    detailPanelEventController?.abort();
    detailPanelEventController = new globalThis.AbortController();
    detailRequest = createRestStopDetailRequest({ onState: renderDetailState });
    bindDetailPanelEvents();

    try {
        const mapConfig = await fetchMapConfig();
        if (initializationId !== mapInitializationId) {
            return;
        }

        if (!mapConfig.naverMapsNcpKeyId) {
            showMapError('네이버 지도 API 키 설정이 필요합니다. NAVER_MAPS_NCP_KEY_ID 값을 확인해주세요.', '지도 설정 필요');
            return;
        }

        setText('restStopMapStatus', '위치 확인 중');
        await loadNaverMapsScript(mapConfig.naverMapsNcpKeyId);
        if (initializationId !== mapInitializationId) {
            return;
        }

        naverMaps = window.naver?.maps;
        if (!naverMaps) {
            showMapError('네이버 지도 스크립트를 불러오지 못했습니다. 지도 API 키와 서비스 URL 등록을 확인해주세요.', '지도 로딩 실패');
            return;
        }

        const initialCenter = await resolveInitialCenter();
        if (initializationId !== mapInitializationId) {
            return;
        }

        map = createMap(mapElement, naverMaps, initialCenter);

        setText('restStopMapStatus', '휴게소 불러오는 중');
        const restStopResult = await fetchRestStops();
        if (initializationId !== mapInitializationId) {
            return;
        }

        if (restStopResult.status === 'external-unavailable') {
            showApiUnavailableAlert();
            renderRestStops(restStopResult.restStops);
            return;
        }

        renderRestStops(restStopResult.restStops);
    } catch (error) {
        if (initializationId !== mapInitializationId) {
            return;
        }

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
        return { status: 'external-unavailable', restStops: [] };
    }

    if (!response.ok || body.code !== 'SUCCESS') {
        throw new Error(`Rest stop API failed: ${body.code}`);
    }

    return {
        status: 'success',
        restStops: Array.isArray(body.data) ? body.data : []
    };
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

export function createPopupContent(restStop) {
    const routeName = formatText(restStop.routeName, '노선 정보 없음');

    return `
        <div class="rest-stop-map-popup-card">
            <div class="rest-stop-map-popup-kicker">선택한 휴게소</div>
            <strong class="rest-stop-map-popup-name">${escapeHtml(restStop.unitName)}</strong>
            <div class="rest-stop-map-popup-route">${escapeHtml(routeName)}</div>
            <div class="rest-stop-map-popup-hint">상세 정보는 오른쪽 패널에서 확인</div>
        </div>
    `;
}

function bindDetailPanelEvents() {
    const closeButton = document.getElementById('restStopDetailClose');
    if (!closeButton || !detailPanelEventController) {
        return;
    }

    closeButton.addEventListener('click', () => {
        closeDetailPanel({ restoreMapFocus: true });
    }, { signal: detailPanelEventController.signal });
    document.addEventListener('keydown', (event) => {
        const panel = document.getElementById('restStopDetailPanel');
        if (event.key === 'Escape' && panel && !panel.classList.contains('d-none')) {
            closeDetailPanel({ restoreMapFocus: true });
        }
    }, { signal: detailPanelEventController.signal });
}

function openDetailPanel(restStop) {
    const panel = document.getElementById('restStopDetailPanel');
    if (!panel || !detailRequest) {
        return;
    }

    selectedRestStopName = restStop.unitName;
    panel.classList.remove('d-none');
    detailRequest.load(restStop.serviceAreaCode);

    if (window.matchMedia('(max-width: 991.98px)').matches) {
        panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

function closeDetailPanel({ restoreMapFocus = false } = {}) {
    detailRequest?.invalidate();

    const panel = document.getElementById('restStopDetailPanel');
    if (panel) {
        panel.classList.add('d-none');
        panel.setAttribute('aria-busy', 'false');
    }

    if (selectedInfoWindow) {
        selectedInfoWindow.close();
        selectedInfoWindow = undefined;
    }

    if (restoreMapFocus) {
        document.getElementById('restStopMap')?.focus();
    }
}

function renderDetailState(state) {
    const panel = document.getElementById('restStopDetailPanel');
    const status = document.getElementById('restStopDetailStatus');
    const content = document.getElementById('restStopDetailContent');
    if (!panel || !status || !content) {
        return;
    }

    panel.classList.remove('d-none');
    setDetailName(selectedRestStopName);
    panel.setAttribute('aria-busy', state.status === 'loading' ? 'true' : 'false');

    if (state.status === 'success') {
        status.textContent = '';
        status.classList.add('d-none');
        content.classList.remove('d-none');
        renderDetail(state.data);
        return;
    }

    if (state.status === 'external-unavailable') {
        showApiUnavailableAlert();
    }

    content.classList.add('d-none');
    status.classList.remove('d-none');
    status.textContent = detailStatusMessage(state.status);
}

function detailStatusMessage(status) {
    if (status === 'loading') {
        return '상세 정보를 불러오는 중입니다.';
    }
    if (status === 'not-found') {
        return '상세 정보가 없습니다.';
    }
    return '상세 정보를 불러오지 못했습니다.';
}

function renderDetail(detail) {
    setDetailName(detail.restStopName, selectedRestStopName);
    setDetailValue('restStopDetailRoute', detail.routeName, '노선 정보 없음');
    setDetailValue('restStopDetailDirection', detail.direction, '방향 정보 없음');
    setDetailValue('restStopDetailAddress', detail.address, '주소 정보 없음');
    renderConvenience(detail.convenience);
    setDetailValue('restStopDetailMaintenance', detail.maintenanceYn, '알 수 없음', formatAvailability);
    setDetailValue('restStopDetailFreight', detail.truckSaYn, '알 수 없음', formatFreightOperation);
    setDetailValue(
        'restStopDetailCompactParking',
        detail.compactCarParkingCount,
        '정보 없음',
        formatParkingCount
    );
    setDetailValue(
        'restStopDetailFullSizeParking',
        detail.fullSizeCarParkingCount,
        '정보 없음',
        formatParkingCount
    );
    setDetailValue(
        'restStopDetailDisabledParking',
        detail.disabledParkingCount,
        '정보 없음',
        formatParkingCount
    );
}

function setDetailName(value, fallbackValue) {
    const element = document.getElementById('restStopDetailName');
    if (!element) {
        return;
    }

    element.textContent = formatText(value, formatText(fallbackValue, '이름 정보 없음'));
    element.classList.toggle(
        'rest-stop-detail-missing',
        isMissingValue(value) && isMissingValue(fallbackValue)
    );
}

function setDetailValue(id, rawValue, fallback, formatter = (value) => formatText(value, fallback)) {
    const element = document.getElementById(id);
    if (!element) {
        return;
    }

    const formattedValue = formatter(rawValue);
    element.textContent = formattedValue;
    element.classList.toggle('rest-stop-detail-missing', isMissingValue(rawValue));
}

function renderConvenience(value) {
    const list = document.getElementById('restStopDetailConvenience');
    const fallback = document.getElementById('restStopDetailConvenienceFallback');
    if (!list || !fallback) {
        return;
    }

    list.replaceChildren();
    const conveniences = parseConvenience(value);
    conveniences.forEach((convenience) => {
        const item = document.createElement('li');
        item.textContent = convenience;
        list.appendChild(item);
    });

    list.classList.toggle('d-none', conveniences.length === 0);
    fallback.classList.toggle('d-none', conveniences.length > 0);
    fallback.textContent = conveniences.length === 0 ? CONVENIENCE_FALLBACK : '';
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
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}
