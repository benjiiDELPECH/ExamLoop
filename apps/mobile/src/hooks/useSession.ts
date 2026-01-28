import { useState, useCallback } from 'react';
import { Question, SessionStats } from '../types';
import * as api from '../api/client';

interface UseSessionReturn {
  questions: Question[];
  currentIndex: number;
  currentQuestion: Question | null;
  stats: SessionStats;
  loading: boolean;
  submitting: boolean;
  showAnswer: boolean;
  selectedChoice: string | null;
  isComplete: boolean;
  progress: number;
  error: string | null;
  quotaExceeded: boolean;
  
  loadSession: (goalId?: number) => Promise<void>;
  submitAnswer: (correct: boolean) => Promise<void>;
  revealAnswer: () => void;
  selectChoice: (choiceId: string) => void;
  reset: () => void;
}

export function useSession(): UseSessionReturn {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [stats, setStats] = useState<SessionStats>({ correct: 0, incorrect: 0, total: 0 });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [showAnswer, setShowAnswer] = useState(false);
  const [selectedChoice, setSelectedChoice] = useState<string | null>(null);
  const [isComplete, setIsComplete] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [quotaExceeded, setQuotaExceeded] = useState(false);

  const currentQuestion = questions[currentIndex] || null;
  const progress = questions.length > 0 ? (currentIndex + 1) / questions.length : 0;

  const loadSession = useCallback(async (goalId?: number) => {
    try {
      setLoading(true);
      setError(null);
      setQuotaExceeded(false);
      setIsComplete(false);
      setCurrentIndex(0);
      setStats({ correct: 0, incorrect: 0, total: 0 });
      
      const data = await api.getTodayItems(goalId);
      setQuestions(data);
      setStats(prev => ({ ...prev, total: data.length }));
    } catch (err: any) {
      console.error('Error loading session:', err);
      if (err.response?.status === 429) {
        setQuotaExceeded(true);
      } else {
        setError('Failed to load session. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const submitAnswer = useCallback(async (correct: boolean) => {
    if (!currentQuestion || submitting) return;

    try {
      setSubmitting(true);
      await api.reviewItem(currentQuestion.id, correct);
      
      setStats(prev => ({
        ...prev,
        correct: prev.correct + (correct ? 1 : 0),
        incorrect: prev.incorrect + (correct ? 0 : 1),
      }));

      if (currentIndex < questions.length - 1) {
        setCurrentIndex(prev => prev + 1);
        setShowAnswer(false);
        setSelectedChoice(null);
      } else {
        setIsComplete(true);
      }
    } catch (err: any) {
      console.error('Error submitting review:', err);
      if (err.response?.status === 429) {
        setQuotaExceeded(true);
      } else {
        setError('Failed to submit answer. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }, [currentQuestion, currentIndex, questions.length, submitting]);

  const revealAnswer = useCallback(() => {
    setShowAnswer(true);
  }, []);

  const selectChoice = useCallback((choiceId: string) => {
    setSelectedChoice(choiceId);
  }, []);

  const reset = useCallback(() => {
    setQuestions([]);
    setCurrentIndex(0);
    setStats({ correct: 0, incorrect: 0, total: 0 });
    setShowAnswer(false);
    setSelectedChoice(null);
    setIsComplete(false);
    setError(null);
    setQuotaExceeded(false);
  }, []);

  return {
    questions,
    currentIndex,
    currentQuestion,
    stats,
    loading,
    submitting,
    showAnswer,
    selectedChoice,
    isComplete,
    progress,
    error,
    quotaExceeded,
    loadSession,
    submitAnswer,
    revealAnswer,
    selectChoice,
    reset,
  };
}
