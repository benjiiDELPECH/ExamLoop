import { useState, useCallback } from 'react';
import { Goal, BootstrapResponse } from '../types';
import * as api from '../api/client';
import { getErrorMessage } from '../components/ErrorBanner';

interface UseGoalsReturn {
  myExams: Goal[];
  communityExams: Goal[];
  stats: BootstrapResponse | null;
  loading: boolean;
  refreshing: boolean;
  error: string | null;
  loadData: () => Promise<void>;
  refresh: () => void;
  createGoal: (title: string, description?: string) => Promise<boolean>;
  copyExam: (examId: number) => Promise<boolean>;
}

export function useGoals(): UseGoalsReturn {
  const [myExams, setMyExams] = useState<Goal[]>([]);
  const [communityExams, setCommunityExams] = useState<Goal[]>([]);
  const [stats, setStats] = useState<BootstrapResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async (isRefresh = false) => {
    try {
      if (!isRefresh) setLoading(true);
      setError(null);
      
      const [myExamsData, publicExamsData, bootstrapData] = await Promise.all([
        api.getGoals(),
        api.getPublicExams(),
        api.bootstrap()
      ]);
      
      setMyExams(myExamsData);
      setCommunityExams(publicExamsData);
      setStats(bootstrapData);
    } catch (err) {
      console.error('Error loading data:', err);
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  const refresh = useCallback(() => {
    setRefreshing(true);
    loadData(true);
  }, [loadData]);

  const createGoal = useCallback(async (title: string, description?: string): Promise<boolean> => {
    try {
      await api.createGoal(title, description);
      await loadData();
      return true;
    } catch (err) {
      console.error('Error creating goal:', err);
      setError('Failed to create goal. Please try again.');
      return false;
    }
  }, [loadData]);

  const copyExam = useCallback(async (examId: number): Promise<boolean> => {
    try {
      await api.copyPublicExam(examId);
      await loadData();
      return true;
    } catch (err) {
      console.error('Error copying exam:', err);
      // Silently fail for now - API endpoint may not exist yet
      return false;
    }
  }, [loadData]);

  return {
    myExams,
    communityExams,
    stats,
    loading,
    refreshing,
    error,
    loadData,
    refresh,
    createGoal,
    copyExam,
  };
}
