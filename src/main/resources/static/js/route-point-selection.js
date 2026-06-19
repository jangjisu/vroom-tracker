export const ROUTE_POINT_TARGET = Object.freeze({
    ORIGIN: 'origin',
    DESTINATION: 'destination'
});

const MAP_POINT_NAME = '지도에서 선택한 위치';

export function createRoutePointSelection() {
    let origin;
    let destination;
    let searchTarget = ROUTE_POINT_TARGET.DESTINATION;
    let mapTarget;
    let mapDraft;

    function select(target, point) {
        assertTarget(target);
        const normalized = normalizePoint(point);

        if (target === ROUTE_POINT_TARGET.ORIGIN) {
            origin = normalized;
        }
        if (target === ROUTE_POINT_TARGET.DESTINATION) {
            destination = normalized;
        }

        return normalized;
    }

    function setSearchTarget(target) {
        assertTarget(target);
        searchTarget = target;
    }

    function clear(target) {
        assertTarget(target);
        if (target === ROUTE_POINT_TARGET.ORIGIN) {
            origin = undefined;
        }
        if (target === ROUTE_POINT_TARGET.DESTINATION) {
            destination = undefined;
        }
    }

    function beginMapSelection(target) {
        assertTarget(target);
        mapTarget = target;
        mapDraft = undefined;
    }

    function updateMapDraft(point) {
        mapDraft = normalizePoint({ ...point, name: MAP_POINT_NAME });
        return mapDraft;
    }

    function confirmMapSelection() {
        if (!mapTarget || !mapDraft) {
            return undefined;
        }

        const selected = select(mapTarget, mapDraft);
        cancelMapSelection();
        return selected;
    }

    function cancelMapSelection() {
        mapTarget = undefined;
        mapDraft = undefined;
    }

    return {
        beginMapSelection,
        cancelMapSelection,
        canRequestRoute: () => Boolean(origin && destination),
        clear,
        confirmMapSelection,
        getDestination: () => destination,
        getMapDraft: () => mapDraft,
        getMapTarget: () => mapTarget,
        getOrigin: () => origin,
        getSearchTarget: () => searchTarget,
        select,
        setSearchTarget,
        updateMapDraft
    };
}

function normalizePoint(point) {
    if (!Number.isFinite(point?.latitude) || !Number.isFinite(point?.longitude)) {
        throw new TypeError('Route point coordinates must be finite numbers.');
    }

    return {
        name: normalizedName(point.name),
        address: typeof point.address === 'string' ? point.address.trim() : '',
        latitude: point.latitude,
        longitude: point.longitude
    };
}

function normalizedName(name) {
    if (typeof name === 'string' && name.trim() !== '') {
        return name.trim();
    }
    return '선택한 위치';
}

function assertTarget(target) {
    if (!Object.values(ROUTE_POINT_TARGET).includes(target)) {
        throw new TypeError('Unknown route point target.');
    }
}
