'use client'

import { useState, useEffect } from 'react'
import { 
  Brain, 
  Play, 
  Plus, 
  TrendingUp, 
  Target, 
  Flame,
  BookOpen,
  Sparkles,
  ChevronRight,
  LogOut
} from 'lucide-react'
import Link from 'next/link'

const API_URL = process.env.API_URL || 'http://localhost:8080'

interface Dashboard {
  dueCount: number
  totalQuestions: number
  goalsCount: number
  masteredCount: number
}

interface Usage {
  reviewsUsed: number
  reviewsLimit: number
  premium: boolean
}

interface Goal {
  id: number
  title: string
  description: string | null
  isPublic: boolean
  stats: { items: number; due: number }
}

export default function AppPage() {
  const [loading, setLoading] = useState(true)
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [usage, setUsage] = useState<Usage | null>(null)
  const [goals, setGoals] = useState<Goal[]>([])
  const [deviceId, setDeviceId] = useState<string>('')

  useEffect(() => {
    const id = localStorage.getItem('examloop_device_id') || `web_${Date.now()}`
    localStorage.setItem('examloop_device_id', id)
    setDeviceId(id)
    
    bootstrap(id)
  }, [])

  const bootstrap = async (id: string) => {
    try {
      // Bootstrap
      const bootRes = await fetch(`${API_URL}/api/v1/bootstrap`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Device-Id': id,
        },
        body: JSON.stringify({}),
      })
      const bootData = await bootRes.json()
      setDashboard(bootData.dashboard)
      setUsage(bootData.usage)

      // Goals
      const goalsRes = await fetch(`${API_URL}/api/v1/goals`, {
        headers: { 'X-Device-Id': id },
      })
      const goalsData = await goalsRes.json()
      setGoals(goalsData.goals || [])
    } catch (error) {
      console.error('Bootstrap failed:', error)
    } finally {
      setLoading(false)
    }
  }

  const startSession = async (goalId?: number) => {
    window.location.href = `/app/session${goalId ? `?goalId=${goalId}` : ''}`
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white">
      {/* Header */}
      <header className="border-b border-slate-700/50 bg-slate-900/80 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Brain className="w-8 h-8 text-primary-500" />
            <span className="text-xl font-bold">ExamLoop</span>
          </div>
          <div className="flex items-center gap-4">
            {!usage?.premium && (
              <Link 
                href="/app/upgrade"
                className="flex items-center gap-2 bg-gradient-to-r from-primary-500 to-accent-500 px-4 py-2 rounded-lg font-medium text-sm"
              >
                <Sparkles className="w-4 h-4" />
                Upgrade
              </Link>
            )}
            <button className="text-slate-400 hover:text-white">
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-10 h-10 bg-orange-500/20 rounded-lg flex items-center justify-center">
                <Flame className="w-5 h-5 text-orange-500" />
              </div>
              <span className="text-slate-400">Due Today</span>
            </div>
            <div className="text-3xl font-bold">{dashboard?.dueCount || 0}</div>
          </div>

          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center">
                <Target className="w-5 h-5 text-green-500" />
              </div>
              <span className="text-slate-400">Mastered</span>
            </div>
            <div className="text-3xl font-bold">{dashboard?.masteredCount || 0}</div>
          </div>

          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-10 h-10 bg-primary-500/20 rounded-lg flex items-center justify-center">
                <BookOpen className="w-5 h-5 text-primary-500" />
              </div>
              <span className="text-slate-400">Total</span>
            </div>
            <div className="text-3xl font-bold">{dashboard?.totalQuestions || 0}</div>
          </div>

          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-10 h-10 bg-accent-500/20 rounded-lg flex items-center justify-center">
                <TrendingUp className="w-5 h-5 text-accent-500" />
              </div>
              <span className="text-slate-400">Today</span>
            </div>
            <div className="text-3xl font-bold">
              {usage?.reviewsUsed || 0}
              <span className="text-lg text-slate-500">/{usage?.reviewsLimit || 20}</span>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid md:grid-cols-2 gap-4 mb-8">
          <button
            onClick={() => startSession()}
            className="bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 rounded-xl p-6 text-left transition-all hover:scale-[1.02] group"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold mb-1">Start Session</h3>
                <p className="text-primary-100">
                  {dashboard?.dueCount || 0} questions waiting for review
                </p>
              </div>
              <Play className="w-12 h-12 text-white/80 group-hover:text-white transition" />
            </div>
          </button>

          <Link
            href="/app/generate"
            className="bg-slate-800/50 hover:bg-slate-700/50 border border-slate-700/50 rounded-xl p-6 text-left transition-all hover:scale-[1.02] group"
          >
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h3 className="text-xl font-bold">AI Generate</h3>
                  {!usage?.premium && (
                    <span className="bg-yellow-500/20 text-yellow-400 text-xs px-2 py-0.5 rounded">Premium</span>
                  )}
                </div>
                <p className="text-slate-400">Create exam on any topic</p>
              </div>
              <Sparkles className="w-12 h-12 text-slate-500 group-hover:text-accent-400 transition" />
            </div>
          </Link>
        </div>

        {/* Exams List */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold">Your Exams</h2>
            <Link 
              href="/app/create"
              className="flex items-center gap-2 text-primary-400 hover:text-primary-300 text-sm font-medium"
            >
              <Plus className="w-4 h-4" />
              Create Exam
            </Link>
          </div>

          <div className="space-y-3">
            {goals.map((goal) => (
              <div
                key={goal.id}
                className="bg-slate-800/50 hover:bg-slate-700/50 border border-slate-700/50 rounded-xl p-4 transition cursor-pointer"
                onClick={() => startSession(goal.id)}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold">{goal.title}</h3>
                      {goal.isPublic && (
                        <span className="bg-blue-500/20 text-blue-400 text-xs px-2 py-0.5 rounded">Public</span>
                      )}
                    </div>
                    <p className="text-sm text-slate-400 mt-1">
                      {goal.stats.items} questions • {goal.stats.due} due
                    </p>
                  </div>
                  <ChevronRight className="w-5 h-5 text-slate-500" />
                </div>
              </div>
            ))}

            {goals.length === 0 && (
              <div className="text-center py-12 text-slate-400">
                <BookOpen className="w-12 h-12 mx-auto mb-4 opacity-50" />
                <p>No exams yet. Create one or generate with AI!</p>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}
