export type ExpenseCategory = 'TRANSPORT' | 'STAY' | 'FOOD' | 'ACTIVITIES' | 'SHOPPING' | 'ENTERTAINMENT' | 'INSURANCE' | 'VISA' | 'MISCELLANEOUS';

export interface ExpenseRequest {
  tripId: number;
  title: string;
  description?: string;
  category: ExpenseCategory;
  amount: number;
  currency: string;
  expenseDate: string;
  paidBy?: string;
  receiptUrl?: string;
  notes?: string;
  isReimbursable?: boolean;
}

export interface ExpenseResponse {
  id: number;
  tripId: number;
  title: string;
  description?: string;
  category: ExpenseCategory;
  amount: number;
  currency: string;
  amountInBaseCurrency: number;
  exchangeRate: number;
  expenseDate: string;
  paidBy?: string;
  receiptUrl?: string;
  notes?: string;
  isReimbursable: boolean;
  isReimbursed: boolean;
  createdAt: string;
}

export interface CategoryBudgetRequest {
  tripId: number;
  category: ExpenseCategory;
  allocatedAmount: number;
}

export interface CategoryBudgetResponse {
  id: number | null;
  category: ExpenseCategory;
  allocatedAmount: number;
  spentAmount: number;
  remainingAmount: number;
  percentUsed: number;
}

export interface DailySpendResponse {
  date: string;
  amount: number;
}

export interface BudgetSummaryResponse {
  tripId: number;
  baseCurrency: string;
  totalBudget: number;
  totalSpent: number;
  totalRemaining: number;
  overallPercentUsed: number;
  totalExpenses: number;
  categoryBreakdown: CategoryBudgetResponse[];
  dailySpending: DailySpendResponse[];
}
