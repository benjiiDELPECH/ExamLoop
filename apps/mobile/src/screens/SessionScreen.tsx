import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { getTodayItems, reviewItem, Item } from '../api/client';

export default function SessionScreen({ navigation }: any) {
  const [items, setItems] = useState<Item[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTodayItems();
  }, []);

  const loadTodayItems = async () => {
    try {
      setLoading(true);
      const todayItems = await getTodayItems();
      setItems(todayItems);
    } catch (error) {
      console.error('Error loading today items:', error);
      Alert.alert('Error', 'Failed to load items for today');
    } finally {
      setLoading(false);
    }
  };

  const handleReview = async (correct: boolean) => {
    const currentItem = items[currentIndex];
    
    try {
      await reviewItem(currentItem.id, correct);
      
      // Move to next item
      if (currentIndex < items.length - 1) {
        setCurrentIndex(currentIndex + 1);
        setShowAnswer(false);
      } else {
        // Session complete
        Alert.alert(
          'Session Complete!',
          `You've reviewed all ${items.length} items for today.`,
          [{ text: 'OK', onPress: () => navigation.goBack() }]
        );
      }
    } catch (error) {
      console.error('Error reviewing item:', error);
      Alert.alert('Error', 'Failed to save review');
    }
  };

  if (loading) {
    return (
      <View style={styles.container}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Text style={styles.backButton}>← Back</Text>
          </TouchableOpacity>
          <Text style={styles.title}>Study Session</Text>
        </View>
        <Text style={styles.loadingText}>Loading...</Text>
      </View>
    );
  }

  if (items.length === 0) {
    return (
      <View style={styles.container}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Text style={styles.backButton}>← Back</Text>
          </TouchableOpacity>
          <Text style={styles.title}>Study Session</Text>
        </View>
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyText}>No items to review today!</Text>
          <Text style={styles.emptySubtext}>
            Add some flashcards or come back tomorrow.
          </Text>
        </View>
      </View>
    );
  }

  const currentItem = items[currentIndex];

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backButton}>← Back</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Study Session</Text>
        <Text style={styles.progress}>
          {currentIndex + 1} / {items.length}
        </Text>
      </View>

      <View style={styles.cardContainer}>
        <View style={styles.card}>
          <Text style={styles.boxLabel}>Box {currentItem.box}</Text>
          <Text style={styles.questionLabel}>Question:</Text>
          <Text style={styles.questionText}>{currentItem.question}</Text>

          {showAnswer && (
            <>
              <View style={styles.divider} />
              <Text style={styles.answerLabel}>Answer:</Text>
              <Text style={styles.answerText}>{currentItem.answer}</Text>
            </>
          )}
        </View>

        {!showAnswer ? (
          <TouchableOpacity
            style={styles.showAnswerButton}
            onPress={() => setShowAnswer(true)}
          >
            <Text style={styles.showAnswerButtonText}>Show Answer</Text>
          </TouchableOpacity>
        ) : (
          <View style={styles.reviewButtons}>
            <TouchableOpacity
              style={[styles.reviewButton, styles.wrongButton]}
              onPress={() => handleReview(false)}
            >
              <Text style={styles.reviewButtonText}>Wrong</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.reviewButton, styles.correctButton]}
              onPress={() => handleReview(true)}
            >
              <Text style={styles.reviewButtonText}>Correct</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#6200ee',
    padding: 20,
    paddingTop: 50,
  },
  backButton: {
    color: 'white',
    fontSize: 16,
    marginBottom: 10,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
  },
  progress: {
    color: 'white',
    fontSize: 16,
    marginTop: 5,
  },
  loadingText: {
    textAlign: 'center',
    marginTop: 20,
    fontSize: 16,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  emptyText: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  emptySubtext: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
  },
  cardContainer: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 20,
    minHeight: 300,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 6,
    elevation: 5,
  },
  boxLabel: {
    fontSize: 12,
    color: '#6200ee',
    fontWeight: 'bold',
    marginBottom: 10,
  },
  questionLabel: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  questionText: {
    fontSize: 18,
    color: '#333',
    marginBottom: 15,
  },
  divider: {
    height: 1,
    backgroundColor: '#ddd',
    marginVertical: 15,
  },
  answerLabel: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#6200ee',
    marginBottom: 5,
  },
  answerText: {
    fontSize: 18,
    color: '#333',
  },
  showAnswerButton: {
    backgroundColor: '#6200ee',
    padding: 16,
    borderRadius: 8,
    marginTop: 20,
  },
  showAnswerButtonText: {
    color: 'white',
    textAlign: 'center',
    fontSize: 18,
    fontWeight: 'bold',
  },
  reviewButtons: {
    flexDirection: 'row',
    gap: 15,
    marginTop: 20,
  },
  reviewButton: {
    flex: 1,
    padding: 16,
    borderRadius: 8,
  },
  wrongButton: {
    backgroundColor: '#dc3545',
  },
  correctButton: {
    backgroundColor: '#28a745',
  },
  reviewButtonText: {
    color: 'white',
    textAlign: 'center',
    fontSize: 18,
    fontWeight: 'bold',
  },
});
