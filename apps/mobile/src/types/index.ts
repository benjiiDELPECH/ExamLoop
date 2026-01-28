// Domain types
export interface Goal {
  id: number;
  title: string;
  description?: string;
  isPublic: boolean;
  stats?: GoalStats;
}

export interface GoalStats {
  items: number;
  due: number;
}

export interface Question {
  id: number;
  goalId: number;
  prompt: string;
  type: QuestionType;
  choices?: Choice[];
  difficulty: Difficulty;
  chapter?: string;
}

export interface Choice {
  id: string;
  label: string;
}

export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'OPEN';
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type ThemeMode = 'light' | 'dark' | 'auto';

// API Response types
export interface Profile {
  premium: boolean;
}

export interface Usage {
  reviewsUsed: number;
  reviewsLimit: number;
  remaining: number;
}

export interface Dashboard {
  dueCount: number;
  totalQuestions: number;
  goalsCount: number;
}

export interface BootstrapResponse {
  profile: Profile;
  usage: Usage;
  dashboard: Dashboard;
}

export interface ReviewResponse {
  questionId: number;
  result: 'CORRECT' | 'INCORRECT';
  reviewState: ReviewState;
  usage: Usage;
}

export interface ReviewState {
  masteryLevel: string;
  successCount: number;
  failCount: number;
  nextReviewAt: string;
}

// Session types
export interface SessionStats {
  correct: number;
  incorrect: number;
  total: number;
}
