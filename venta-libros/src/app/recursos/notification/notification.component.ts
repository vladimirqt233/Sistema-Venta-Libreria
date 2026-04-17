import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificationService } from './notification.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css'
})
export class NotificationComponent implements OnInit, OnDestroy {
  notification: { type: string; message: string } | null = null;
  private subscription: Subscription = new Subscription();

  constructor(private readonly notificationService: NotificationService) {}

  ngOnInit(): void {
    this.subscription = this.notificationService.notification$.subscribe((notification) => {
      this.notification = notification;
      setTimeout(() => {
        this.notification = null;
      }, 3000);
    });
  }

  getClass(type?: string): string {
    switch (type) {
      case 'success':
        return 'success';
      case 'error':
        return 'error';
      case 'info':
        return 'info';
      case 'warning':
        return 'warning';
      default:
        return '';
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
