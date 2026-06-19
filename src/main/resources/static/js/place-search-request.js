const PLACE_SEARCH_ENDPOINT = '/api/place-search';

export function createPlaceSearchRequest({ fetchImpl = fetch, onState = () => {} } = {}) {
    let currentRequestId = 0;
    let activeRequestController;

    function emitIfCurrent(requestId, state) {
        if (requestId === currentRequestId) {
            onState(state);
        }
    }

    async function load(query) {
        activeRequestController?.abort();
        activeRequestController = new globalThis.AbortController();

        const requestId = ++currentRequestId;
        const normalizedQuery = typeof query === 'string' ? query.trim() : '';
        if (normalizedQuery === '') {
            emitIfCurrent(requestId, { status: 'error' });
            return;
        }

        emitIfCurrent(requestId, { status: 'loading' });

        try {
            const response = await fetchImpl(
                `${PLACE_SEARCH_ENDPOINT}?query=${encodeURIComponent(normalizedQuery)}`,
                { signal: activeRequestController.signal }
            );
            const body = await response.json();

            if (body?.code === 'EXTERNAL_API_UNAVAILABLE') {
                emitIfCurrent(requestId, { status: 'external-unavailable' });
                return;
            }

            if (response.ok && body?.code === 'SUCCESS' && Array.isArray(body.data)) {
                emitIfCurrent(requestId, { status: 'success', candidates: body.data });
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
