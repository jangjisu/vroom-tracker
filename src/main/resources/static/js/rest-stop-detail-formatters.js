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
