export const CONVENIENCE_FALLBACK = '편의시설 정보 없음';

export function isMissingValue(value) {
    return value === null
        || value === undefined
        || (typeof value !== 'string' && typeof value !== 'number')
        || (typeof value === 'string' && value.trim() === '');
}

export function formatText(value, fallback) {
    return isMissingValue(value) ? fallback : value;
}

export function parseConvenience(value) {
    if (typeof value !== 'string' || isMissingValue(value)) {
        return [];
    }

    return [...new Set(value
        .split('|')
        .map((token) => token.trim())
        .filter((token) => token !== ''))];
}

export function formatAvailability(value) {
    if (value === 'O') {
        return '가능';
    }
    if (value === 'X') {
        return '불가';
    }
    return '알 수 없음';
}

export function formatFreightOperation(value) {
    if (value === 'O') {
        return '운영';
    }
    if (value === 'X') {
        return '미운영';
    }
    return '알 수 없음';
}

export function formatParkingCount(value) {
    const isNumber = typeof value === 'number' && Number.isInteger(value) && value >= 0;
    const isNumericString = typeof value === 'string' && /^\d+$/.test(value.trim());
    const normalizedValue = typeof value === 'string' ? value.trim() : value;
    return isNumber || isNumericString ? `${normalizedValue}대` : '정보 없음';
}

export function formatOilPrice(value) {
    return formatText(value, '정보 없음');
}

export function formatOperationTime(startTime, endTime) {
    if (isMissingValue(startTime) || isMissingValue(endTime)) {
        return '운영시간 정보 없음';
    }

    return `운영시간 ${String(startTime).trim()} ~ ${String(endTime).trim()}`;
}

export function hasFoodMenu(foodMenu) {
    return Boolean(foodMenu) && Array.isArray(foodMenu.menus) && foodMenu.menus.length > 0;
}

export function orderFoodMenus(menus) {
    if (!Array.isArray(menus)) {
        return [];
    }

    const representatives = menus.filter((menu) => menu?.representative);
    const others = menus.filter((menu) => !menu?.representative);
    return [...representatives, ...others];
}

export function formatSeasonLabel(value) {
    const labels = {
        4: '사계절',
        S: '여름',
        W: '겨울'
    };
    const key = String(formatText(value, '')).trim().toUpperCase();
    return labels[key] ?? null;
}

export function formatFoodBadges(menu) {
    if (!menu || typeof menu !== 'object') {
        return [];
    }

    const badges = [];
    if (menu.representative) {
        badges.push('대표');
    }
    if (menu.bestFood) {
        badges.push('베스트');
    }
    if (menu.premium) {
        badges.push('프리미엄');
    }

    const season = formatSeasonLabel(menu.season);
    if (season) {
        badges.push(season);
    }

    return badges;
}

export function formatFoodCost(value) {
    if (isMissingValue(value)) {
        return '가격 정보 없음';
    }

    const text = String(value).trim();
    if (/^\d+$/.test(text)) {
        return `${Number(text).toLocaleString('ko-KR')}원`;
    }

    return text;
}

function parkingCountValue(value) {
    if (!isParkingCount(value)) {
        return null;
    }
    return Number(String(value).trim());
}

export function summarizeParking(detail) {
    const counts = [
        parkingCountValue(detail?.compactCarParkingCount),
        parkingCountValue(detail?.fullSizeCarParkingCount),
        parkingCountValue(detail?.disabledParkingCount)
    ].filter((value) => value !== null);

    if (counts.length === 0) {
        return '주차 정보 없음';
    }

    return `총 ${counts.reduce((sum, value) => sum + value, 0).toLocaleString('ko-KR')}대`;
}

function summarizeConveniences(value) {
    const conveniences = parseConvenience(value);
    if (conveniences.length === 0) {
        return CONVENIENCE_FALLBACK;
    }

    const visible = conveniences.slice(0, 3).join(', ');
    const extraCount = conveniences.length - 3;
    if (extraCount <= 0) {
        return visible;
    }

    return `${visible} 외 ${extraCount}개`;
}

function summarizeOperation(detail) {
    const hasMaintenance = !isMissingValue(detail?.maintenanceYn);
    const hasFreight = !isMissingValue(detail?.truckSaYn);
    if (!hasMaintenance && !hasFreight) {
        return '운영 정보 없음';
    }

    return `경정비 ${formatAvailability(detail?.maintenanceYn)} · 화물휴게소 ${formatFreightOperation(detail?.truckSaYn)}`;
}

export function summarizeDetailHighlights(detail) {
    return [
        {
            key: 'brand',
            label: '입점 브랜드',
            value: formatText(detail?.brand, '입점 브랜드 정보 없음'),
            missing: isMissingValue(detail?.brand)
        },
        {
            key: 'parking',
            label: '주차',
            value: summarizeParking(detail),
            missing: !hasParkingInfo(detail)
        },
        {
            key: 'convenience',
            label: '주요 편의시설',
            value: summarizeConveniences(detail?.convenience),
            missing: parseConvenience(detail?.convenience).length === 0
        },
        {
            key: 'operation',
            label: '운영',
            value: summarizeOperation(detail),
            missing: isMissingValue(detail?.maintenanceYn) && isMissingValue(detail?.truckSaYn)
        }
    ];
}

export const DATA_TAG_DEFINITIONS = [
    { key: 'food', label: '먹거리' },
    { key: 'parking', label: '주차' },
    { key: 'oil', label: '주유' }
];

export function hasOilConveniences(conveniences) {
    return Array.isArray(conveniences) && conveniences.length > 0;
}

export function hasOilInfo(oilInfo) {
    if (!oilInfo || typeof oilInfo !== 'object') {
        return false;
    }

    return !isMissingValue(oilInfo.gasolinePrice)
        || !isMissingValue(oilInfo.dieselPrice)
        || !isMissingValue(oilInfo.lpgPrice)
        || !isMissingValue(oilInfo.oilCompany)
        || !isMissingValue(oilInfo.telNo)
        || !isMissingValue(oilInfo.lastRefreshedAt)
        || hasOilConveniences(oilInfo.oilStationConveniences);
}

function isParkingCount(value) {
    if (typeof value === 'number') {
        return Number.isInteger(value) && value >= 0;
    }
    return typeof value === 'string' && /^\d+$/.test(value.trim());
}

export function hasParkingInfo(detail) {
    if (!detail || typeof detail !== 'object') {
        return false;
    }

    return isParkingCount(detail.compactCarParkingCount)
        || isParkingCount(detail.fullSizeCarParkingCount)
        || isParkingCount(detail.disabledParkingCount);
}

export function availableDataTags(detail) {
    if (!detail || typeof detail !== 'object') {
        return [];
    }

    const present = {
        food: hasFoodMenu(detail.foodMenu),
        parking: hasParkingInfo(detail),
        oil: hasOilInfo(detail.oilInfo)
    };

    return DATA_TAG_DEFINITIONS.filter((tag) => present[tag.key]);
}

export function formatRefreshedAt(value) {
    if (isMissingValue(value)) {
        return '최근 갱신: 갱신 정보 없음';
    }

    const match = String(value).trim().match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
    if (!match) {
        return '최근 갱신: 갱신 정보 없음';
    }

    const [, year, month, day, hour, minute] = match;
    return `최근 갱신: ${year}.${month}.${day} ${hour}:${minute}`;
}
