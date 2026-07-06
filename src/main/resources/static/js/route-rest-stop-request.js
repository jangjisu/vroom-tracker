const ROUTE_REST_STOPS_ENDPOINT = '/api/route-rest-stops';

export function createRouteRestStopRequest({ fetchImpl = fetch, onState = () => {} } = {}) {
    let currentRequestId = 0;
    let activeRequestController;

    function emitIfCurrent(requestId, state) {
        if (requestId === currentRequestId) {
            onState(state);
        }
    }

    async function load(originLatitude, originLongitude, destinationQuery, destinationLat, destinationLng, destinationName) {
        activeRequestController?.abort();
        activeRequestController = new globalThis.AbortController();

        const requestId = ++currentRequestId;
        const query = typeof destinationQuery === 'string' ? destinationQuery.trim() : '';
        const hasOrigin = Number.isFinite(originLatitude) && Number.isFinite(originLongitude);
        const hasCoordinates = Number.isFinite(destinationLat) && Number.isFinite(destinationLng);

        if (!hasOrigin || (query === '' && !hasCoordinates)) {
            emitIfCurrent(requestId, { status: 'error' });
            return;
        }

        emitIfCurrent(requestId, { status: 'loading' });

        try {
            let url = `${ROUTE_REST_STOPS_ENDPOINT}?originLat=${originLatitude}&originLng=${originLongitude}`;
            if (hasCoordinates) {
                url += `&destinationLat=${destinationLat}&destinationLng=${destinationLng}`;
                if (destinationName) {
                    url += `&destinationName=${encodeURIComponent(destinationName)}`;
                }
            }
            if (!hasCoordinates) {
                url += `&destinationQuery=${encodeURIComponent(query)}`;
            }
            const response = await fetchImpl(url, { signal: activeRequestController.signal });
            const body = await response.json();

            if (response.status === 404 && body?.code === 'NOT_FOUND') {
                emitIfCurrent(requestId, { status: 'not-found', message: body?.message });
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

    function invalidate() {
        currentRequestId += 1;
        activeRequestController?.abort();
        activeRequestController = undefined;
        onState({ status: 'idle' });
    }

    return { invalidate, load };
}
