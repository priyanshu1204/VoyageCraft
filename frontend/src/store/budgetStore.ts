import { create } from 'zustand';
import { budgetApi } from '../api/budgetApi';
import type { ExpenseResponse, ExpenseRequest, BudgetSummaryResponse, CategoryBudgetRequest } from '../types/budget';

interface BudgetState {
  expenses: ExpenseResponse[];
  summary: BudgetSummaryResponse | null;
  currencies: Record<string, number>;
  isLoading: boolean;

  fetchExpenses: (tripId: number) => Promise<void>;
  fetchSummary: (tripId: number) => Promise<void>;
  fetchCurrencies: () => Promise<void>;
  addExpense: (data: ExpenseRequest) => Promise<void>;
  updateExpense: (id: number, data: ExpenseRequest) => Promise<void>;
  deleteExpense: (id: number, tripId: number) => Promise<void>;
  setCategoryBudget: (data: CategoryBudgetRequest) => Promise<void>;
  exportCsv: (tripId: number) => Promise<void>;
}

export const useBudgetStore = create<BudgetState>((set, get) => ({
  expenses: [],
  summary: null,
  currencies: {},
  isLoading: false,

  fetchExpenses: async (tripId) => {
    set({ isLoading: true });
    try {
      const res = await budgetApi.getExpenses(tripId);
      set({ expenses: res.data.data });
    } finally {
      set({ isLoading: false });
    }
  },

  fetchSummary: async (tripId) => {
    try {
      const res = await budgetApi.getSummary(tripId);
      set({ summary: res.data.data });
    } catch { /* ignore */ }
  },

  fetchCurrencies: async () => {
    try {
      const res = await budgetApi.getCurrencies();
      set({ currencies: res.data.data });
    } catch { /* ignore */ }
  },

  addExpense: async (data) => {
    await budgetApi.addExpense(data);
    await Promise.all([get().fetchExpenses(data.tripId), get().fetchSummary(data.tripId)]);
  },

  updateExpense: async (id, data) => {
    await budgetApi.updateExpense(id, data);
    await Promise.all([get().fetchExpenses(data.tripId), get().fetchSummary(data.tripId)]);
  },

  deleteExpense: async (id, tripId) => {
    await budgetApi.deleteExpense(id);
    await Promise.all([get().fetchExpenses(tripId), get().fetchSummary(tripId)]);
  },

  setCategoryBudget: async (data) => {
    await budgetApi.setCategoryBudget(data);
    await get().fetchSummary(data.tripId);
  },

  exportCsv: async (tripId) => {
    const res = await budgetApi.exportCsv(tripId);
    const blob = new Blob([res.data], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `trip_${tripId}_expenses.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  },
}));
