'use client'

import { useState, useEffect } from 'react'
import { useSearchParams } from 'next/navigation'
import { 
  Brain, 
  Check, 
  X, 
  ChevronRight,
  Trophy,
  ArrowLeft,
  Lightbulb
} from 'lucide-react'
import Link from 'next/link'

const API_URL = process.env.API_URL || 'http://localhost:8080'

interface Question {
  id: number
  prompt: string
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'OPEN'
  choices: Array<{ id: string; label: string }> | null
  difficulty: string
  chapter: string | null
}

interface ReviewResult {
  result: 'CORRECT' | 'INCORRECT'
  reviewState: {
    masteryLevel: string
    successCount: number
    failCount: number
  }
}

export default function SessionPage() {
  const searchParams = useSearchParams()
  const goalId = searchParams.get('goalId')
  
  const [loading, setLoading] = useState(true)
  const [questions, setQuestions] = useState<Question[]>([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null)
  const [showResult, setShowResult] = useState(false)
  const [lastResult, setLastResult] = useState<ReviewResult | null>(null)
  const [score, setScore] = useState({ correct: 0, incorrect: 0 })
  const [sessionComplete, setSessionComplete] = useState(false)
  const [deviceId, setDeviceId] = useState('')

  useEffect(() => {
    const id = localStorage.getItem('examloop_device_id') || ''
    setDeviceId(id)
    loadSession(id)
  }, [])

  const loadSession = async (id: string) => {
    try {
      const body: any = { limit: 10 }
      if (goalId) body.goalId = parseInt(goalId)

      const res = await fetch(`${API_URL}/api/v1/sessions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Device-Id': id,
        },
        body: JSON.stringify(body),
      })
      const data = await res.json()
      setQuestions(data.questions || [])
      
      if (!data.questions || data.questions.length === 0) {
        setSessionComplete(true)
      }
    } catch (error) {
      console.error('Failed to load session:', error)
    } finally {
      setLoading(false)
    }
  }

  const submitAnswer = async (isCorrect: boolean) => {
    const question = questions[currentIndex]
    
    try {
      const res = await fetch(`${API_URL}/api/v1/reviews`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Device-Id': deviceId,
        },
        body: JSON.stringify({
          questionId: question.id,
          correct: isCorrect,
          selectedChoiceIds: selectedAnswer ? [selectedAnswer] : undefined,
        }),
      })

      if (res.status === 429) {
        // Quota exceeded
        window.location.href = '/app/upgrade?reason=quota'
        return
      }

      const data = await res.json()
      setLastResult(data)
      setShowResult(true)
      setScore(prev => ({
        correct: prev.correct + (isCorrect ? 1 : 0),
        incorrect: prev.incorrect + (isCorrect ? 0 : 1),
      }))
    } catch (error) {
      console.error('Failed to submit review:', error)
    }
  }

  const nextQuestion = () => {
    if (currentIndex + 1 >= questions.length) {
      setSessionComplete(true)
    } else {
      setCurrentIndex(prev => prev + 1)
      setSelectedAnswer(null)
      setShowResult(false)
      setLastResult(null)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin" />
      </div>
    )
  }

  if (sessionComplete) {
    const total = score.correct + score.incorrect
    const percentage = total > 0 ? Math.round((score.correct / total) * 100) : 0

    return (
      <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white flex items-center justify-center px-4">
        <div className="max-w-md w-full text-center">
          <div className="w-20 h-20 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-6">
            <Trophy className="w-10 h-10 text-green-500" />
          </div>
          <h1 className="text-3xl font-bold mb-2">Session Complete!</h1>
          <p className="text-slate-400 mb-8">Great job on your review session</p>

          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50 mb-8">
            <div className="text-5xl font-bold mb-2">{percentage}%</div>
            <p className="text-slate-400">
              {score.correct} correct • {score.incorrect} incorrect
            </p>
          </div>

          <div className="flex flex-col gap-3">
            <Link
              href="/app"
              className="bg-primary-600 hover:bg-primary-500 py-3 rounded-xl font-semibold transition"
            >
              Back to Dashboard
            </Link>
            <button
              onClick={() => window.location.reload()}
              className="bg-slate-700 hover:bg-slate-600 py-3 rounded-xl font-semibold transition"
            >
              Start Another Session
            </button>
          </div>
        </div>
      </div>
    )
  }

  const question = questions[currentIndex]

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white">
      {/* Header */}
      <header className="border-b border-slate-700/50 bg-slate-900/80 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-3xl mx-auto px-4 py-4 flex items-center justify-between">
          <Link href="/app" className="flex items-center gap-2 text-slate-400 hover:text-white">
            <ArrowLeft className="w-5 h-5" />
            Exit
          </Link>
          <div className="flex items-center gap-2">
            <Brain className="w-6 h-6 text-primary-500" />
            <span className="font-bold">ExamLoop</span>
          </div>
          <div className="text-slate-400">
            {currentIndex + 1}/{questions.length}
          </div>
        </div>
        {/* Progress bar */}
        <div className="h-1 bg-slate-700">
          <div 
            className="h-full bg-primary-500 transition-all duration-300"
            style={{ width: `${((currentIndex + 1) / questions.length) * 100}%` }}
          />
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-8">
        {/* Question */}
        <div className="bg-slate-800/50 rounded-2xl p-8 border border-slate-700/50 mb-6">
          <div className="flex items-center gap-2 mb-4">
            {question.chapter && (
              <span className="bg-primary-500/20 text-primary-400 text-sm px-3 py-1 rounded-full">
                {question.chapter}
              </span>
            )}
            <span className="bg-slate-700 text-slate-400 text-sm px-3 py-1 rounded-full">
              {question.difficulty}
            </span>
          </div>
          
          <h2 className="text-xl md:text-2xl font-semibold mb-6">{question.prompt}</h2>

          {question.type !== 'OPEN' && question.choices && (
            <div className="space-y-3">
              {question.choices.map((choice) => (
                <button
                  key={choice.id}
                  onClick={() => !showResult && setSelectedAnswer(choice.id)}
                  disabled={showResult}
                  className={`w-full text-left p-4 rounded-xl border transition ${
                    selectedAnswer === choice.id
                      ? showResult
                        ? lastResult?.result === 'CORRECT'
                          ? 'bg-green-500/20 border-green-500'
                          : 'bg-red-500/20 border-red-500'
                        : 'bg-primary-500/20 border-primary-500'
                      : 'bg-slate-700/50 border-slate-600 hover:border-slate-500'
                  } ${showResult ? 'cursor-default' : 'cursor-pointer'}`}
                >
                  {choice.label}
                </button>
              ))}
            </div>
          )}

          {question.type === 'OPEN' && !showResult && (
            <div className="bg-slate-700/30 rounded-xl p-4 text-slate-400 text-center">
              Think about the answer, then rate yourself
            </div>
          )}
        </div>

        {/* Result feedback */}
        {showResult && lastResult && (
          <div className={`rounded-xl p-6 mb-6 ${
            lastResult.result === 'CORRECT' 
              ? 'bg-green-500/10 border border-green-500/30' 
              : 'bg-red-500/10 border border-red-500/30'
          }`}>
            <div className="flex items-center gap-3 mb-3">
              {lastResult.result === 'CORRECT' ? (
                <Check className="w-6 h-6 text-green-500" />
              ) : (
                <X className="w-6 h-6 text-red-500" />
              )}
              <span className="font-semibold">
                {lastResult.result === 'CORRECT' ? 'Correct!' : 'Incorrect'}
              </span>
            </div>
            <div className="flex items-center gap-2 text-sm text-slate-400">
              <Lightbulb className="w-4 h-4" />
              Mastery: {lastResult.reviewState.masteryLevel}
            </div>
          </div>
        )}

        {/* Actions */}
        {!showResult ? (
          <div className="flex gap-4">
            {question.type === 'OPEN' ? (
              <>
                <button
                  onClick={() => submitAnswer(false)}
                  className="flex-1 bg-red-600 hover:bg-red-500 py-4 rounded-xl font-semibold flex items-center justify-center gap-2 transition"
                >
                  <X className="w-5 h-5" />
                  I didn't know
                </button>
                <button
                  onClick={() => submitAnswer(true)}
                  className="flex-1 bg-green-600 hover:bg-green-500 py-4 rounded-xl font-semibold flex items-center justify-center gap-2 transition"
                >
                  <Check className="w-5 h-5" />
                  I knew it
                </button>
              </>
            ) : (
              <button
                onClick={() => submitAnswer(selectedAnswer !== null)}
                disabled={selectedAnswer === null}
                className="flex-1 bg-primary-600 hover:bg-primary-500 disabled:bg-slate-700 disabled:text-slate-500 py-4 rounded-xl font-semibold transition"
              >
                Check Answer
              </button>
            )}
          </div>
        ) : (
          <button
            onClick={nextQuestion}
            className="w-full bg-primary-600 hover:bg-primary-500 py-4 rounded-xl font-semibold flex items-center justify-center gap-2 transition"
          >
            {currentIndex + 1 >= questions.length ? 'Finish Session' : 'Next Question'}
            <ChevronRight className="w-5 h-5" />
          </button>
        )}
      </main>
    </div>
  )
}
