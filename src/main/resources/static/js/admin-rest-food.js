import { fetchAdminRestStops, validateRestStopImageFile } from './admin-rest-stop-image.js';
import {
    clearAdminRestFoodOverride,
    createAdminRestFood,
    deleteAdminRestFood,
    deleteAdminRestFoodImage,
    fetchAdminRestFoods,
    saveAdminRestFoodImage,
    updateAdminRestFood
} from './admin-rest-food-request.js';

const EMPTY_FOOD_LIST_MESSAGE = '등록된 메뉴가 없습니다.';

function createOption(document, restStop) {
    const option = document.createElement('option');
    option.value = restStop.serviceAreaCode;
    option.textContent = `${restStop.unitName || '이름 정보 없음'} · ${restStop.serviceAreaCode}`;
    return option;
}

function csrfFrom(form) {
    return {
        headerName: form.dataset.csrfHeader || 'X-CSRF-TOKEN',
        token: form.querySelector('input[name="_csrf"]')?.value || ''
    };
}

export function initializeAdminRestFood(document, {
    fetchImpl = fetch,
    confirmImpl = globalThis.confirm,
    onNotice = () => {}
} = {}) {
    const select = document.getElementById('restStopFoodSelect');
    const status = document.getElementById('restStopFoodStatus');
    const listSection = document.getElementById('restStopFoodListSection');
    const list = document.getElementById('restStopFoodList');
    const editSection = document.getElementById('restStopFoodEditSection');
    const editForm = document.getElementById('restStopFoodEditForm');
    const editName = document.getElementById('restStopFoodEditName');
    const editCost = document.getElementById('restStopFoodEditCost');
    const editDescription = document.getElementById('restStopFoodEditDescription');
    const editCancel = document.getElementById('restStopFoodEditCancel');
    const addForm = document.getElementById('restStopFoodAddForm');
    const addName = document.getElementById('restStopFoodAddName');
    const addCost = document.getElementById('restStopFoodAddCost');
    const addDescription = document.getElementById('restStopFoodAddDescription');
    if (!select || !status || !listSection || !list || !editSection || !editForm || !editName || !editCost
        || !editDescription || !editCancel || !addForm || !addName || !addCost || !addDescription) {
        return;
    }

    let editingFoodId = null;

    function pageCsrf() {
        return csrfFrom(addForm);
    }

    function hideEditSection() {
        editSection.hidden = true;
        editingFoodId = null;
    }

    function createFoodItem(food) {
        const item = document.createElement('li');
        item.className = 'food-item';

        const header = document.createElement('div');
        header.className = 'food-item-header';
        const name = document.createElement('span');
        name.className = 'food-item-name';
        name.textContent = food.foodName;
        const badge = document.createElement('span');
        badge.className = 'food-item-badge';
        badge.dataset.state = food.adminOverridden ? 'overridden' : 'synced';
        badge.textContent = food.adminOverridden ? '관리자 수정됨' : '동기화중';
        header.appendChild(name);
        header.appendChild(badge);

        const meta = document.createElement('div');
        meta.className = 'food-item-meta';
        meta.textContent = [food.foodCost, food.description].filter(Boolean).join(' · ');

        const actions = document.createElement('div');
        actions.className = 'food-item-actions';
        const editButton = document.createElement('button');
        editButton.className = 'food-item-edit';
        editButton.type = 'button';
        editButton.textContent = '수정';
        editButton.addEventListener('click', () => {
            editingFoodId = food.id;
            editName.value = food.foodName ?? '';
            editCost.value = food.foodCost ?? '';
            editDescription.value = food.description ?? '';
            editSection.hidden = false;
        });
        actions.appendChild(editButton);

        if (food.adminOverridden) {
            const unlockButton = document.createElement('button');
            unlockButton.className = 'food-item-unlock';
            unlockButton.type = 'button';
            unlockButton.textContent = '잠금 해제';
            unlockButton.addEventListener('click', async () => {
                const result = await clearAdminRestFoodOverride(select.value, food.id, pageCsrf(), fetchImpl);
                if (result.status !== 'success') {
                    onNotice('동기화 잠금 해제에 실패했습니다.', 'error');
                    return;
                }
                onNotice('동기화 잠금을 해제했습니다.');
                await loadFoods();
            });
            actions.appendChild(unlockButton);
        }

        if (food.adminCreated) {
            const deleteButton = document.createElement('button');
            deleteButton.className = 'food-item-delete';
            deleteButton.type = 'button';
            deleteButton.textContent = '삭제';
            deleteButton.addEventListener('click', async () => {
                if (!confirmImpl('이 메뉴를 삭제할까요?')) {
                    return;
                }
                const result = await deleteAdminRestFood(select.value, food.id, pageCsrf(), fetchImpl);
                if (result.status !== 'success') {
                    onNotice('메뉴 삭제에 실패했습니다.', 'error');
                    return;
                }
                if (editingFoodId === food.id) {
                    hideEditSection();
                }
                onNotice('메뉴를 삭제했습니다.');
                await loadFoods();
            });
            actions.appendChild(deleteButton);
        }

        const imageRow = document.createElement('div');
        imageRow.className = 'food-item-image-row';
        const imageInput = document.createElement('input');
        imageInput.className = 'food-item-image-input';
        imageInput.type = 'file';
        imageInput.addEventListener('change', async () => {
            const file = imageInput.files?.[0];
            if (!file) {
                return;
            }
            const validationMessage = validateRestStopImageFile(file);
            if (validationMessage) {
                imageInput.value = '';
                onNotice(validationMessage, 'error');
                return;
            }
            const result = await saveAdminRestFoodImage(select.value, food.id, file, pageCsrf(), fetchImpl);
            imageInput.value = '';
            if (result.status !== 'success') {
                onNotice('메뉴 이미지 저장에 실패했습니다.', 'error');
                return;
            }
            onNotice('메뉴 이미지를 저장했습니다.');
        });
        const imageDeleteButton = document.createElement('button');
        imageDeleteButton.className = 'food-item-image-delete';
        imageDeleteButton.type = 'button';
        imageDeleteButton.textContent = '이미지 삭제';
        imageDeleteButton.addEventListener('click', async () => {
            if (!confirmImpl('등록된 메뉴 이미지를 삭제할까요?')) {
                return;
            }
            const result = await deleteAdminRestFoodImage(select.value, food.id, pageCsrf(), fetchImpl);
            if (result.status !== 'success') {
                onNotice('메뉴 이미지 삭제에 실패했습니다.', 'error');
                return;
            }
            onNotice('메뉴 이미지를 삭제했습니다.');
        });
        imageRow.appendChild(imageInput);
        imageRow.appendChild(imageDeleteButton);

        item.appendChild(header);
        item.appendChild(meta);
        item.appendChild(actions);
        item.appendChild(imageRow);
        return item;
    }

    function renderFoodList(foods) {
        if (foods.length === 0) {
            const empty = document.createElement('li');
            empty.className = 'food-list-empty';
            empty.textContent = EMPTY_FOOD_LIST_MESSAGE;
            list.replaceChildren(empty);
            return;
        }
        list.replaceChildren(...foods.map((food) => createFoodItem(food)));
    }

    async function loadFoods() {
        const serviceAreaCode = select.value;
        listSection.hidden = true;
        hideEditSection();
        status.textContent = '';

        if (serviceAreaCode === '') {
            return;
        }

        status.textContent = '메뉴 목록을 불러오고 있습니다.';
        const result = await fetchAdminRestFoods(serviceAreaCode, fetchImpl);

        if (result.status !== 'success') {
            status.textContent = '메뉴 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.';
            return;
        }

        renderFoodList(result.foods);
        status.textContent = '';
        listSection.hidden = false;
    }

    select.addEventListener('change', loadFoods);

    editCancel.addEventListener('click', hideEditSection);

    editForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (editingFoodId === null) {
            return;
        }

        const payload = {
            foodName: editName.value,
            foodCost: editCost.value,
            description: editDescription.value
        };
        const result = await updateAdminRestFood(select.value, editingFoodId, payload, pageCsrf(), fetchImpl);

        if (result.status === 'invalid') {
            onNotice(result.message, 'error');
            return;
        }
        if (result.status !== 'success') {
            onNotice('메뉴 수정에 실패했습니다.', 'error');
            return;
        }

        hideEditSection();
        onNotice('메뉴를 수정했습니다.');
        await loadFoods();
    });

    addForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const serviceAreaCode = select.value;
        if (serviceAreaCode === '') {
            return;
        }

        const payload = {
            foodName: addName.value,
            foodCost: addCost.value,
            description: addDescription.value
        };
        const result = await createAdminRestFood(serviceAreaCode, payload, pageCsrf(), fetchImpl);

        if (result.status === 'invalid') {
            onNotice(result.message, 'error');
            return;
        }
        if (result.status !== 'success') {
            onNotice('메뉴 추가에 실패했습니다.', 'error');
            return;
        }

        addName.value = '';
        addCost.value = '';
        addDescription.value = '';
        onNotice('메뉴를 추가했습니다.');
        await loadFoods();
    });

    fetchAdminRestStops(fetchImpl)
        .then((restStops) => {
            select.append(...restStops.map((restStop) => createOption(document, restStop)));
            select.disabled = false;
        })
        .catch((error) => {
            console.error('휴게소 목록 조회에 실패했습니다.', error);
            status.textContent = '휴게소 목록을 불러오지 못했습니다.';
        });
}
