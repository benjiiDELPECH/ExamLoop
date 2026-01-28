'use client'

import { useState } from 'react'
import { Brain, ArrowRight } from 'lucide-react'
import Link from 'next/link'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    
    // TODO: Implement actual login
    const deviceId = `web_${btoa(email).slice(0, 20)}`
    localStorage.setItem('examloop_device_id', deviceId)
    
    setTimeout(() => {
      window.location.href = '/app'
    }, 1000)
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <Link href="/" className="flex items-center justify-center gap-2 mb-8">
          <Brain className="w-10 h-10 text-primary-500" />
          <span className="text-2xl font-bold">ExamLoop</span>
        </Link>

        {/* Card */}
        <div className="bg-slate-800/50 rounded-2xl p-8 border border-slate-700/50">
          <h1 className="text-2xl font-bold text-center mb-2">Welcome back</h1>
          <p className="text-slate-400 text-center mb-8">Continue your learning journey</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                className="w-full bg-slate-700/50 border border-slate-600 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-primary-600 hover:bg-primary-500 disabled:bg-primary-600/50 py-3 rounded-xl font-semibold flex items-center justify-center gap-2 transition"
            >
              {loading ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>
                  Log in
                  <ArrowRight className="w-5 h-5" />
                </>
              )}
            </button>
          </form>
        </div>

        <p className="text-center mt-6 text-slate-400">
          Don't have an account?{' '}
          <Link href="/signup" className="text-primary-400 hover:text-primary-300">
            Sign up free
          </Link>
        </p>
      </div>
    </div>
  )
}
