import * as React from 'react';
import { useState } from 'react';
import { View, StyleSheet, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { 
  Card, 
  Text, 
  Button, 
  useTheme,
  TextInput,
  SegmentedButtons,
  IconButton,
  Snackbar,
  Surface
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { createItem } from '../api/client';

type QuestionType = 'OPEN' | 'SINGLE_CHOICE';

export default function AddItemScreen({ navigation, route }: any) {
  const theme = useTheme();
  const { goalId, goalTitle } = route.params;
  
  const [questionType, setQuestionType] = useState<QuestionType>('OPEN');
  const [prompt, setPrompt] = useState('');
  const [answer, setAnswer] = useState('');
  const [explanation, setExplanation] = useState('');
  const [loading, setLoading] = useState(false);
  const [snackbarVisible, setSnackbarVisible] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');

  const handleSubmit = async () => {
    if (!prompt.trim() || !answer.trim()) {
      setSnackbarMessage('Please fill in both question and answer');
      setSnackbarVisible(true);
      return;
    }

    try {
      setLoading(true);
      await createItem(goalId, prompt, answer);
      
      setSnackbarMessage('Question added successfully! 🎉');
      setSnackbarVisible(true);
      
      // Clear form for next question
      setPrompt('');
      setAnswer('');
      setExplanation('');
    } catch (error) {
      console.error('Error creating item:', error);
      setSnackbarMessage('Failed to add question. Please try again.');
      setSnackbarVisible(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        {/* Header */}
        <Surface style={styles.header} elevation={1}>
          <View style={styles.headerRow}>
            <IconButton icon="arrow-left" onPress={() => navigation.goBack()} />
            <View style={styles.headerText}>
              <Text variant="titleLarge">Add Question</Text>
              <Text variant="bodySmall" style={styles.goalName}>{goalTitle}</Text>
            </View>
            <View style={{ width: 48 }} />
          </View>
        </Surface>

        <ScrollView style={styles.content} contentContainerStyle={styles.contentContainer}>
          {/* Question Type Selector */}
          <Text variant="labelLarge" style={styles.label}>Question Type</Text>
          <SegmentedButtons
            value={questionType}
            onValueChange={(value) => setQuestionType(value as QuestionType)}
            buttons={[
              { value: 'OPEN', label: 'Open Answer', icon: 'text' },
              { value: 'SINGLE_CHOICE', label: 'Multiple Choice', icon: 'format-list-bulleted' },
            ]}
            style={styles.segmentedButtons}
          />

          {/* Question Input */}
          <Card style={styles.inputCard} mode="elevated">
            <Card.Content>
              <TextInput
                label="Question"
                value={prompt}
                onChangeText={setPrompt}
                mode="outlined"
                multiline
                numberOfLines={4}
                placeholder="What do you want to learn?"
                style={styles.textInput}
              />

              <TextInput
                label="Answer"
                value={answer}
                onChangeText={setAnswer}
                mode="outlined"
                multiline
                numberOfLines={3}
                placeholder="The correct answer"
                style={styles.textInput}
              />

              <TextInput
                label="Explanation (optional)"
                value={explanation}
                onChangeText={setExplanation}
                mode="outlined"
                multiline
                numberOfLines={3}
                placeholder="Why is this the correct answer?"
                style={styles.textInput}
              />
            </Card.Content>
          </Card>

          {/* Tips */}
          <Card style={styles.tipsCard} mode="outlined">
            <Card.Content>
              <Text variant="titleSmall" style={styles.tipsTitle}>💡 Tips for great questions</Text>
              <Text variant="bodySmall" style={styles.tipText}>
                • Be specific and clear
              </Text>
              <Text variant="bodySmall" style={styles.tipText}>
                • Focus on one concept per question
              </Text>
              <Text variant="bodySmall" style={styles.tipText}>
                • Add explanations to help you learn why
              </Text>
            </Card.Content>
          </Card>
        </ScrollView>

        {/* Action Buttons */}
        <Surface style={styles.footer} elevation={2}>
          <Button 
            mode="outlined" 
            onPress={() => navigation.goBack()}
            style={styles.footerButton}
          >
            Done
          </Button>
          <Button 
            mode="contained" 
            onPress={handleSubmit}
            loading={loading}
            disabled={loading || !prompt.trim() || !answer.trim()}
            style={styles.footerButton}
            icon="plus"
          >
            Add Question
          </Button>
        </Surface>
      </KeyboardAvoidingView>

      <Snackbar
        visible={snackbarVisible}
        onDismiss={() => setSnackbarVisible(false)}
        duration={3000}
        action={{
          label: 'OK',
          onPress: () => setSnackbarVisible(false),
        }}
      >
        {snackbarMessage}
      </Snackbar>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  keyboardView: {
    flex: 1,
  },
  header: {
    padding: 8,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  headerText: {
    flex: 1,
    alignItems: 'center',
  },
  goalName: {
    opacity: 0.6,
  },
  content: {
    flex: 1,
  },
  contentContainer: {
    padding: 16,
  },
  label: {
    marginBottom: 8,
    marginTop: 8,
  },
  segmentedButtons: {
    marginBottom: 20,
  },
  inputCard: {
    marginBottom: 16,
  },
  textInput: {
    marginBottom: 16,
  },
  tipsCard: {
    backgroundColor: 'rgba(99, 102, 241, 0.05)',
  },
  tipsTitle: {
    marginBottom: 8,
    color: '#6366f1',
  },
  tipText: {
    opacity: 0.7,
    marginVertical: 2,
  },
  footer: {
    flexDirection: 'row',
    padding: 16,
    justifyContent: 'space-between',
  },
  footerButton: {
    flex: 1,
    marginHorizontal: 4,
  },
});
