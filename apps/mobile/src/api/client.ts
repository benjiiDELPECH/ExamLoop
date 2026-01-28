import axios from 'axios';
import { getDeviceId } from '../utils/deviceId';

// API URL - change for production
const API_BASE_URL = 'https://ultimately-www-extract-peer.trycloudflare.com';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add device ID to all requests
api.interceptors.request.use(async (config) => {
  const deviceId = await getDeviceId();
  config.headers['X-Device-Id'] = deviceId;
  return config;
});

export interface Goal {
  id: number;
  title: string;
  description?: string;
  isPublic: boolean;
  stats?: { items: number; due: number };
}

export interface Item {
  id: number;
  goalId: number;
  prompt: string;
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'OPEN';
  choices?: Array<{ id: string; label: string }>;
  difficulty: string;
  chapter?: string;
}

export interface BootstrapResponse {
  profile: { premium: boolean };
  usage: { reviewsUsed: number; reviewsLimit: number; remaining: number };
  dashboard: { dueCount: number; totalQuestions: number; goalsCount: number };
}

// Bootstrap API
export const bootstrap = async (): Promise<BootstrapResponse> => {
  const response = await api.post('/api/v1/bootstrap', {});
  return response.data;
};

// Goals API
export const getGoals = async (): Promise<Goal[]> => {
  const response = await api.get('/api/v1/goals');
  return response.data.goals || [];
};

export const createGoal = async (title: string, description?: string): Promise<Goal> => {
  const response = await api.post('/api/v1/goals', { title, description });
  return response.data;
};

// Items (for compatibility)
export const getItems = async (): Promise<Item[]> => {
  // Get all questions from all goals
  return [];
};

export const createItem = async (goalId: number, question: string, answer: string): Promise<any> => {
  const response = await api.post('/api/v1/questions', { 
    goalId, 
    prompt: question, 
    answer,
    type: 'OPEN',
    difficulty: 'MEDIUM'
  });
  return response.data;
};

// Session API
export const getTodayItems = async (goalId?: number): Promise<Item[]> => {
  const body: any = { limit: 10 };
  if (goalId) body.goalId = goalId;
  const response = await api.post('/api/v1/sessions', body);
  return response.data.questions || [];
};

export const reviewItem = async (id: number, correct: boolean): Promise<any> => {
  const response = await api.post('/api/v1/reviews', { questionId: id, correct });
  return response.data;
};

// Billing API
export const createCheckout = async (): Promise<{ url: string }> => {
  const response = await api.post('/api/v1/billing/checkout', {});
  return response.data;
};

export default api;
