'use client'

import { useState } from 'react'
import { useSearchParams } from 'next/navigation'
import { 
  Brain, 
  ArrowLeft,
  Check,
  Sparkles,
  Zap,
  Infinity,
  Crown
} from 'lucide-react'
import Link from 'next/link'

const API_URL = process.env.API_URL || 'http://localhost:8080'

export default function UpgradePage() {
  const searchParams = useSearchParams()
  const reason = searchParams.get('reason')
  const [loading, setLoading] = useState(false)

  const handleUpgrade = async () => {
    setLoading(true)
    const deviceId = localStorage.getItem('examloop_device_id') || ''

    try {
      const res = await fetch(`${API_URL}/api/v1/billing/checkout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Device-Id': deviceId,
        },
        body: JSON.stringify({}),
      })

      const data = await res.json()
      
      if (data.url) {
        window.location.href = data.url
      }
    } catch (error) {
      console.error('Checkout failed:', error)
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white">
      {/* Header */}
      <header className="border-b border-slate-700/50 bg-slate-900/80 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-3xl mx-auto px-4 py-4 flex items-center justify-between">
          <Link href="/app" className="flex items-center gap-2 text-slate-400 hover:text-white">
            <ArrowLeft className="w-5 h-5" />
            Back
          </Link>
          <div className="flex items-center gap-2">
            <Brain className="w-6 h-6 text-primary-500" />
            <span className="font-bold">ExamLoop</span>
          </div>
          <div className="w-16" />
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-8">
        {/* Reason banner */}
        {reason && (
          <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-xl p-4 mb-8 text-center">
            {reason === 'quota' && (
              <p>You've reached your daily limit of 20 reviews. Upgrade to continue learning!</p>
            )}
            {reason === 'ai' && (
              <p>AI question generation is a Premium feature. Upgrade to unlock it!</p>
            )}
          </div>
        )}

        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-r from-yellow-500/20 to-orange-500/20 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <Crown className="w-8 h-8 text-yellow-400" />
          </div>
          <h1 className="text-3xl font-bold mb-2">Upgrade to Premium</h1>
          <p className="text-slate-400">
            Unlock your full learning potential
          </p>
        </div>

        <div className="bg-gradient-to-br from-primary-500/10 to-accent-500/10 rounded-2xl p-8 border border-primary-500/20 mb-8">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h2 className="text-2xl font-bold">Premium</h2>
              <p className="text-slate-400">Everything you need to master any topic</p>
            </div>
            <div className="text-right">
              <div className="text-4xl font-bold">$9</div>
              <div className="text-slate-400">/month</div>
            </div>
          </div>

          <div className="space-y-4 mb-8">
            {[
              { icon: Infinity, text: "Unlimited daily reviews" },
              { icon: Sparkles, text: "AI question generation" },
              { icon: Zap, text: "Create unlimited exams" },
              { icon: Check, text: "Priority support" },
              { icon: Check, text: "Export your data" },
              { icon: Check, text: "Upload documents (coming soon)" },
            ].map((item, i) => (
              <div key={i} className="flex items-center gap-3">
                <div className="w-8 h-8 bg-primary-500/20 rounded-lg flex items-center justify-center">
                  <item.icon className="w-4 h-4 text-primary-400" />
                </div>
                <span>{item.text}</span>
              </div>
            ))}
          </div>

          <button
            onClick={handleUpgrade}
            disabled={loading}
            className="w-full bg-gradient-to-r from-primary-600 to-accent-600 hover:from-primary-500 hover:to-accent-500 py-4 rounded-xl font-semibold flex items-center justify-center gap-2 transition"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            ) : (
              <>
                <Crown className="w-5 h-5" />
                Upgrade Now
              </>
            )}
          </button>

          <p className="text-sm text-slate-500 text-center mt-4">
            Cancel anytime. 7-day money-back guarantee.
          </p>
        </div>

        <div className="text-center text-slate-500">
          <p>Questions? <a href="mailto:hello@examloop.com" className="text-primary-400">Contact us</a></p>
        </div>
      </main>
    </div>
  )
}
