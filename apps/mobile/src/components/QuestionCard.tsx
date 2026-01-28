import * as React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text, Button, Chip, Surface, RadioButton, useTheme } from 'react-native-paper';
import { Question } from '../types';

interface QuestionCardProps {
  question: Question;
  showAnswer: boolean;
  selectedChoice: string | null;
  onRevealAnswer: () => void;
  onSelectChoice: (choiceId: string) => void;
}

export function QuestionCard({ 
  question, 
  showAnswer, 
  selectedChoice, 
  onRevealAnswer, 
  onSelectChoice 
}: QuestionCardProps) {
  const theme = useTheme();
  const isOpenQuestion = question.type === 'OPEN';

  return (
    <Card style={styles.card} mode="elevated">
      <Card.Content>
        {/* Metadata chips */}
        <View style={styles.chipRow}>
          {question.chapter && (
            <Chip compact style={styles.chip} icon="folder">
              {question.chapter}
            </Chip>
          )}
          <Chip compact style={styles.chip} icon="signal">
            {question.difficulty}
          </Chip>
        </View>
        
        {/* Question text */}
        <Text variant="headlineSmall" style={styles.questionText}>
          {question.prompt}
        </Text>

        {/* MCQ Choices */}
        {!isOpenQuestion && question.choices && (
          <View style={styles.choicesContainer}>
            <RadioButton.Group 
              onValueChange={onSelectChoice} 
              value={selectedChoice || ''}
            >
              {question.choices.map((choice) => (
                <Surface 
                  key={choice.id} 
                  style={[
                    styles.choiceItem,
                    selectedChoice === choice.id && { 
                      borderWidth: 2,
                      borderColor: theme.colors.primary 
                    }
                  ]}
                  elevation={1}
                >
                  <RadioButton.Item 
                    label={choice.label} 
                    value={choice.id}
                    labelStyle={styles.choiceLabel}
                  />
                </Surface>
              ))}
            </RadioButton.Group>
          </View>
        )}

        {/* Open Question - Reveal Answer */}
        {isOpenQuestion && !showAnswer && (
          <Button 
            mode="outlined" 
            onPress={onRevealAnswer}
            style={styles.revealButton}
            icon="eye"
          >
            Reveal Answer
          </Button>
        )}

        {/* Answer Section */}
        {showAnswer && (
          <Surface style={[styles.answerContainer, { backgroundColor: theme.colors.primaryContainer }]} elevation={0}>
            <Text variant="labelLarge" style={[styles.answerLabel, { color: theme.colors.primary }]}>
              Answer:
            </Text>
            <Text variant="bodyLarge" style={styles.answerText}>
              {question.prompt}
            </Text>
          </Surface>
        )}
      </Card.Content>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    marginBottom: 16,
  },
  chipRow: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  chip: {
    marginRight: 8,
  },
  questionText: {
    lineHeight: 32,
  },
  choicesContainer: {
    marginTop: 24,
  },
  choiceItem: {
    marginVertical: 6,
    borderRadius: 8,
  },
  choiceLabel: {
    fontSize: 16,
  },
  revealButton: {
    marginTop: 24,
  },
  answerContainer: {
    marginTop: 24,
    padding: 16,
    borderRadius: 8,
  },
  answerLabel: {
    marginBottom: 8,
  },
  answerText: {
    lineHeight: 24,
  },
});
