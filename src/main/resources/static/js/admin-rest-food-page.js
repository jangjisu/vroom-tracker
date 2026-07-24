import { initializeAdminRestFood } from './admin-rest-food.js';
import { showToast } from './admin-common.js';

if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        initializeAdminRestFood(document, {
            onNotice: (message, type) => showToast(document, message, type)
        });
    });
}
