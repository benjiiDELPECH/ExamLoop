'use client'

import { useState } from 'react'
import { 
  Brain, 
  Sparkles, 
  ArrowLeft,
  Loader2,
  Check,
  AlertCircle
} from 'lucide-react'
import Link from 'next/link'

const API_URL = process.env.API_URL || 'http://localhost:8080'

export default function GeneratePage() {
  const [topic, setTopic] = useState('')
  const [description, setDescription] = useState('')
  const [questionCount, setQuestionCount] = useState(10)
  const [difficulty, setDifficulty] = useState('BALANCED')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [generatedExamId, setGeneratedExamId] = useState<number | null>(null)

  const handleGenerate = async () => {
    if (!topic.trim()) {
      setError('Please enter a topic')
      return
    }

    setLoading(true)
    setError(null)

    const deviceId = localStorage.getItem('examloop_device_id') || ''

    try {
      const res = await fetch(`${API_URL}/api/v1/exams/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Device-Id': deviceId,
        },
        body: JSON.stringify({
          topic: topic.trim(),
          description: description.trim() || undefined,
          questionCount,
          difficultyMix: difficulty,
          questionTypes: ['SINGLE_CHOICE', 'OPEN'],
          language: 'fr',
        }),
      })

      if (res.status === 402) {
        // Premium required
        window.location.href = '/app/upgrade?reason=ai'
        return
      }

      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.detail || 'Generation failed')
      }

      const data = await res.json()
      setSuccess(true)
      setGeneratedExamId(data.examId)
    } catch (err: any) {
      setError(err.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  if (success && generatedExamId) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white flex items-center justify-center px-4">
        <div className="max-w-md w-full text-center">
          <div className="w-20 h-20 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-6">
            <Check className="w-10 h-10 text-green-500" />
          </div>
          <h1 className="text-3xl font-bold mb-2">Exam Generated!</h1>
          <p className="text-slate-400 mb-8">
            Your AI-generated exam on "{topic}" is ready
          </p>

          <div className="flex flex-col gap-3">
            <Link
              href={`/app/session?goalId=${generatedExamId}`}
              className="bg-primary-600 hover:bg-primary-500 py-3 rounded-xl font-semibold transition"
            >
              Start Learning Now
            </Link>
            <Link
              href="/app"
              className="bg-slate-700 hover:bg-slate-600 py-3 rounded-xl font-semibold transition"
            >
              Back to Dashboard
            </Link>
          </div>
        </div>
      </div>
    )
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
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-r from-primary-500/20 to-accent-500/20 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <Sparkles className="w-8 h-8 text-accent-400" />
          </div>
          <h1 className="text-3xl font-bold mb-2">AI Exam Generator</h1>
          <p className="text-slate-400">
            Enter any topic and get tailored questions instantly
          </p>
        </div>

        <div className="bg-slate-800/50 rounded-2xl p-6 border border-slate-700/50 space-y-6">
          {error && (
            <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-4 flex items-center gap-3">
              <AlertCircle className="w-5 h-5 text-red-400" />
              <span className="text-red-300">{error}</span>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium mb-2">Topic *</label>
            <input
              type="text"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="e.g., Spring Security OAuth2"
              className="w-full bg-slate-700/50 border border-slate-600 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">Description (optional)</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Focus on specific aspects..."
              rows={3}
              className="w-full bg-slate-700/50 border border-slate-600 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-2">Questions</label>
              <select
                value={questionCount}
                onChange={(e) => setQuestionCount(parseInt(e.target.value))}
                className="w-full bg-slate-700/50 border border-slate-600 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value={5}>5 questions</option>
                <option value={10}>10 questions</option>
                <option value={15}>15 questions</option>
                <option value={20}>20 questions</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Difficulty</label>
              <select
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value)}
                className="w-full bg-slate-700/50 border border-slate-600 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="EASY_FOCUSED">Easy focused</option>
                <option value="BALANCED">Balanced</option>
                <option value="HARD_FOCUSED">Hard focused</option>
                <option value="PROGRESSIVE">Progressive</option>
              </select>
            </div>
          </div>

          <button
            onClick={handleGenerate}
            disabled={loading || !topic.trim()}
            className="w-full bg-gradient-to-r from-primary-600 to-accent-600 hover:from-primary-500 hover:to-accent-500 disabled:from-slate-600 disabled:to-slate-600 py-4 rounded-xl font-semibold flex items-center justify-center gap-2 transition"
          >
            {loading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Sparkles className="w-5 h-5" />
                Generate Exam
              </>
            )}
          </button>

          <p className="text-sm text-slate-500 text-center">
            Generation typically takes 10-30 seconds depending on the topic complexity
          </p>
        </div>
      </main>
    </div>
  )
}
