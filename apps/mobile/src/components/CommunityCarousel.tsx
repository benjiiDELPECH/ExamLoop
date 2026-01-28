import * as React from 'react';
import { View, StyleSheet, ScrollView, Dimensions } from 'react-native';
import { Card, Text, Button, Chip, useTheme } from 'react-native-paper';
import { Goal } from '../types';

const { width } = Dimensions.get('window');
const CARD_WIDTH = width * 0.72;
const CARD_HEIGHT = 180;

interface CommunityCarouselProps {
  exams: Goal[];
  onExamPress: (exam: Goal) => void;
  onCopyExam: (exam: Goal) => void;
}

export function CommunityCarousel({ exams, onExamPress, onCopyExam }: CommunityCarouselProps) {
  const theme = useTheme();

  if (exams.length === 0) return null;

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text variant="titleMedium" style={styles.sectionTitle}>
          🔥 Community Exams
        </Text>
        <Chip compact icon="crown" style={styles.premiumChip} textStyle={styles.premiumText}>
          Premium
        </Chip>
      </View>
      
      {/* Carousel */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
        decelerationRate="fast"
        snapToInterval={CARD_WIDTH + 12}
      >
        {exams.map((exam) => (
          <View key={exam.id} style={[styles.cardWrapper, { backgroundColor: theme.colors.surfaceVariant }]}>
            {/* Top section */}
            <View style={styles.cardTop}>
              <Chip compact icon="earth" style={styles.publicChip}>
                Public
              </Chip>
              <Text style={styles.questionCount}>{exam.stats?.items || 0} Q</Text>
            </View>
            
            {/* Title */}
            <Text style={styles.examTitle} numberOfLines={1}>
              {exam.title}
            </Text>
            
            {/* Description */}
            {exam.description && (
              <Text style={styles.examDescription} numberOfLines={2}>
                {exam.description}
              </Text>
            )}
            
            {/* Button */}
            <Button 
              mode="contained" 
              onPress={() => onCopyExam(exam)}
              icon="plus"
              compact
              style={styles.addButton}
              labelStyle={styles.addButtonLabel}
            >
              Add to My Exams
            </Button>
          </View>
        ))}
        
        {/* Browse All Card */}
        <View style={[styles.cardWrapper, styles.browseAllCard]}>
          <Text style={styles.browseEmoji}>📚</Text>
          <Text style={styles.browseTitle}>Browse All</Text>
          <Text style={styles.browseSubtitle}>100+ exams</Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: 16,
    marginBottom: 8,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  sectionTitle: {
    fontWeight: '600',
  },
  premiumChip: {
    backgroundColor: '#fef3c7',
  },
  premiumText: {
    color: '#b45309',
    fontWeight: '600',
  },
  scrollContent: {
    paddingHorizontal: 12,
    paddingBottom: 4,
  },
  cardWrapper: {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,
    marginHorizontal: 6,
    borderRadius: 16,
    padding: 16,
    justifyContent: 'space-between',
  },
  cardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  publicChip: {
    backgroundColor: '#dbeafe',
  },
  questionCount: {
    fontSize: 13,
    fontWeight: '600',
    opacity: 0.7,
  },
  examTitle: {
    fontSize: 17,
    fontWeight: '700',
    marginTop: 8,
  },
  examDescription: {
    fontSize: 13,
    opacity: 0.7,
    lineHeight: 18,
    marginTop: 4,
  },
  addButton: {
    marginTop: 8,
    borderRadius: 10,
  },
  addButtonLabel: {
    fontSize: 13,
  },
  browseAllCard: {
    borderWidth: 2,
    borderColor: 'rgba(255,255,255,0.2)',
    borderStyle: 'dashed',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'transparent',
  },
  browseEmoji: {
    fontSize: 32,
    marginBottom: 8,
  },
  browseTitle: {
    fontSize: 16,
    fontWeight: '600',
  },
  browseSubtitle: {
    fontSize: 13,
    opacity: 0.6,
  },
});
