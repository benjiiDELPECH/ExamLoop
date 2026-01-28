import * as React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text, useTheme } from 'react-native-paper';

interface StatCardProps {
  value: number | string;
  label: string;
  highlight?: boolean;
}

export function StatCard({ value, label, highlight = true }: StatCardProps) {
  const theme = useTheme();
  
  return (
    <View style={styles.container}>
      <Text 
        variant="headlineSmall" 
        style={[
          styles.value, 
          highlight && { color: theme.colors.primary }
        ]}
      >
        {value}
      </Text>
      <Text variant="bodySmall" style={styles.label}>
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
  },
  value: {
    fontWeight: 'bold',
  },
  label: {
    opacity: 0.6,
  },
});
