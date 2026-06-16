const REST_STOPS_ENDPOINT = '/api/rest-stops';

export function createRestStopDetailRequest({ fetchImpl = fetch, onState = () => {} } = {}) {
    let currentRequestId = 0;
    let activeRequestController;

    function emitIfCurrent(requestId, state) {
        if (requestId === currentRequestId) {
            onState(state);
        }
    }

    async function load(serviceAreaCode) {
        activeRequestController?.abort();
        activeRequestController = new globalThis.AbortController();

        const requestId = ++currentRequestId;
        const normalizedServiceAreaCode = typeof serviceAreaCode === 'string'
            ? serviceAreaCode.trim()
            : '';

        if (normalizedServiceAreaCode === '') {
            emitIfCurrent(requestId, { status: 'error' });
            return;
        }

        emitIfCurrent(requestId, { status: 'loading' });

        try {
            const response = await fetchImpl(
                `${REST_STOPS_ENDPOINT}/${encodeURIComponent(normalizedServiceAreaCode)}`,
                { signal: activeRequestController.signal }
            );
            const body = await response.json();

            if (response.status === 404 && body?.code === 'NOT_FOUND') {
                emitIfCurrent(requestId, { status: 'not-found' });
                return;
            }

            if (body?.code === 'EXTERNAL_API_UNAVAILABLE') {
                emitIfCurrent(requestId, { status: 'external-unavailable' });
                return;
            }

            const hasValidData = body?.data !== null
                && typeof body?.data === 'object'
                && !Array.isArray(body.data);
            if (response.ok && body?.code === 'SUCCESS' && hasValidData) {
                emitIfCurrent(requestId, { status: 'success', data: body.data });
                return;
            }

            emitIfCurrent(requestId, { status: 'error' });
        } catch (error) {
            if (error?.name === 'AbortError') {
                return;
            }

            emitIfCurrent(requestId, { status: 'error' });
        }
    }

    async function refreshOilPrice(serviceAreaCode) {
        const normalizedServiceAreaCode = typeof serviceAreaCode === 'string'
            ? serviceAreaCode.trim()
            : '';

        if (normalizedServiceAreaCode === '') {
            return { status: 'error' };
        }

        try {
            const response = await fetchImpl(
                `${REST_STOPS_ENDPOINT}/${encodeURIComponent(normalizedServiceAreaCode)}/oil-price/refresh`,
                { method: 'POST' }
            );
            const body = await response.json();

            if (response.status === 404 && body?.code === 'NOT_FOUND') {
                return { status: 'not-found' };
            }

            if (body?.code === 'EXTERNAL_API_UNAVAILABLE') {
                return { status: 'external-unavailable' };
            }

            const hasValidData = body?.data !== null
                && typeof body?.data === 'object'
                && !Array.isArray(body.data);
            if (response.ok && body?.code === 'SUCCESS' && hasValidData) {
                return { status: 'success', data: body.data };
            }

            return { status: 'error' };
        } catch {
            return { status: 'error' };
        }
    }

    function invalidate() {
        currentRequestId += 1;
        activeRequestController?.abort();
        activeRequestController = undefined;
    }

    return { invalidate, load, refreshOilPrice };
}
