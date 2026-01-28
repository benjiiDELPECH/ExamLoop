import axios from 'axios';
import { getDeviceId } from '../utils/deviceId';

// Change this to your API URL when running in production
const API_BASE_URL = 'http://localhost:8080';

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
  deviceId: string;
  title: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Item {
  id: number;
  goalId: number;
  deviceId: string;
  question: string;
  answer: string;
  box: number;
  nextReview: string;
  createdAt: string;
  updatedAt: string;
}

// Auth API
export const login = async () => {
  const deviceId = await getDeviceId();
  const response = await api.post('/anon/login', { deviceId });
  return response.data;
};

// Goals API
export const getGoals = async (): Promise<Goal[]> => {
  const response = await api.get('/goals');
  return response.data;
};

export const createGoal = async (title: string, description?: string): Promise<Goal> => {
  const response = await api.post('/goals', { title, description });
  return response.data;
};

export const updateGoal = async (id: number, title: string, description?: string): Promise<Goal> => {
  const response = await api.put(`/goals/${id}`, { title, description });
  return response.data;
};

export const deleteGoal = async (id: number): Promise<void> => {
  await api.delete(`/goals/${id}`);
};

// Items API
export const getItems = async (): Promise<Item[]> => {
  const response = await api.get('/items');
  return response.data;
};

export const createItem = async (goalId: number, question: string, answer: string): Promise<Item> => {
  const response = await api.post('/items', { goalId, question, answer });
  return response.data;
};

export const updateItem = async (id: number, question: string, answer: string): Promise<Item> => {
  const response = await api.put(`/items/${id}`, { question, answer });
  return response.data;
};

export const deleteItem = async (id: number): Promise<void> => {
  await api.delete(`/items/${id}`);
};

// Session API
export const getTodayItems = async (): Promise<Item[]> => {
  const response = await api.get('/session/today');
  return response.data;
};

export const reviewItem = async (id: number, correct: boolean): Promise<Item> => {
  const response = await api.post(`/review/${id}`, { correct });
  return response.data;
};

// Billing API
export const createCheckout = async (): Promise<{ checkoutUrl: string }> => {
  const response = await api.post('/billing/checkout');
  return response.data;
};

export default api;
