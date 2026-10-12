import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  toasts = signal<ToastMessage[]>([]);
  private counter = 0;

  show(type: 'success' | 'error' | 'warning' | 'info', message: string, title?: string, duration = 4000) {
    const id = ++this.counter;
    const toast: ToastMessage = { id, type, title, message };
    this.toasts.update(list => [...list, toast]);

    setTimeout(() => {
      this.remove(id);
    }, duration);
  }

  success(message: string, title = 'Thành công') {
    this.show('success', message, title);
  }

  error(message: string, title = 'Có lỗi xảy ra') {
    this.show('error', message, title, 5000);
  }

  warning(message: string, title = 'Cảnh báo') {
    this.show('warning', message, title);
  }

  info(message: string, title = 'Thông báo') {
    this.show('info', message, title);
  }

  remove(id: number) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}
