import * as React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text, Surface, IconButton, Chip, ProgressBar, useTheme } from 'react-native-paper';

interface SessionHeaderProps {
  currentIndex: number;
  total: number;
  correctCount: number;
  progress: number;
  onClose: () => void;
}

export function SessionHeader({ 
  currentIndex, 
  total, 
  correctCount, 
  progress, 
  onClose 
}: SessionHeaderProps) {
  const theme = useTheme();

  return (
    <Surface style={styles.header} elevation={1}>
      <View style={styles.headerRow}>
        <IconButton icon="close" onPress={onClose} />
        <View style={styles.progressInfo}>
          <Text variant="titleMedium">
            Question {currentIndex + 1} of {total}
          </Text>
          <ProgressBar 
            progress={progress} 
            style={styles.progressBar}
            color={theme.colors.primary}
          />
        </View>
        <Chip compact icon="check-circle">
          {correctCount}/{currentIndex}
        </Chip>
      </View>
    </Surface>
  );
}

const styles = StyleSheet.create({
  header: {
    padding: 8,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  progressInfo: {
    flex: 1,
    marginHorizontal: 8,
  },
  progressBar: {
    marginTop: 8,
    height: 6,
    borderRadius: 3,
  },
});
