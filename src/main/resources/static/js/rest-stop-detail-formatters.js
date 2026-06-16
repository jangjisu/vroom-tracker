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
