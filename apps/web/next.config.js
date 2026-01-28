/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  env: {
    API_URL: process.env.API_URL || 'https://ultimately-www-extract-peer.trycloudflare.com',
  },
}

module.exports = nextConfig
