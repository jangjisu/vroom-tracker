function foodsEndpoint(serviceAreaCode) {
    return `/api/admin/rest-stops/${encodeURIComponent(serviceAreaCode)}/foods`;
}

function foodEndpoint(serviceAreaCode, foodId) {
    return `${foodsEndpoint(serviceAreaCode)}/${encodeURIComponent(foodId)}`;
}

function overrideEndpoint(serviceAreaCode, foodId) {
    return `${foodEndpoint(serviceAreaCode, foodId)}/override`;
}

function imageEndpoint(serviceAreaCode, foodId) {
    return `${foodEndpoint(serviceAreaCode, foodId)}/image`;
}

export async function fetchAdminRestFoods(serviceAreaCode, fetchImpl = fetch) {
    try {
        const response = await fetchImpl(foodsEndpoint(serviceAreaCode), { headers: { Accept: 'application/json' } });
        const body = await response.json();

        if (response.ok && body?.code === 'SUCCESS' && Array.isArray(body.data)) {
            return { status: 'success', foods: body.data };
        }

        return { status: 'error' };
    } catch {
        return { status: 'error' };
    }
}

function jsonMutationOptions(method, csrf, payload) {
    return {
        method,
        headers: { [csrf.headerName]: csrf.token, 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    };
}

async function parseFoodMutationResponse(response) {
    const body = await response.json();

    if (response.status === 404 && body?.code === 'NOT_FOUND') {
        return { status: 'not-found' };
    }

    if (response.status === 400 && body?.code === 'INVALID_PARAMETER') {
        return { status: 'invalid', message: body?.message || '입력값을 확인해주세요.' };
    }

    if (response.ok && body?.code === 'SUCCESS' && body?.data && typeof body.data === 'object') {
        return { status: 'success', food: body.data };
    }

    return { status: 'error' };
}

export async function createAdminRestFood(serviceAreaCode, payload, csrf, fetchImpl = fetch) {
    try {
        const response = await fetchImpl(foodsEndpoint(serviceAreaCode), jsonMutationOptions('POST', csrf, payload));
        return await parseFoodMutationResponse(response);
    } catch {
        return { status: 'error' };
    }
}

export async function updateAdminRestFood(serviceAreaCode, foodId, payload, csrf, fetchImpl = fetch) {
    try {
        const response = await fetchImpl(
            foodEndpoint(serviceAreaCode, foodId),
            jsonMutationOptions('PUT', csrf, payload)
        );
        return await parseFoodMutationResponse(response);
    } catch {
        return { status: 'error' };
    }
}

export async function clearAdminRestFoodOverride(serviceAreaCode, foodId, csrf, fetchImpl = fetch) {
    try {
        const response = await fetchImpl(overrideEndpoint(serviceAreaCode, foodId), {
            method: 'DELETE',
            headers: { [csrf.headerName]: csrf.token }
        });
        return await parseFoodMutationResponse(response);
    } catch {
        return { status: 'error' };
    }
}

async function parseVoidResponse(response) {
    if (response.status === 404) {
        return { status: 'not-found' };
    }
    if (response.status === 400) {
        return { status: 'invalid' };
    }
    if (response.ok) {
        return { status: 'success' };
    }
    return { status: 'error' };
}

export async function deleteAdminRestFood(serviceAreaCode, foodId, csrf, fetchImpl = fetch) {
    try {
        const response = await fetchImpl(foodEndpoint(serviceAreaCode, foodId), {
            method: 'DELETE',
            headers: { [csrf.headerName]: csrf.token }
        });
        return await parseVoidResponse(response);
    } catch {
        return { status: 'error' };
    }
}

export async function saveAdminRestFoodImage(serviceAreaCode, foodId, file, csrf, fetchImpl = fetch) {
    const body = new globalThis.FormData();
    body.append('file', file);
    try {
        const response = await fetchImpl(imageEndpoint(serviceAreaCode, foodId), {
            method: 'PUT',
            headers: { [csrf.headerName]: csrf.token },
            body
        });
        return await parseVoidResponse(response);
    } catch {
        return { status: 'error' };
    }
}

export async function deleteAdminRestFoodImage(serviceAreaCode, foodId, csrf, fetchImpl = fetch) {
    try {
        const response = await fetchImpl(imageEndpoint(serviceAreaCode, foodId), {
            method: 'DELETE',
            headers: { [csrf.headerName]: csrf.token }
        });
        return await parseVoidResponse(response);
    } catch {
        return { status: 'error' };
    }
}

export async function fetchAdminRestFoodImage(serviceAreaCode, foodId, fetchImpl = fetch) {
    try {
        const response = await fetchImpl(imageEndpoint(serviceAreaCode, foodId));
        if (response.status === 204) {
            return { status: 'empty' };
        }
        if (response.status === 404) {
            return { status: 'not-found' };
        }
        if (!response.ok) {
            return { status: 'error' };
        }
        return { status: 'success', blob: await response.blob() };
    } catch {
        return { status: 'error' };
    }
}
