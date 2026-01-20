type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface ToastEventDetail {
    message: string;
    type: ToastType;
    id: number;
}

class ToastEmitter extends EventTarget {
    notify(message: string, type: ToastType = 'info') {
        const event = new CustomEvent<ToastEventDetail>('toast', {
            detail: { message, type, id: Date.now() }
        });
        this.dispatchEvent(event);
    }

    success(message: string) {
        this.notify(message, 'success');
    }

    error(message: string) {
        this.notify(message, 'error');
    }
}

export const toast = new ToastEmitter();
