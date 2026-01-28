/**
 * ExamLoop API Service
 * 
 * Client TypeScript pour l'API ExamLoop
 */

const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080';

// ============================================================================
// TYPES
// ============================================================================

export interface Profile {
  userId: string;
  premium: boolean;
}

export interface Usage {
  date: string;
  reviewsUsed: number;
  reviewsLimit: number;
  remaining: number;
  percentUsed: number;
  premium: boolean;
}

export interface Dashboard {
  dueCount: number;
  totalQuestions: number;
  goalsCount: number;
  masteredCount: number;
}

export interface BootstrapResponse {
  profile: Profile;
  usage: Usage;
  dashboard: Dashboard;
}

export interface Goal {
  id: number;
  title: string;
  description: string | null;
  isPublic: boolean;
  stats: {
    items: number;
    due: number;
  };
}

export interface Question {
  id: number;
  goalId: number;
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'OPEN';
  prompt: string;
  choices: Array<{ id: string; label: string }> | null;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD' | 'VERY_HARD';
  chapter: string | null;
}

export interface SessionResponse {
  sessionId: string;
  questions: Question[];
  strategy: string;
  reviewCount: number;
  discoveryCount: number;
  limits: { maxItems: number };
}

export interface ReviewState {
  masteryLevel: string;
  successCount: number;
  failCount: number;
  nextReviewAt: string;
}

export interface ReviewResponse {
  questionId: number;
  result: 'CORRECT' | 'INCORRECT';
  reviewState: ReviewState;
  usage: Usage;
}

export interface GenerateExamRequest {
  topic: string;
  description?: string;
  questionCount?: number;
  difficultyMix?: 'EASY_FOCUSED' | 'BALANCED' | 'HARD_FOCUSED' | 'PROGRESSIVE';
  questionTypes?: string[];
  language?: string;
}

export interface GenerateExamResponse {
  examId: number;
  title: string;
  description: string | null;
  questionsGenerated: number;
  questions: Question[];
  topicAnalysis: {
    mainTopic: string;
    subtopics: string[];
    suggestedChapters: string[];
    depth: string;
  };
  metadata: {
    model: string;
    tokensUsed: number;
    generationTimeMs: number;
  };
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  errorCode: string;
  correlationId?: string;
  meta?: Record<string, any>;
}

// ============================================================================
// API CLIENT
// ============================================================================

class ExamLoopApi {
  private deviceId: string;

  constructor(deviceId: string) {
    this.deviceId = deviceId;
  }

  private async request<T>(
    method: string,
    path: string,
    body?: any
  ): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'X-Device-Id': this.deviceId,
      },
      body: body ? JSON.stringify(body) : undefined,
    });

    if (!response.ok) {
      const error: ProblemDetail = await response.json();
      throw new ApiError(error);
    }

    return response.json();
  }

  // ============================================================================
  // BOOTSTRAP
  // ============================================================================

  async bootstrap(): Promise<BootstrapResponse> {
    return this.request('POST', '/api/v1/bootstrap', {});
  }

  // ============================================================================
  // GOALS
  // ============================================================================

  async getGoals(): Promise<{ goals: Goal[] }> {
    return this.request('GET', '/api/v1/goals');
  }

  async createGoal(title: string, description?: string): Promise<Goal> {
    return this.request('POST', '/api/v1/goals', { title, description });
  }

  // ============================================================================
  // SESSIONS
  // ============================================================================

  async generateSession(params?: {
    goalId?: number;
    limit?: number;
    difficulty?: string;
    strategy?: string;
  }): Promise<SessionResponse> {
    return this.request('POST', '/api/v1/sessions', params || {});
  }

  // ============================================================================
  // REVIEWS
  // ============================================================================

  async submitReview(
    questionId: number,
    correct: boolean,
    selectedChoiceIds?: string[]
  ): Promise<ReviewResponse> {
    return this.request('POST', '/api/v1/reviews', {
      questionId,
      correct,
      selectedChoiceIds,
    });
  }

  // ============================================================================
  // AI GENERATION (Premium)
  // ============================================================================

  async generateExam(request: GenerateExamRequest): Promise<GenerateExamResponse> {
    return this.request('POST', '/api/v1/exams/generate', request);
  }

  // ============================================================================
  // BILLING
  // ============================================================================

  async createCheckout(): Promise<{ sessionId: string; url: string }> {
    return this.request('POST', '/api/v1/billing/checkout', {});
  }

  async getBillingStatus(): Promise<{ premium: boolean; features: string[] }> {
    return this.request('GET', '/api/v1/billing/status');
  }
}

// ============================================================================
// ERROR CLASS
// ============================================================================

export class ApiError extends Error {
  public readonly errorCode: string;
  public readonly status: number;
  public readonly meta?: Record<string, any>;

  constructor(problem: ProblemDetail) {
    super(problem.detail);
    this.name = 'ApiError';
    this.errorCode = problem.errorCode;
    this.status = problem.status;
    this.meta = problem.meta;
  }

  isQuotaExceeded(): boolean {
    return this.errorCode === 'QUOTA_EXCEEDED';
  }

  isPremiumRequired(): boolean {
    return this.errorCode === 'PREMIUM_REQUIRED';
  }
}

// ============================================================================
// FACTORY
// ============================================================================

export function createApiClient(deviceId: string): ExamLoopApi {
  return new ExamLoopApi(deviceId);
}

export default ExamLoopApi;
