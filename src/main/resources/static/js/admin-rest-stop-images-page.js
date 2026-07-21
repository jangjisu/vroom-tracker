import { initializeAdminRestStopImage } from './admin-rest-stop-image.js';
import { showToast } from './admin-common.js';

if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        initializeAdminRestStopImage(document, {
            onNotice: (message, type) => showToast(document, message, type)
        });
    });
}
