import * as React from 'react';
import { useState } from 'react';
import { View, StyleSheet, ScrollView, Linking } from 'react-native';
import { 
  Card, 
  Text, 
  Button, 
  useTheme,
  Surface,
  List,
  Divider,
  IconButton
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { createCheckout } from '../api/client';

export default function PaywallScreen({ navigation }: any) {
  const theme = useTheme();
  const [loading, setLoading] = useState(false);

  const handleUpgrade = async () => {
    try {
      setLoading(true);
      const { url } = await createCheckout();
      await Linking.openURL(url);
    } catch (error) {
      console.error('Error creating checkout:', error);
    } finally {
      setLoading(false);
    }
  };

  const freeFeatures = [
    { title: '20 reviews per day', icon: 'check-circle' },
    { title: 'Access public exams', icon: 'check-circle' },
    { title: 'Basic spaced repetition', icon: 'check-circle' },
  ];

  const premiumFeatures = [
    { title: 'Unlimited reviews', icon: 'infinity', highlight: true },
    { title: 'AI question generation', icon: 'robot', highlight: true },
    { title: 'Advanced analytics', icon: 'chart-line', highlight: true },
    { title: 'Create unlimited exams', icon: 'folder-plus' },
    { title: 'Priority support', icon: 'headset' },
    { title: 'Early access to features', icon: 'rocket' },
  ];

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <ScrollView contentContainerStyle={styles.content}>
        {/* Header */}
        <View style={styles.header}>
          <IconButton 
            icon="close" 
            onPress={() => navigation.goBack()}
            style={styles.closeButton}
          />
          <Text variant="displaySmall" style={styles.emoji}>👑</Text>
          <Text variant="headlineMedium" style={styles.title}>
            Unlock Your Full Potential
          </Text>
          <Text variant="bodyLarge" style={styles.subtitle}>
            Master any subject faster with unlimited access
          </Text>
        </View>

        {/* Free Plan */}
        <Card style={styles.planCard} mode="outlined">
          <Card.Content>
            <Text variant="titleLarge" style={styles.planTitle}>Free</Text>
            <Text variant="headlineMedium" style={styles.planPrice}>$0</Text>
            <Text variant="bodySmall" style={styles.planPeriod}>forever</Text>
            
            <Divider style={styles.divider} />
            
            {freeFeatures.map((feature, index) => (
              <List.Item
                key={index}
                title={feature.title}
                left={props => <List.Icon {...props} icon={feature.icon} color="#22c55e" />}
                titleStyle={styles.featureText}
              />
            ))}
          </Card.Content>
        </Card>

        {/* Premium Plan */}
        <Card style={[styles.planCard, styles.premiumCard]} mode="elevated">
          <Surface style={styles.recommendedBadge} elevation={0}>
            <Text variant="labelSmall" style={styles.recommendedText}>
              RECOMMENDED
            </Text>
          </Surface>
          
          <Card.Content>
            <Text variant="titleLarge" style={[styles.planTitle, styles.premiumTitle]}>
              Premium
            </Text>
            <View style={styles.priceRow}>
              <Text variant="headlineLarge" style={styles.premiumPrice}>$9.99</Text>
              <Text variant="bodyMedium" style={styles.planPeriod}>/month</Text>
            </View>
            
            <Divider style={styles.divider} />
            
            {premiumFeatures.map((feature, index) => (
              <List.Item
                key={index}
                title={feature.title}
                left={props => (
                  <List.Icon 
                    {...props} 
                    icon={feature.icon} 
                    color={feature.highlight ? '#6366f1' : '#22c55e'} 
                  />
                )}
                titleStyle={[
                  styles.featureText,
                  feature.highlight && styles.highlightFeature
                ]}
              />
            ))}
          </Card.Content>

          <Card.Actions style={styles.cardActions}>
            <Button 
              mode="contained" 
              onPress={handleUpgrade}
              loading={loading}
              disabled={loading}
              style={styles.upgradeButton}
              contentStyle={styles.upgradeButtonContent}
              icon="crown"
            >
              Upgrade to Premium
            </Button>
          </Card.Actions>
        </Card>

        {/* Trust badges */}
        <View style={styles.trustSection}>
          <Text variant="bodySmall" style={styles.trustText}>
            🔒 Secure payment via Stripe
          </Text>
          <Text variant="bodySmall" style={styles.trustText}>
            ✓ Cancel anytime
          </Text>
          <Text variant="bodySmall" style={styles.trustText}>
            💰 7-day money-back guarantee
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    padding: 16,
    paddingBottom: 32,
  },
  header: {
    alignItems: 'center',
    marginBottom: 24,
    position: 'relative',
  },
  closeButton: {
    position: 'absolute',
    top: -8,
    left: -8,
  },
  emoji: {
    marginBottom: 12,
  },
  title: {
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    textAlign: 'center',
    opacity: 0.7,
  },
  planCard: {
    marginBottom: 16,
  },
  premiumCard: {
    borderColor: '#6366f1',
    borderWidth: 2,
    position: 'relative',
    overflow: 'visible',
  },
  recommendedBadge: {
    position: 'absolute',
    top: -12,
    alignSelf: 'center',
    backgroundColor: '#6366f1',
    paddingHorizontal: 16,
    paddingVertical: 4,
    borderRadius: 12,
  },
  recommendedText: {
    color: 'white',
    fontWeight: 'bold',
  },
  planTitle: {
    marginTop: 8,
  },
  premiumTitle: {
    marginTop: 16,
  },
  planPrice: {
    fontWeight: 'bold',
    marginTop: 8,
  },
  priceRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
    marginTop: 8,
  },
  premiumPrice: {
    fontWeight: 'bold',
    color: '#6366f1',
  },
  planPeriod: {
    opacity: 0.6,
    marginLeft: 4,
  },
  divider: {
    marginVertical: 16,
  },
  featureText: {
    fontSize: 15,
  },
  highlightFeature: {
    fontWeight: '600',
  },
  cardActions: {
    padding: 16,
  },
  upgradeButton: {
    flex: 1,
  },
  upgradeButtonContent: {
    paddingVertical: 8,
  },
  trustSection: {
    alignItems: 'center',
    marginTop: 16,
  },
  trustText: {
    opacity: 0.6,
    marginVertical: 4,
  },
});
