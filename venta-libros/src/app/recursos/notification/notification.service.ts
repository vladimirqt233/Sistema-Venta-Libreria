import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly notificationSubject = new Subject<NotificationMessage>();
  notification$ = this.notificationSubject.asObservable();

  showSuccess(message: string): void {
    this.notificationSubject.next({ type: 'success', message });
  }

  showError(message: string): void {
    this.notificationSubject.next({ type: 'error', message });
  }

  showInfo(message: string): void {
    this.notificationSubject.next({ type: 'info', message });
  }

  showWarning(message: string): void {
    this.notificationSubject.next({ type: 'warning', message });
  }
}

interface NotificationMessage {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}
