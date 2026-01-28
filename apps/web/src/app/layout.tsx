import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'ExamLoop - Stop Forgetting What You Learn',
  description: 'The smart revision system that helps you retain knowledge forever. Spaced repetition + adaptive difficulty = 84% retention rate.',
  keywords: ['spaced repetition', 'learning', 'flashcards', 'exam prep', 'spring certification', 'developer tools'],
  openGraph: {
    title: 'ExamLoop - Stop Forgetting What You Learn',
    description: 'The smart revision system for developers. 84% retention rate.',
    url: 'https://examloop.com',
    siteName: 'ExamLoop',
    images: [
      {
        url: '/og-image.png',
        width: 1200,
        height: 630,
      },
    ],
    locale: 'en_US',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'ExamLoop - Stop Forgetting What You Learn',
    description: 'The smart revision system for developers.',
    images: ['/og-image.png'],
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={inter.className}>{children}</body>
    </html>
  )
}
