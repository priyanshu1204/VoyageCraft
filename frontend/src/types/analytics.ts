export interface CategoryBudgetComparison {
  category: string;
  categoryLabel: string;
  emoji: string;
  budgeted: number;
  actual: number;
  variance: number;
  percentUsed: number;
  status: 'UNDER' | 'ON_TRACK' | 'OVER' | 'NO_BUDGET';
}

export interface CategoryTimeAllocation {
  category: string;
  categoryLabel: string;
  emoji: string;
  totalMinutes: number;
  percent: number;
  activityCount: number;
}

export interface DailySpendTrend {
  date: string;
  amount: number;
  expenseCount: number;
}

export interface TripAnalytics {
  tripId: number;
  tripTitle: string;
  currency: string;
  totalDays: number;

  totalBudget: number;
  totalSpent: number;
  totalRemaining: number;
  budgetUsedPercent: number;
  budgetVsActual: CategoryBudgetComparison[];

  totalActivityMinutes: number;
  totalTransitMinutes: number;
  transitPercent: number;
  activityPercent: number;
  freeTimePercent: number;
  timeByCategory: CategoryTimeAllocation[];

  transitActivityRatio: string;
  totalTransportLegs: number;
  totalActivities: number;
  avgTransitMinutesPerDay: number;
  avgActivityMinutesPerDay: number;

  avgDailySpend: number;
  highestSingleExpense: number;
  highestExpenseTitle: string;
  topSpendingCategory: string;
  dailySpendTrend: DailySpendTrend[];
}
