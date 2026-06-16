import assert from 'node:assert/strict';
import test from 'node:test';

import {
    CONVENIENCE_FALLBACK,
    formatAvailability,
    formatFreightOperation,
    formatOilPrice,
    formatOperationTime,
    formatParkingCount,
    formatRefreshedAt,
    formatText,
    isMissingValue,
    parseConvenience
} from '../../main/resources/static/js/rest-stop-detail-formatters.js';

test('formatText returns the original value when present', () => {
    assert.equal(formatText('서울만남의광장휴게소', '이름 정보 없음'), '서울만남의광장휴게소');
});

test('formatText uses the exact fallback for missing name, route, address and direction values', () => {
    assert.equal(formatText(null, '이름 정보 없음'), '이름 정보 없음');
    assert.equal(formatText('', '노선 정보 없음'), '노선 정보 없음');
    assert.equal(formatText('   ', '주소 정보 없음'), '주소 정보 없음');
    assert.equal(formatText(undefined, '방향 정보 없음'), '방향 정보 없음');
    assert.equal(formatText({}, '이름 정보 없음'), '이름 정보 없음');
    assert.equal(isMissingValue(null), true);
});

test('parseConvenience trims tokens, removes empty values and preserves first occurrence order for tags', () => {
    assert.deepEqual(
        parseConvenience(' 수유실 | ATM | | 수유실 | 편의점 '),
        ['수유실', 'ATM', '편의점']
    );
});

test('parseConvenience returns no tags for missing values so the UI can show 편의시설 정보 없음', () => {
    assert.deepEqual(parseConvenience(null), []);
    assert.deepEqual(parseConvenience('   '), []);
    assert.deepEqual(parseConvenience(['수유실', '샤워실']), []);
    assert.equal(CONVENIENCE_FALLBACK, '편의시설 정보 없음');
});

test('formatAvailability converts maintenance status and falls back for unknown values', () => {
    assert.equal(formatAvailability('O'), '가능');
    assert.equal(formatAvailability('X'), '불가');
    assert.equal(formatAvailability(null), '알 수 없음');
    assert.equal(formatAvailability(''), '알 수 없음');
    assert.equal(formatAvailability('Y'), '알 수 없음');
});

test('formatFreightOperation converts freight operation status and falls back for unknown values', () => {
    assert.equal(formatFreightOperation('O'), '운영');
    assert.equal(formatFreightOperation('X'), '미운영');
    assert.equal(formatFreightOperation(null), '알 수 없음');
    assert.equal(formatFreightOperation(''), '알 수 없음');
    assert.equal(formatFreightOperation('Y'), '알 수 없음');
});

test('formatParkingCount appends the unit and keeps zero as a valid count', () => {
    assert.equal(formatParkingCount(12), '12대');
    assert.equal(formatParkingCount('7'), '7대');
    assert.equal(formatParkingCount(0), '0대');
    assert.equal(formatParkingCount(null), '정보 없음');
    assert.equal(formatParkingCount(''), '정보 없음');
    assert.equal(formatParkingCount('   '), '정보 없음');
    assert.equal(formatParkingCount({}), '정보 없음');
    assert.equal(formatParkingCount(-1), '정보 없음');
    assert.equal(formatParkingCount(1.5), '정보 없음');
});

test('formatOilPrice keeps present price text and falls back for missing values', () => {
    assert.equal(formatOilPrice('1,699원'), '1,699원');
    assert.equal(formatOilPrice(null), '정보 없음');
    assert.equal(formatOilPrice('   '), '정보 없음');
});

test('formatOperationTime joins start and end times when both are present', () => {
    assert.equal(formatOperationTime('00:00', '24:00'), '운영시간 00:00 ~ 24:00');
    assert.equal(formatOperationTime('08:00', ''), '운영시간 정보 없음');
    assert.equal(formatOperationTime(null, '20:00'), '운영시간 정보 없음');
});

test('formatRefreshedAt formats ISO local date time for display', () => {
    assert.equal(formatRefreshedAt('2026-06-16T07:30:00'), '최근 갱신: 2026.06.16 07:30');
    assert.equal(formatRefreshedAt(null), '최근 갱신: 갱신 정보 없음');
    assert.equal(formatRefreshedAt('invalid'), '최근 갱신: 갱신 정보 없음');
});
