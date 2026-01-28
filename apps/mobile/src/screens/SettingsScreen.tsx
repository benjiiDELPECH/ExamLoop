import * as React from 'react';
import { View, StyleSheet, ScrollView } from 'react-native';
import { 
  Text, 
  useTheme,
  Surface,
  List,
  RadioButton,
  Divider,
  IconButton,
  Switch
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAppTheme } from '../context/ThemeContext';

export default function SettingsScreen({ navigation }: any) {
  const theme = useTheme();
  const { themeMode, setThemeMode, isDark } = useAppTheme();

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      {/* Header */}
      <Surface style={styles.header} elevation={1}>
        <View style={styles.headerRow}>
          <IconButton icon="arrow-left" onPress={() => navigation.goBack()} />
          <Text variant="titleLarge" style={styles.headerTitle}>Settings</Text>
          <View style={{ width: 48 }} />
        </View>
      </Surface>

      <ScrollView style={styles.content}>
        {/* Appearance Section */}
        <Text variant="titleSmall" style={[styles.sectionTitle, { color: theme.colors.primary }]}>
          APPEARANCE
        </Text>
        
        <Surface style={styles.card} elevation={1}>
          <List.Item
            title="Light"
            description="Classic light theme"
            left={props => <List.Icon {...props} icon="white-balance-sunny" />}
            right={() => (
              <RadioButton
                value="light"
                status={themeMode === 'light' ? 'checked' : 'unchecked'}
                onPress={() => setThemeMode('light')}
              />
            )}
            onPress={() => setThemeMode('light')}
          />
          <Divider />
          <List.Item
            title="Dark"
            description="Easy on the eyes"
            left={props => <List.Icon {...props} icon="moon-waning-crescent" />}
            right={() => (
              <RadioButton
                value="dark"
                status={themeMode === 'dark' ? 'checked' : 'unchecked'}
                onPress={() => setThemeMode('dark')}
              />
            )}
            onPress={() => setThemeMode('dark')}
          />
          <Divider />
          <List.Item
            title="System"
            description="Follow device settings"
            left={props => <List.Icon {...props} icon="cellphone" />}
            right={() => (
              <RadioButton
                value="auto"
                status={themeMode === 'auto' ? 'checked' : 'unchecked'}
                onPress={() => setThemeMode('auto')}
              />
            )}
            onPress={() => setThemeMode('auto')}
          />
        </Surface>

        {/* About Section */}
        <Text variant="titleSmall" style={[styles.sectionTitle, { color: theme.colors.primary }]}>
          ABOUT
        </Text>
        
        <Surface style={styles.card} elevation={1}>
          <List.Item
            title="Version"
            description="0.1.0"
            left={props => <List.Icon {...props} icon="information" />}
          />
          <Divider />
          <List.Item
            title="Rate ExamLoop"
            description="Help us improve"
            left={props => <List.Icon {...props} icon="star" />}
            right={props => <List.Icon {...props} icon="chevron-right" />}
            onPress={() => {}}
          />
          <Divider />
          <List.Item
            title="Privacy Policy"
            left={props => <List.Icon {...props} icon="shield-account" />}
            right={props => <List.Icon {...props} icon="chevron-right" />}
            onPress={() => {}}
          />
          <Divider />
          <List.Item
            title="Terms of Service"
            left={props => <List.Icon {...props} icon="file-document" />}
            right={props => <List.Icon {...props} icon="chevron-right" />}
            onPress={() => {}}
          />
        </Surface>

        {/* Current Theme Preview */}
        <Surface style={[styles.previewCard, { backgroundColor: theme.colors.surfaceVariant }]} elevation={0}>
          <Text variant="bodySmall" style={{ opacity: 0.7 }}>
            Current: {isDark ? '🌙 Dark Mode' : '☀️ Light Mode'}
          </Text>
        </Surface>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    padding: 8,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  headerTitle: {
    flex: 1,
    textAlign: 'center',
    fontWeight: '600',
  },
  content: {
    flex: 1,
    padding: 16,
  },
  sectionTitle: {
    marginBottom: 8,
    marginTop: 16,
    marginLeft: 4,
    fontWeight: '600',
  },
  card: {
    borderRadius: 12,
    overflow: 'hidden',
  },
  previewCard: {
    marginTop: 24,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
});
