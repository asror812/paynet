import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { Authority } from 'app/config/authority.constants';
import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import SharedModule from 'app/shared/shared.module';
import { FormBuilder } from '@angular/forms';
import { Payment, PayliteApiService } from './paylite-api.service';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, ReactiveFormsModule],
})
export default class HomeComponent implements OnInit {
  readonly account = signal<Account | null>(null);
  readonly balance = signal<number | null>(null);
  readonly payments = signal<Payment[]>([]);
  readonly page = signal(0);
  readonly pageSize = 10;
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly submitting = signal(false);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly isAgent = computed(() => this.account()?.authorities.includes(Authority.AGENT) ?? false);
  readonly isAdmin = computed(() => this.account()?.authorities.includes(Authority.ADMIN) ?? false);

  readonly paymentForm = inject(FormBuilder).group({
    account: ['', [Validators.required, Validators.pattern(/^\d{16}$/)]],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  readonly topUpForm = inject(FormBuilder).group({
    agentId: [null as number | null, [Validators.required, Validators.min(1)]],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  private readonly accountService = inject(AccountService);
  private readonly loginService = inject(LoginService);
  private readonly api = inject(PayliteApiService);

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      this.account.set(account);
      if (account?.authorities.includes(Authority.AGENT)) {
        this.refreshAgentData();
      }
    });
  }

  login(): void {
    this.loginService.login();
  }

  createPayment(): void {
    if (this.paymentForm.invalid || this.submitting()) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    const account = this.paymentForm.controls.account.value!;
    const amount = this.paymentForm.controls.amount.value!;
    this.clearFeedback();
    this.submitting.set(true);

    this.api.createPayment({ account, amount }).subscribe({
      next: payment => {
        this.message.set(`Платёж #${payment.id} успешно выполнен. Списано ${this.formatTiyin(payment.totalAmount)} тийин.`);
        this.paymentForm.reset();
        this.page.set(0);
        this.refreshAgentData();
        this.submitting.set(false);
      },
      error: (response: HttpErrorResponse) => {
        const failedPayment = response.error as Payment | undefined;
        if (response.status === 422 && failedPayment?.status === 'FAILED') {
          this.error.set(`Недостаточно средств. Платёж сохранён со статусом FAILED.`);
          this.page.set(0);
          this.refreshAgentData();
        } else {
          this.error.set(this.readError(response));
        }
        this.submitting.set(false);
      },
    });
  }

  topUp(): void {
    if (this.topUpForm.invalid || this.submitting()) {
      this.topUpForm.markAllAsTouched();
      return;
    }

    const agentId = this.topUpForm.controls.agentId.value!;
    const amount = this.topUpForm.controls.amount.value!;
    this.clearFeedback();
    this.submitting.set(true);

    this.api.topUp(agentId, amount).subscribe({
      next: () => {
        this.message.set(`Баланс агента #${agentId} пополнен на ${this.formatTiyin(amount)} тийин.`);
        this.topUpForm.reset();
        this.submitting.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.error.set(this.readError(response));
        this.submitting.set(false);
      },
    });
  }

  previousPage(): void {
    if (this.page() > 0) {
      this.page.update(value => value - 1);
      this.loadPayments();
    }
  }

  nextPage(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update(value => value + 1);
      this.loadPayments();
    }
  }

  refreshAgentData(): void {
    this.loadBalance();
    this.loadPayments();
  }

  formatTiyin(value: number): string {
    return new Intl.NumberFormat('ru-RU').format(value);
  }

  formatSom(value: number): string {
    return new Intl.NumberFormat('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value / 100);
  }

  private loadBalance(): void {
    this.api.getBalance().subscribe({
      next: response => this.balance.set(response.balance),
      error: response => this.error.set(this.readError(response)),
    });
  }

  private loadPayments(): void {
    this.loading.set(true);
    this.api.getPayments(this.page(), this.pageSize).subscribe({
      next: response => {
        this.payments.set(response.content);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: response => {
        this.error.set(this.readError(response));
        this.loading.set(false);
      },
    });
  }

  private clearFeedback(): void {
    this.message.set(null);
    this.error.set(null);
  }

  private readError(response: HttpErrorResponse): string {
    if (typeof response.error === 'object' && response.error !== null) {
      const body = response.error as Record<string, unknown>;
      if (typeof body['detail'] === 'string') {
        return body['detail'];
      }
      if (typeof body['message'] === 'string') {
        return body['message'];
      }
    }
    return `Ошибка запроса (${response.status})`;
  }
}
