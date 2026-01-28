import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Linking, Alert } from 'react-native';
import { createCheckout } from '../api/client';

export default function PaywallScreen({ navigation }: any) {
  const [loading, setLoading] = useState(false);

  const handleCheckout = async () => {
    try {
      setLoading(true);
      const { checkoutUrl } = await createCheckout();
      
      // Open checkout URL in browser
      const supported = await Linking.canOpenURL(checkoutUrl);
      if (supported) {
        await Linking.openURL(checkoutUrl);
      } else {
        Alert.alert('Error', 'Cannot open checkout URL');
      }
    } catch (error) {
      console.error('Error creating checkout:', error);
      Alert.alert('Error', 'Failed to create checkout session');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backButton}>← Back</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Go Premium</Text>
      </View>

      <View style={styles.content}>
        <View style={styles.heroSection}>
          <Text style={styles.heroTitle}>ExamLoop Premium</Text>
          <Text style={styles.heroSubtitle}>
            Unlock your full learning potential
          </Text>
        </View>

        <View style={styles.featuresSection}>
          <View style={styles.feature}>
            <Text style={styles.featureIcon}>✓</Text>
            <Text style={styles.featureText}>Unlimited goals and flashcards</Text>
          </View>
          <View style={styles.feature}>
            <Text style={styles.featureIcon}>✓</Text>
            <Text style={styles.featureText}>Advanced spaced repetition</Text>
          </View>
          <View style={styles.feature}>
            <Text style={styles.featureIcon}>✓</Text>
            <Text style={styles.featureText}>Detailed progress analytics</Text>
          </View>
          <View style={styles.feature}>
            <Text style={styles.featureIcon}>✓</Text>
            <Text style={styles.featureText}>Cloud sync across devices</Text>
          </View>
          <View style={styles.feature}>
            <Text style={styles.featureIcon}>✓</Text>
            <Text style={styles.featureText}>Priority support</Text>
          </View>
        </View>

        <View style={styles.pricingSection}>
          <Text style={styles.price}>$9.99</Text>
          <Text style={styles.priceSubtext}>per month</Text>
        </View>

        <TouchableOpacity
          style={[styles.checkoutButton, loading && styles.checkoutButtonDisabled]}
          onPress={handleCheckout}
          disabled={loading}
        >
          <Text style={styles.checkoutButtonText}>
            {loading ? 'Loading...' : 'Start Premium'}
          </Text>
        </TouchableOpacity>

        <Text style={styles.disclaimer}>
          Cancel anytime. No commitment required.
        </Text>
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
  content: {
    flex: 1,
    padding: 20,
  },
  heroSection: {
    alignItems: 'center',
    marginBottom: 30,
  },
  heroTitle: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#6200ee',
    marginBottom: 10,
  },
  heroSubtitle: {
    fontSize: 18,
    color: '#666',
    textAlign: 'center',
  },
  featuresSection: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 20,
    marginBottom: 30,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  feature: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 15,
  },
  featureIcon: {
    fontSize: 24,
    color: '#28a745',
    marginRight: 15,
    fontWeight: 'bold',
  },
  featureText: {
    fontSize: 16,
    color: '#333',
    flex: 1,
  },
  pricingSection: {
    alignItems: 'center',
    marginBottom: 20,
  },
  price: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#6200ee',
  },
  priceSubtext: {
    fontSize: 16,
    color: '#666',
  },
  checkoutButton: {
    backgroundColor: '#6200ee',
    padding: 18,
    borderRadius: 12,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 4,
  },
  checkoutButtonDisabled: {
    backgroundColor: '#999',
  },
  checkoutButtonText: {
    color: 'white',
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold',
  },
  disclaimer: {
    fontSize: 12,
    color: '#999',
    textAlign: 'center',
  },
});
