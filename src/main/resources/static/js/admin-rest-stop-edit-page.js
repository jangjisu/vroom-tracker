import { initializeAdminRestStopEdit } from './admin-rest-stop-edit.js';
import { showToast } from './admin-common.js';

if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        initializeAdminRestStopEdit(document, {
            onNotice: (message, type) => showToast(document, message, type)
        });
    });
}
