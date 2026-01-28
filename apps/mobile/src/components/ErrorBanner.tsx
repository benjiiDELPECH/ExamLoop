import * as React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text, Button, Surface, useTheme } from 'react-native-paper';

interface ErrorBannerProps {
  message: string;
  onRetry?: () => void;
  type?: 'error' | 'warning' | 'offline';
}

export function ErrorBanner({ message, onRetry, type = 'error' }: ErrorBannerProps) {
  const theme = useTheme();
  
  const config = {
    error: { emoji: '❌', bg: '#fef2f2', border: '#fecaca', text: '#991b1b' },
    warning: { emoji: '⚠️', bg: '#fffbeb', border: '#fde68a', text: '#92400e' },
    offline: { emoji: '📡', bg: '#f3f4f6', border: '#d1d5db', text: '#374151' },
  };
  
  const { emoji, bg, border, text } = config[type];

  return (
    <View style={[styles.container, { backgroundColor: bg, borderColor: border }]}>
      <View style={styles.content}>
        <Text style={styles.emoji}>{emoji}</Text>
        <Text style={[styles.message, { color: text }]}>{message}</Text>
      </View>
      {onRetry && (
        <Button mode="text" onPress={onRetry} compact textColor={text}>
          Retry
        </Button>
      )}
    </View>
  );
}

// Parse Problem Details error response
export interface ProblemDetails {
  type?: string;
  title: string;
  status: number;
  detail?: string;
  errorCode?: string;
  correlationId?: string;
}

export function parseProblemDetails(error: any): ProblemDetails | null {
  if (error?.response?.data) {
    const data = error.response.data;
    if (data.title && data.status) {
      return data as ProblemDetails;
    }
  }
  return null;
}

export function getErrorMessage(error: any): string {
  const problem = parseProblemDetails(error);
  
  if (problem) {
    // Handle known error codes
    switch (problem.errorCode) {
      case 'QUOTA_EXCEEDED':
        return 'Daily review limit reached. Upgrade to Premium for unlimited reviews!';
      case 'NOT_FOUND':
        return problem.detail || 'Resource not found.';
      case 'VALIDATION_FAILED':
        return problem.detail || 'Please check your input.';
      case 'FORBIDDEN':
        return 'You don\'t have permission to do this.';
      default:
        return problem.detail || problem.title || 'Something went wrong.';
    }
  }
  
  // Network errors
  if (error?.message?.includes('Network Error')) {
    return 'Unable to connect. Please check your internet connection.';
  }
  
  if (error?.code === 'ECONNABORTED') {
    return 'Request timed out. Please try again.';
  }
  
  return 'Something went wrong. Please try again.';
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 12,
    marginHorizontal: 16,
    marginVertical: 8,
    borderRadius: 12,
    borderWidth: 1,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  emoji: {
    fontSize: 18,
    marginRight: 10,
  },
  message: {
    fontSize: 14,
    flex: 1,
  },
});
