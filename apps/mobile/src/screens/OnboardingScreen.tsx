import * as React from 'react';
import { useState, useRef } from 'react';
import { View, StyleSheet, FlatList, Dimensions, Animated } from 'react-native';
import { Text, Button, useTheme } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';

const { width } = Dimensions.get('window');

const ONBOARDING_KEY = '@examloop_onboarded';

interface Slide {
  id: string;
  emoji: string;
  title: string;
  description: string;
}

const slides: Slide[] = [
  {
    id: '1',
    emoji: '🎯',
    title: 'Master Any Subject',
    description: 'Create custom exams for certifications, courses, or any topic you want to master.',
  },
  {
    id: '2',
    emoji: '🧠',
    title: 'Smart Repetition',
    description: 'Our algorithm shows you questions at the perfect time to maximize retention.',
  },
  {
    id: '3',
    emoji: '📈',
    title: 'Adaptive Difficulty',
    description: 'Questions automatically adjust to your level. Challenge yourself, grow faster.',
  },
  {
    id: '4',
    emoji: '🚀',
    title: 'Ready to Start?',
    description: 'Join thousands of learners achieving their goals with ExamLoop.',
  },
];

export default function OnboardingScreen({ navigation }: any) {
  const theme = useTheme();
  const [currentIndex, setCurrentIndex] = useState(0);
  const flatListRef = useRef<FlatList>(null);
  const scrollX = useRef(new Animated.Value(0)).current;

  const handleNext = () => {
    if (currentIndex < slides.length - 1) {
      flatListRef.current?.scrollToIndex({ index: currentIndex + 1 });
    } else {
      completeOnboarding();
    }
  };

  const handleSkip = () => {
    completeOnboarding();
  };

  const completeOnboarding = async () => {
    try {
      await AsyncStorage.setItem(ONBOARDING_KEY, 'true');
      navigation.replace('Board');
    } catch (e) {
      console.error('Failed to save onboarding state:', e);
      navigation.replace('Board');
    }
  };

  const renderSlide = ({ item }: { item: Slide }) => (
    <View style={[styles.slide, { width }]}>
      <Text style={styles.emoji}>{item.emoji}</Text>
      <Text variant="headlineMedium" style={[styles.title, { color: theme.colors.onBackground }]}>
        {item.title}
      </Text>
      <Text variant="bodyLarge" style={[styles.description, { color: theme.colors.onSurfaceVariant }]}>
        {item.description}
      </Text>
    </View>
  );

  const renderDots = () => (
    <View style={styles.dotsContainer}>
      {slides.map((_, index) => {
        const inputRange = [(index - 1) * width, index * width, (index + 1) * width];
        
        const dotWidth = scrollX.interpolate({
          inputRange,
          outputRange: [8, 24, 8],
          extrapolate: 'clamp',
        });

        const opacity = scrollX.interpolate({
          inputRange,
          outputRange: [0.3, 1, 0.3],
          extrapolate: 'clamp',
        });

        return (
          <Animated.View
            key={index}
            style={[
              styles.dot,
              { 
                width: dotWidth, 
                opacity,
                backgroundColor: theme.colors.primary,
              },
            ]}
          />
        );
      })}
    </View>
  );

  const isLastSlide = currentIndex === slides.length - 1;

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <View style={styles.header}>
        {!isLastSlide && (
          <Button mode="text" onPress={handleSkip} textColor={theme.colors.onSurfaceVariant}>
            Skip
          </Button>
        )}
      </View>

      <FlatList
        ref={flatListRef}
        data={slides}
        renderItem={renderSlide}
        keyExtractor={(item) => item.id}
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        onScroll={Animated.event(
          [{ nativeEvent: { contentOffset: { x: scrollX } } }],
          { useNativeDriver: false }
        )}
        onMomentumScrollEnd={(e) => {
          const index = Math.round(e.nativeEvent.contentOffset.x / width);
          setCurrentIndex(index);
        }}
      />

      {renderDots()}

      <View style={styles.footer}>
        <Button 
          mode="contained" 
          onPress={handleNext}
          style={styles.button}
          contentStyle={styles.buttonContent}
        >
          {isLastSlide ? "Let's Go!" : 'Next'}
        </Button>
      </View>
    </SafeAreaView>
  );
}

export async function checkOnboardingStatus(): Promise<boolean> {
  try {
    const value = await AsyncStorage.getItem(ONBOARDING_KEY);
    return value === 'true';
  } catch (e) {
    return false;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    padding: 16,
    minHeight: 60,
  },
  slide: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 40,
  },
  emoji: {
    fontSize: 80,
    marginBottom: 32,
  },
  title: {
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 16,
  },
  description: {
    textAlign: 'center',
    lineHeight: 24,
  },
  dotsContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginVertical: 32,
  },
  dot: {
    height: 8,
    borderRadius: 4,
    marginHorizontal: 4,
  },
  footer: {
    padding: 24,
    paddingBottom: 32,
  },
  button: {
    borderRadius: 12,
  },
  buttonContent: {
    paddingVertical: 8,
  },
});
