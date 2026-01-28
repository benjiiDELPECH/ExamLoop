import * as React from 'react';
import { createContext, useContext, useState, useEffect } from 'react';
import { useColorScheme } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { MD3DarkTheme, MD3LightTheme, MD3Theme } from 'react-native-paper';

type ThemeMode = 'light' | 'dark' | 'auto';

interface ThemeContextType {
  themeMode: ThemeMode;
  setThemeMode: (mode: ThemeMode) => void;
  theme: typeof MD3DarkTheme;
  isDark: boolean;
}

const THEME_KEY = '@examloop_theme';

const lightTheme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    // Primary - Indigo fort
    primary: '#4f46e5',
    onPrimary: '#ffffff',
    primaryContainer: '#4f46e5',
    onPrimaryContainer: '#ffffff',
    // Secondary
    secondary: '#7c3aed',
    onSecondary: '#ffffff',
    secondaryContainer: '#7c3aed',
    onSecondaryContainer: '#ffffff',
    // Backgrounds - Plus de contraste
    background: '#ffffff',
    onBackground: '#111827',
    surface: '#ffffff',
    onSurface: '#111827',
    surfaceVariant: '#f3f4f6',
    onSurfaceVariant: '#4b5563',
    // Outline plus visible
    outline: '#d1d5db',
    outlineVariant: '#e5e7eb',
    // Error
    error: '#dc2626',
    onError: '#ffffff',
    // Elevation overlay
    elevation: {
      level0: 'transparent',
      level1: '#ffffff',
      level2: '#f9fafb',
      level3: '#f3f4f6',
      level4: '#e5e7eb',
      level5: '#d1d5db',
    },
  },
};

const darkTheme = {
  ...MD3DarkTheme,
  colors: {
    ...MD3DarkTheme.colors,
    primary: '#818cf8',
    onPrimary: '#1e1b4b',
    primaryContainer: '#4338ca',
    onPrimaryContainer: '#e0e7ff',
    secondary: '#a78bfa',
    onSecondary: '#2e1065',
    secondaryContainer: '#6d28d9',
    onSecondaryContainer: '#ede9fe',
    background: '#0f172a',
    onBackground: '#e2e8f0',
    surface: '#1e293b',
    onSurface: '#e2e8f0',
    surfaceVariant: '#334155',
    onSurfaceVariant: '#94a3b8',
    error: '#f87171',
    onError: '#7f1d1d',
  },
};

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const systemColorScheme = useColorScheme();
  const [themeMode, setThemeModeState] = useState<ThemeMode>('auto');
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    loadTheme();
  }, []);

  const loadTheme = async () => {
    try {
      const saved = await AsyncStorage.getItem(THEME_KEY);
      if (saved && ['light', 'dark', 'auto'].includes(saved)) {
        setThemeModeState(saved as ThemeMode);
      }
    } catch (e) {
      console.error('Failed to load theme:', e);
    } finally {
      setIsLoaded(true);
    }
  };

  const setThemeMode = async (mode: ThemeMode) => {
    setThemeModeState(mode);
    try {
      await AsyncStorage.setItem(THEME_KEY, mode);
    } catch (e) {
      console.error('Failed to save theme:', e);
    }
  };

  const isDark = themeMode === 'dark' || (themeMode === 'auto' && systemColorScheme === 'dark');
  const theme = isDark ? darkTheme : lightTheme;

  if (!isLoaded) {
    return null; // ou un splash screen
  }

  return (
    <ThemeContext.Provider value={{ themeMode, setThemeMode, theme, isDark }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useAppTheme() {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useAppTheme must be used within ThemeProvider');
  }
  return context;
}
