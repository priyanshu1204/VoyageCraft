import api from './axios';
import type { ExpenseRequest, ExpenseResponse, CategoryBudgetRequest, CategoryBudgetResponse, BudgetSummaryResponse } from '../types/budget';
import type { ApiResponse } from '../types/trip';

export const budgetApi = {
  // Expenses
  addExpense: (data: ExpenseRequest) =>
    api.post<ApiResponse<ExpenseResponse>>('/budget/expenses', data),

  updateExpense: (id: number, data: ExpenseRequest) =>
    api.put<ApiResponse<ExpenseResponse>>(`/budget/expenses/${id}`, data),

  deleteExpense: (id: number) =>
    api.delete<ApiResponse<void>>(`/budget/expenses/${id}`),

  getExpenses: (tripId: number) =>
    api.get<ApiResponse<ExpenseResponse[]>>(`/budget/expenses/trip/${tripId}`),

  // Category Budgets
  setCategoryBudget: (data: CategoryBudgetRequest) =>
    api.post<ApiResponse<CategoryBudgetResponse>>('/budget/category', data),

  getCategoryBudgets: (tripId: number) =>
    api.get<ApiResponse<CategoryBudgetResponse[]>>(`/budget/category/trip/${tripId}`),

  // Summary
  getSummary: (tripId: number) =>
    api.get<ApiResponse<BudgetSummaryResponse>>(`/budget/summary/trip/${tripId}`),

  // Export
  exportCsv: (tripId: number) =>
    api.get<Blob>(`/budget/export/trip/${tripId}`, { responseType: 'blob' }),

  // Currencies
  getCurrencies: () =>
    api.get<ApiResponse<Record<string, number>>>('/budget/currencies'),
};
