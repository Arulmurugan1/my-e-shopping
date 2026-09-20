import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './popup.component.html'
})
export class PopupComponent {
  @Input() visible = false;
  @Input() success = true;
  @Input() title = '';
  @Input() message = '';
  @Output() closed = new EventEmitter<void>();

  close() {
    this.closed.emit();
  }
}
