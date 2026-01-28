import * as React from 'react';
import { useEffect } from 'react';
import { View, StyleSheet, ScrollView } from 'react-native';
import { Text, Button, useTheme, ActivityIndicator } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';

import { useSession } from '../hooks';
import { 
  QuestionCard, 
  SessionHeader, 
  SessionResults, 
  EmptyState 
} from '../components';

export default function SessionScreen({ navigation, route }: any) {
  const theme = useTheme();
  const goalId = route?.params?.goalId;
  
  const {
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
    quotaExceeded,
    loadSession,
    submitAnswer,
    revealAnswer,
    selectChoice,
  } = useSession();

  useEffect(() => {
    loadSession(goalId);
  }, [goalId, loadSession]);

  // Redirect to paywall if quota exceeded
  useEffect(() => {
    if (quotaExceeded) {
      navigation.navigate('Paywall');
    }
  }, [quotaExceeded, navigation]);

  // Loading state
  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <View style={styles.centerContainer}>
          <ActivityIndicator size="large" />
          <Text style={styles.loadingText}>Preparing your session...</Text>
        </View>
      </SafeAreaView>
    );
  }

  // Empty state
  if (questions.length === 0) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <View style={styles.centerContainer}>
          <EmptyState
            emoji="🎉"
            title="All caught up!"
            description="No questions due for review right now. Come back later or add more questions to your exams."
            actionLabel="Back to Dashboard"
            onAction={() => navigation.goBack()}
          />
        </View>
      </SafeAreaView>
    );
  }

  // Session complete
  if (isComplete) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <SessionResults
          stats={stats}
          onGoBack={() => navigation.goBack()}
          onPlayAgain={() => loadSession(goalId)}
        />
      </SafeAreaView>
    );
  }

  // Active session
  const isOpenQuestion = currentQuestion?.type === 'OPEN';
  const canSubmit = isOpenQuestion ? showAnswer : selectedChoice !== null;

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <SessionHeader
        currentIndex={currentIndex}
        total={questions.length}
        correctCount={stats.correct}
        progress={progress}
        onClose={() => navigation.goBack()}
      />

      <ScrollView style={styles.content} contentContainerStyle={styles.contentContainer}>
        {currentQuestion && (
          <QuestionCard
            question={currentQuestion}
            showAnswer={showAnswer}
            selectedChoice={selectedChoice}
            onRevealAnswer={revealAnswer}
            onSelectChoice={selectChoice}
          />
        )}

        {/* Answer Buttons */}
        {canSubmit && (
          <View style={styles.answerButtons}>
            {isOpenQuestion ? (
              <>
                <Button 
                  mode="contained" 
                  onPress={() => submitAnswer(false)}
                  style={styles.answerButton}
                  icon="close"
                  loading={submitting}
                  disabled={submitting}
                  buttonColor="#ef4444"
                >
                  Got it Wrong
                </Button>
                <Button 
                  mode="contained" 
                  onPress={() => submitAnswer(true)}
                  style={styles.answerButton}
                  icon="check"
                  loading={submitting}
                  disabled={submitting}
                  buttonColor="#22c55e"
                >
                  Got it Right
                </Button>
              </>
            ) : (
              <Button 
                mode="contained" 
                onPress={() => submitAnswer(true)}
                style={styles.submitButton}
                loading={submitting}
                disabled={submitting}
              >
                Submit Answer
              </Button>
            )}
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  loadingText: {
    marginTop: 16,
    opacity: 0.7,
  },
  content: {
    flex: 1,
  },
  contentContainer: {
    padding: 16,
    paddingBottom: 32,
  },
  answerButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  answerButton: {
    flex: 1,
    marginHorizontal: 4,
  },
  submitButton: {
    flex: 1,
  },
});
