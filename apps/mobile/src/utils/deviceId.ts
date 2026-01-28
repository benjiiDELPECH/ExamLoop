import * as Device from 'expo-device';
import AsyncStorage from '@react-native-async-storage/async-storage';

const DEVICE_ID_KEY = '@examloop:deviceId';

export const getDeviceId = async (): Promise<string> => {
  try {
    // Try to get existing device ID from storage
    let deviceId = await AsyncStorage.getItem(DEVICE_ID_KEY);
    
    if (!deviceId) {
      // Generate a new device ID
      deviceId = `${Device.osName}-${Device.modelName}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      await AsyncStorage.setItem(DEVICE_ID_KEY, deviceId);
    }
    
    return deviceId;
  } catch (error) {
    console.error('Error getting device ID:', error);
    // Fallback to a temporary ID
    return `temp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
};
