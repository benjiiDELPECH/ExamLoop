import axios from 'axios';
import { getDeviceId } from '../utils/deviceId';
import { Goal, Question, BootstrapResponse } from '../types';

// API URL - change for production
const API_BASE_URL = 'https://ultimately-www-extract-peer.trycloudflare.com';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Add device ID to all requests
api.interceptors.request.use(async (config) => {
  const deviceId = await getDeviceId();
  config.headers['X-Device-Id'] = deviceId;
  return config;
});

// ============================================
// Bootstrap
// ============================================

export async function bootstrap(): Promise<BootstrapResponse> {
  const response = await api.post('/api/v1/bootstrap', {});
  return response.data;
}

// ============================================
// Goals (Exams)
// ============================================

export async function getGoals(): Promise<Goal[]> {
  const response = await api.get('/api/v1/goals');
  const goals = response.data.goals || [];
  // Filter: only user's own exams (not public community ones)
  return goals.filter((g: Goal) => !g.isPublic);
}

export async function getPublicExams(): Promise<Goal[]> {
  const response = await api.get('/api/v1/goals');
  const goals = response.data.goals || [];
  // Filter: only public community exams
  return goals.filter((g: Goal) => g.isPublic);
}

export async function createGoal(title: string, description?: string): Promise<Goal> {
  const response = await api.post('/api/v1/goals', { title, description });
  return response.data;
}

export async function copyPublicExam(examId: number): Promise<Goal> {
  // Copy a public exam to user's personal exams
  const response = await api.post(`/api/v1/goals/${examId}/copy`, {});
  return response.data;
}

// ============================================
// Questions (Items)
// ============================================

export async function getItems(): Promise<Question[]> {
  // Placeholder - returns empty for now
  return [];
}

export async function createItem(
  goalId: number, 
  question: string, 
  answer: string
): Promise<{ id: number }> {
  const response = await api.post('/api/v1/questions', { 
    goalId, 
    prompt: question, 
    answer,
    type: 'OPEN',
    difficulty: 'MEDIUM'
  });
  return response.data;
}

// ============================================
// Session
// ============================================

export async function getTodayItems(goalId?: number): Promise<Question[]> {
  const body: { limit: number; goalId?: number } = { limit: 10 };
  if (goalId) body.goalId = goalId;
  
  const response = await api.post('/api/v1/sessions', body);
  return response.data.questions || [];
}

// ============================================
// Reviews
// ============================================

export async function reviewItem(
  questionId: number, 
  correct: boolean
): Promise<any> {
  const response = await api.post('/api/v1/reviews', { questionId, correct });
  return response.data;
}

// ============================================
// Billing
// ============================================

export async function createCheckout(): Promise<{ url: string }> {
  const response = await api.post('/api/v1/billing/checkout', {});
  return response.data;
}

export default api;
