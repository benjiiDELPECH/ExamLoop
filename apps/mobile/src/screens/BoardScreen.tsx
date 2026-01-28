import * as React from 'react';
import { useCallback, useState } from 'react';
import { View, StyleSheet, FlatList, RefreshControl } from 'react-native';
import { 
  Text, 
  Button, 
  FAB, 
  Portal, 
  Modal, 
  TextInput,
  useTheme,
  ActivityIndicator,
  Surface,
  IconButton,
  Divider
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';

import { useGoals } from '../hooks';
import { GoalCard, EmptyStateCard, CommunityCarousel, ErrorBanner } from '../components';
import { Goal } from '../types';

export default function BoardScreen({ navigation }: any) {
  const theme = useTheme();
  const { myExams, communityExams, stats, loading, refreshing, refresh, loadData, createGoal, copyExam, error } = useGoals();
  
  const [modalVisible, setModalVisible] = useState(false);
  const [newGoalTitle, setNewGoalTitle] = useState('');
  const [newGoalDescription, setNewGoalDescription] = useState('');
  const [creating, setCreating] = useState(false);

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [loadData])
  );

  const handleCreateGoal = async () => {
    if (!newGoalTitle.trim()) return;
    
    setCreating(true);
    const success = await createGoal(newGoalTitle, newGoalDescription);
    setCreating(false);
    
    if (success) {
      setNewGoalTitle('');
      setNewGoalDescription('');
      setModalVisible(false);
    }
  };

  const handleCloseModal = () => {
    setModalVisible(false);
    setNewGoalTitle('');
    setNewGoalDescription('');
  };

  const renderGoalCard = ({ item }: { item: Goal }) => (
    <GoalCard
      goal={item}
      onStudy={() => navigation.navigate('Session', { goalId: item.id })}
      onAdd={() => navigation.navigate('AddItem', { goalId: item.id, goalTitle: item.title })}
      onPress={() => navigation.navigate('AddItem', { goalId: item.id, goalTitle: item.title })}
    />
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: theme.colors.primary }]}>
        <View style={styles.headerTop}>
          <View>
            <Text variant="headlineMedium" style={styles.headerTitle}>ExamLoop</Text>
            <Text variant="bodyMedium" style={styles.headerSubtitle}>Master your knowledge</Text>
          </View>
          <IconButton 
            icon="cog" 
            iconColor="#ffffff"
            onPress={() => navigation.navigate('Settings')}
          />
        </View>
        
        {stats && (
          <View style={styles.statsContainer}>
            <View style={styles.statBox}>
              <Text variant="headlineLarge" style={styles.statValue}>{stats.dashboard.dueCount}</Text>
              <Text variant="bodySmall" style={styles.statLabel}>Due Today</Text>
            </View>
            <View style={styles.statBox}>
              <Text variant="headlineLarge" style={styles.statValue}>{stats.dashboard.totalQuestions}</Text>
              <Text variant="bodySmall" style={styles.statLabel}>Total</Text>
            </View>
            <View style={styles.statBox}>
              <Text variant="headlineLarge" style={styles.statValue}>{stats.usage.remaining}</Text>
              <Text variant="bodySmall" style={styles.statLabel}>Reviews Left</Text>
            </View>
          </View>
        )}

        <View style={styles.actionButtons}>
          <Button 
            mode="contained" 
            icon="play-circle"
            onPress={() => navigation.navigate('Session')}
            style={styles.actionButton}
            buttonColor="#ffffff"
            textColor={theme.colors.primary}
          >
            Start Session
          </Button>
          <Button 
            mode="outlined" 
            icon="crown"
            onPress={() => navigation.navigate('Paywall')}
            style={[styles.actionButton, styles.premiumButton]}
            textColor="#ffffff"
          >
            {stats?.profile.premium ? 'Premium ✓' : 'Go Premium'}
          </Button>
        </View>
      </View>

      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" />
          <Text style={styles.loadingText}>Loading your exams...</Text>
        </View>
      ) : (
        <FlatList
          data={myExams}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderGoalCard}
          contentContainerStyle={styles.listContent}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={refresh} />
          }
          ListHeaderComponent={
            <>
              {/* Error Banner */}
              {error && (
                <ErrorBanner 
                  message={error} 
                  onRetry={refresh}
                  type={error.includes('internet') ? 'offline' : 'error'}
                />
              )}
              
              {/* Community Carousel */}
              <CommunityCarousel
                exams={communityExams}
                onExamPress={(exam) => navigation.navigate('Session', { goalId: exam.id })}
                onCopyExam={(exam) => copyExam(exam.id)}
              />
              
              {/* My Exams Header */}
              <View style={styles.myExamsHeader}>
                <Text variant="titleMedium" style={styles.sectionTitle}>
                  📖 My Exams
                </Text>
                <Button 
                  mode="text" 
                  icon="plus" 
                  compact 
                  onPress={() => setModalVisible(true)}
                >
                  Create
                </Button>
              </View>
            </>
          }
          ListEmptyComponent={
            <EmptyStateCard
              emoji="✨"
              title="Start your journey"
              description="Create your first exam or explore community exams above!"
              actionLabel="Create Exam"
              onAction={() => setModalVisible(true)}
            />
          }
        />
      )}

      {/* FAB */}
      <FAB
        icon="plus"
        style={[styles.fab, { backgroundColor: theme.colors.primary }]}
        onPress={() => setModalVisible(true)}
      />

      {/* Create Goal Modal */}
      <Portal>
        <Modal
          visible={modalVisible}
          onDismiss={handleCloseModal}
          contentContainerStyle={[styles.modalContent, { backgroundColor: theme.colors.surface }]}
        >
          <Text variant="headlineSmall" style={styles.modalTitle}>Create New Exam</Text>
          
          <TextInput
            label="Exam Title"
            value={newGoalTitle}
            onChangeText={setNewGoalTitle}
            mode="outlined"
            style={styles.input}
            placeholder="e.g. Spring Security, AWS Solutions Architect"
          />
          
          <TextInput
            label="Description (optional)"
            value={newGoalDescription}
            onChangeText={setNewGoalDescription}
            mode="outlined"
            multiline
            numberOfLines={3}
            style={styles.input}
            placeholder="What do you want to master?"
          />

          <View style={styles.modalActions}>
            <Button mode="text" onPress={handleCloseModal}>
              Cancel
            </Button>
            <Button 
              mode="contained" 
              onPress={handleCreateGoal}
              loading={creating}
              disabled={!newGoalTitle.trim() || creating}
            >
              Create
            </Button>
          </View>
        </Modal>
      </Portal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    padding: 20,
    paddingTop: 16,
    paddingBottom: 24,
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  headerTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  headerTitle: {
    fontWeight: 'bold',
    color: '#ffffff',
  },
  headerSubtitle: {
    opacity: 0.85,
    marginBottom: 20,
    color: '#ffffff',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 20,
    backgroundColor: 'rgba(255,255,255,0.15)',
    borderRadius: 16,
    paddingVertical: 16,
  },
  statBox: {
    alignItems: 'center',
  },
  statValue: {
    fontWeight: 'bold',
    color: '#ffffff',
  },
  statLabel: {
    color: 'rgba(255,255,255,0.8)',
    marginTop: 4,
  },
  actionButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  actionButton: {
    flex: 1,
    marginHorizontal: 4,
    borderRadius: 12,
  },
  premiumButton: {
    borderColor: 'rgba(255,255,255,0.5)',
    borderWidth: 2,
  },
  sectionTitle: {
    fontWeight: '600',
  },
  myExamsHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 8,
  },
  listContent: {
    padding: 8,
    paddingBottom: 100,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 12,
    opacity: 0.7,
  },
  fab: {
    position: 'absolute',
    right: 16,
    bottom: 16,
  },
  modalContent: {
    margin: 20,
    padding: 20,
    borderRadius: 12,
  },
  modalTitle: {
    marginBottom: 20,
    fontWeight: '600',
  },
  input: {
    marginBottom: 16,
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 8,
  },
});
