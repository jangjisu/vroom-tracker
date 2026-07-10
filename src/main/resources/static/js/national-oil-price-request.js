const NATIONAL_OIL_PRICE_SUMMARY_ENDPOINT = '/api/national-oil-prices/summary';

export function createNationalOilPriceRequest({ fetchImpl = fetch, onState = () => {} } = {}) {
    let currentRequestId = 0;
    let activeRequestController;

    function emitIfCurrent(requestId, state) {
        if (requestId === currentRequestId) {
            onState(state);
        }
    }

    async function load() {
        activeRequestController?.abort();
        activeRequestController = new globalThis.AbortController();

        const requestId = ++currentRequestId;
        emitIfCurrent(requestId, { status: 'loading' });

        try {
            const response = await fetchImpl(
                NATIONAL_OIL_PRICE_SUMMARY_ENDPOINT,
                { signal: activeRequestController.signal }
            );
            const body = await response.json();

            if (body?.code === 'EXTERNAL_API_UNAVAILABLE') {
                emitIfCurrent(requestId, { status: 'external-unavailable' });
                return;
            }

            if (response.ok && body?.code === 'SUCCESS' && hasValidData(body)) {
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

    function invalidate() {
        currentRequestId += 1;
        activeRequestController?.abort();
        activeRequestController = undefined;
        onState({ status: 'idle' });
    }

    return { invalidate, load };
}

function hasValidData(body) {
    return body?.data !== null
        && typeof body?.data === 'object'
        && !Array.isArray(body.data);
}
