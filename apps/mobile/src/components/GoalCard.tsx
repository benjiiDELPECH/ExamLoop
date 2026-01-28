import * as React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text, Button, Chip, useTheme } from 'react-native-paper';
import { Goal } from '../types';

interface GoalCardProps {
  goal: Goal;
  onStudy: () => void;
  onAdd: () => void;
  onPress: () => void;
}

export function GoalCard({ goal, onStudy, onAdd, onPress }: GoalCardProps) {
  const theme = useTheme();
  
  return (
    <Card style={styles.card} mode="elevated" onPress={onPress}>
      <Card.Content>
        <View style={styles.header}>
          <Text variant="titleMedium" style={styles.title}>
            {goal.title}
          </Text>
          {goal.isPublic && (
            <Chip 
              compact 
              icon="earth" 
              style={[styles.publicChip, { backgroundColor: '#dbeafe' }]}
              textStyle={{ color: '#1d4ed8', fontWeight: '600' }}
            >
              Public
            </Chip>
          )}
        </View>
        
        {goal.description && (
          <Text variant="bodyMedium" style={styles.description}>
            {goal.description}
          </Text>
        )}
        
        <View style={styles.statsRow}>
          <View style={[styles.statBadge, { backgroundColor: '#f3f4f6' }]}>
            <Text style={styles.statText}>📚 {goal.stats?.items || 0} questions</Text>
          </View>
          <View style={[
            styles.statBadge, 
            { backgroundColor: (goal.stats?.due || 0) > 0 ? '#fef3c7' : '#f3f4f6' }
          ]}>
            <Text style={[
              styles.statText,
              (goal.stats?.due || 0) > 0 && { color: '#b45309' }
            ]}>
              ⏰ {goal.stats?.due || 0} due
            </Text>
          </View>
        </View>
      </Card.Content>
      
      <Card.Actions style={styles.actions}>
        <Button 
          mode="contained" 
          onPress={onStudy} 
          icon="play"
          style={styles.studyButton}
          compact
        >
          Study
        </Button>
        <Button 
          mode="outlined" 
          onPress={onAdd} 
          icon="plus"
          compact
        >
          Add
        </Button>
      </Card.Actions>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    marginHorizontal: 12,
    marginVertical: 8,
    borderRadius: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  title: {
    fontWeight: '700',
    flex: 1,
    fontSize: 18,
  },
  publicChip: {
    height: 28,
    borderRadius: 8,
  },
  description: {
    opacity: 0.7,
    marginBottom: 16,
    lineHeight: 20,
  },
  statsRow: {
    flexDirection: 'row',
    marginTop: 4,
  },
  statBadge: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    marginRight: 8,
  },
  statText: {
    fontSize: 13,
    fontWeight: '500',
    color: '#4b5563',
  },
  actions: {
    paddingHorizontal: 8,
    paddingBottom: 8,
  },
  studyButton: {
    marginRight: 8,
  },
});
