const REST_STOP_SEARCH_ENDPOINT = '/api/rest-stops/search';

export function createRestStopNameSearchRequest({ fetchImpl = fetch, onState = () => {} } = {}) {
    let currentRequestId = 0;
    let activeRequestController;

    function emitIfCurrent(requestId, state) {
        if (requestId === currentRequestId) {
            onState(state);
        }
    }

    async function load(name) {
        activeRequestController?.abort();
        activeRequestController = new globalThis.AbortController();

        const requestId = ++currentRequestId;
        const normalizedName = typeof name === 'string' ? name.trim() : '';
        if (normalizedName === '') {
            emitIfCurrent(requestId, { status: 'error' });
            return;
        }

        emitIfCurrent(requestId, { status: 'loading' });

        try {
            const response = await fetchImpl(
                `${REST_STOP_SEARCH_ENDPOINT}?name=${encodeURIComponent(normalizedName)}`,
                { signal: activeRequestController.signal }
            );
            const body = await response.json();

            if (response.ok && body?.code === 'SUCCESS' && Array.isArray(body.data)) {
                emitIfCurrent(requestId, { status: 'success', restStops: body.data });
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
    }

    return { invalidate, load };
}
