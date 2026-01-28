import * as React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text, Surface, Button, Divider, useTheme } from 'react-native-paper';
import { SessionStats } from '../types';

interface SessionResultsProps {
  stats: SessionStats;
  onGoBack: () => void;
  onPlayAgain: () => void;
}

export function SessionResults({ stats, onGoBack, onPlayAgain }: SessionResultsProps) {
  const theme = useTheme();
  const percentage = stats.total > 0 ? Math.round((stats.correct / stats.total) * 100) : 0;
  
  const emoji = percentage >= 80 ? '🏆' : percentage >= 60 ? '👍' : '💪';

  return (
    <View style={styles.container}>
      <Text style={styles.emoji}>{emoji}</Text>
      <Text variant="headlineMedium" style={styles.title}>
        Session Complete!
      </Text>
      
      <Surface style={styles.resultsCard} elevation={2}>
        <View style={styles.resultRow}>
          <Text variant="titleMedium">Correct</Text>
          <Text variant="headlineSmall" style={{ color: '#22c55e' }}>
            {stats.correct}
          </Text>
        </View>
        <Divider style={styles.divider} />
        <View style={styles.resultRow}>
          <Text variant="titleMedium">Incorrect</Text>
          <Text variant="headlineSmall" style={{ color: '#ef4444' }}>
            {stats.incorrect}
          </Text>
        </View>
        <Divider style={styles.divider} />
        <View style={styles.resultRow}>
          <Text variant="titleMedium">Score</Text>
          <Text variant="headlineSmall" style={{ color: theme.colors.primary }}>
            {percentage}%
          </Text>
        </View>
      </Surface>

      <Button 
        mode="contained" 
        onPress={onGoBack}
        style={styles.button}
        icon="home"
      >
        Back to Dashboard
      </Button>
      <Button 
        mode="outlined" 
        onPress={onPlayAgain}
        style={styles.button}
        icon="refresh"
      >
        Start Another Session
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  emoji: {
    fontSize: 64,
    marginBottom: 16,
  },
  title: {
    marginBottom: 24,
    fontWeight: 'bold',
  },
  resultsCard: {
    width: '100%',
    padding: 20,
    borderRadius: 12,
    marginBottom: 24,
  },
  resultRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
  },
  divider: {
    marginVertical: 4,
  },
  button: {
    marginTop: 12,
    width: '100%',
  },
});
