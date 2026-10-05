import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

export type PaymentStatus = 'PAID' | 'FAILED';

export interface Payment {
  id: number;
  account: string;
  amount: number;
  commissionAmount: number;
  totalAmount: number;
  status: PaymentStatus;
  createdDate: string;
}

export interface PaymentPage {
  content: Payment[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface BalanceResponse {
  balance: number;
}

export interface CreatePaymentRequest {
  account: string;
  amount: number;
}

@Injectable({ providedIn: 'root' })
export class PayliteApiService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  private readonly paymentsUrl = this.applicationConfigService.getEndpointFor('api/payments');
  private readonly agentsUrl = this.applicationConfigService.getEndpointFor('api/agents');

  getBalance(): Observable<BalanceResponse> {
    return this.http.get<BalanceResponse>(`${this.agentsUrl}/me/balance`);
  }

  getPayments(page: number, size: number): Observable<PaymentPage> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'createdDate,desc');
    return this.http.get<PaymentPage>(this.paymentsUrl, { params });
  }

  createPayment(request: CreatePaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(this.paymentsUrl, request);
  }

  topUp(agentId: number, amount: number): Observable<unknown> {
    return this.http.post(`${this.agentsUrl}/${agentId}/topup`, { amount });
  }
}
