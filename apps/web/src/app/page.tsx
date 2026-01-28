'use client'

import { useState } from 'react'
import { motion } from 'framer-motion'
import { 
  Brain, 
  Zap, 
  Target, 
  TrendingUp, 
  Sparkles, 
  Check, 
  ArrowRight,
  Play,
  Star,
  Clock,
  BookOpen
} from 'lucide-react'

export default function LandingPage() {
  const [email, setEmail] = useState('')

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 text-white">
      {/* Navigation */}
      <nav className="fixed top-0 w-full z-50 bg-slate-900/80 backdrop-blur-md border-b border-slate-700/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-2">
              <Brain className="w-8 h-8 text-primary-500" />
              <span className="text-xl font-bold">ExamLoop</span>
            </div>
            <div className="hidden md:flex items-center gap-8">
              <a href="#features" className="text-slate-300 hover:text-white transition">Features</a>
              <a href="#pricing" className="text-slate-300 hover:text-white transition">Pricing</a>
              <a href="#testimonials" className="text-slate-300 hover:text-white transition">Reviews</a>
            </div>
            <div className="flex items-center gap-4">
              <a href="/login" className="text-slate-300 hover:text-white transition">Log in</a>
              <a 
                href="/signup" 
                className="bg-primary-600 hover:bg-primary-500 px-4 py-2 rounded-lg font-medium transition"
              >
                Start Free
              </a>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-32 pb-20 px-4">
        <div className="max-w-7xl mx-auto text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            {/* Badge */}
            <div className="inline-flex items-center gap-2 bg-primary-500/10 border border-primary-500/20 rounded-full px-4 py-1.5 mb-8">
              <Sparkles className="w-4 h-4 text-primary-400" />
              <span className="text-sm text-primary-300">AI-Powered Learning System</span>
            </div>

            {/* Headline */}
            <h1 className="text-5xl md:text-7xl font-bold mb-6 leading-tight">
              Stop Forgetting<br />
              <span className="gradient-text">What You Learn</span>
            </h1>

            {/* Subheadline */}
            <p className="text-xl md:text-2xl text-slate-400 max-w-3xl mx-auto mb-8">
              The smart revision system that combines spaced repetition + adaptive difficulty.
              <br className="hidden md:block" />
              <span className="text-white font-semibold">84% retention rate</span> on Spring certifications.
            </p>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-12">
              <a 
                href="/signup"
                className="group flex items-center gap-2 bg-primary-600 hover:bg-primary-500 px-8 py-4 rounded-xl font-semibold text-lg transition-all hover:scale-105"
              >
                Start Free Trial
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition" />
              </a>
              <a 
                href="#demo"
                className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 px-8 py-4 rounded-xl font-semibold text-lg transition"
              >
                <Play className="w-5 h-5" />
                Watch Demo
              </a>
            </div>

            {/* Social Proof */}
            <div className="flex flex-col sm:flex-row items-center justify-center gap-6 text-slate-400">
              <div className="flex items-center gap-1">
                {[1,2,3,4,5].map(i => (
                  <Star key={i} className="w-5 h-5 fill-yellow-500 text-yellow-500" />
                ))}
                <span className="ml-2">4.9/5 rating</span>
              </div>
              <div className="hidden sm:block w-px h-6 bg-slate-600" />
              <span>1,200+ developers using ExamLoop</span>
            </div>
          </motion.div>

          {/* Hero Image/Demo */}
          <motion.div
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="mt-16 relative"
          >
            <div className="bg-gradient-to-r from-primary-500/20 to-accent-500/20 rounded-2xl p-1">
              <div className="bg-slate-800 rounded-xl overflow-hidden shadow-2xl">
                <div className="bg-slate-900 px-4 py-3 flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-red-500" />
                  <div className="w-3 h-3 rounded-full bg-yellow-500" />
                  <div className="w-3 h-3 rounded-full bg-green-500" />
                  <span className="ml-4 text-slate-500 text-sm">examloop.com/app</span>
                </div>
                <div className="p-8">
                  {/* App Preview */}
                  <div className="grid md:grid-cols-3 gap-6">
                    {/* Stats Card */}
                    <div className="bg-slate-700/50 rounded-xl p-6">
                      <h3 className="text-slate-400 text-sm mb-2">Today's Progress</h3>
                      <div className="text-4xl font-bold mb-4">12/20</div>
                      <div className="h-2 bg-slate-600 rounded-full overflow-hidden">
                        <div className="h-full bg-primary-500 w-3/5" />
                      </div>
                    </div>
                    {/* Streak Card */}
                    <div className="bg-slate-700/50 rounded-xl p-6">
                      <h3 className="text-slate-400 text-sm mb-2">Current Streak</h3>
                      <div className="text-4xl font-bold text-orange-500 mb-2">🔥 14</div>
                      <p className="text-slate-400 text-sm">days in a row</p>
                    </div>
                    {/* Mastery Card */}
                    <div className="bg-slate-700/50 rounded-xl p-6">
                      <h3 className="text-slate-400 text-sm mb-2">Mastered</h3>
                      <div className="text-4xl font-bold text-green-500 mb-2">47</div>
                      <p className="text-slate-400 text-sm">questions</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Problem Section */}
      <section className="py-20 px-4 bg-slate-800/50">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl md:text-4xl font-bold mb-6">
            You don't have a learning problem.<br />
            <span className="text-slate-400">You have a retention problem.</span>
          </h2>
          <p className="text-xl text-slate-400 mb-12">
            Studies show we forget 70% of what we learn within 24 hours.
            That's why you keep re-reading the same documentation.
          </p>
          
          <div className="grid md:grid-cols-3 gap-8">
            {[
              { icon: Clock, title: "Wasted Time", desc: "Hours spent re-learning the same concepts" },
              { icon: BookOpen, title: "Notes Chaos", desc: "Documentation you'll never open again" },
              { icon: Target, title: "Failed Exams", desc: "Blank mind when it matters most" },
            ].map((item, i) => (
              <div key={i} className="bg-slate-700/30 rounded-xl p-6 border border-slate-600/50">
                <item.icon className="w-10 h-10 text-red-400 mx-auto mb-4" />
                <h3 className="font-semibold mb-2">{item.title}</h3>
                <p className="text-slate-400">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">
              The Science of <span className="gradient-text">Never Forgetting</span>
            </h2>
            <p className="text-xl text-slate-400">
              Two proven algorithms working together for maximum retention
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-8">
            {/* Spaced Repetition */}
            <div className="bg-gradient-to-br from-primary-500/10 to-primary-600/5 rounded-2xl p-8 border border-primary-500/20">
              <div className="w-14 h-14 bg-primary-500/20 rounded-xl flex items-center justify-center mb-6">
                <TrendingUp className="w-7 h-7 text-primary-400" />
              </div>
              <h3 className="text-2xl font-bold mb-4">Spaced Repetition</h3>
              <p className="text-slate-400 mb-6">
                Questions appear at the optimal moment—right before you forget them.
                Based on the Ebbinghaus forgetting curve.
              </p>
              <ul className="space-y-3">
                {[
                  "Intervals: 1, 3, 7, 14, 30, 90 days",
                  "Forgetting probability calculated in real-time",
                  "4 mastery levels: Novice → Mastered"
                ].map((item, i) => (
                  <li key={i} className="flex items-center gap-3">
                    <Check className="w-5 h-5 text-primary-400" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Adaptive Difficulty */}
            <div className="bg-gradient-to-br from-accent-500/10 to-accent-600/5 rounded-2xl p-8 border border-accent-500/20">
              <div className="w-14 h-14 bg-accent-500/20 rounded-xl flex items-center justify-center mb-6">
                <Zap className="w-7 h-7 text-accent-400" />
              </div>
              <h3 className="text-2xl font-bold mb-4">Adaptive Difficulty</h3>
              <p className="text-slate-400 mb-6">
                The system adjusts to your level. Score 75%+? Move up.
                Below 40%? We'll ease it down.
              </p>
              <ul className="space-y-3">
                {[
                  "4 difficulty levels: Easy → Very Hard",
                  "Auto-adjusts based on your performance",
                  "Never bored, never overwhelmed"
                ].map((item, i) => (
                  <li key={i} className="flex items-center gap-3">
                    <Check className="w-5 h-5 text-accent-400" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* AI Feature */}
          <div className="mt-8 bg-gradient-to-r from-slate-700/50 to-slate-800/50 rounded-2xl p-8 border border-slate-600/50">
            <div className="flex flex-col md:flex-row items-center gap-8">
              <div className="flex-1">
                <div className="inline-flex items-center gap-2 bg-yellow-500/10 border border-yellow-500/20 rounded-full px-3 py-1 mb-4">
                  <Sparkles className="w-4 h-4 text-yellow-400" />
                  <span className="text-sm text-yellow-300">Premium Feature</span>
                </div>
                <h3 className="text-2xl font-bold mb-4">AI Question Generation</h3>
                <p className="text-slate-400 mb-4">
                  Enter any topic and our AI generates tailored exam questions instantly.
                  Spring Security? Kubernetes? System Design? Done in seconds.
                </p>
                <a href="/signup" className="inline-flex items-center gap-2 text-primary-400 hover:text-primary-300 font-medium">
                  Try it free <ArrowRight className="w-4 h-4" />
                </a>
              </div>
              <div className="flex-1 bg-slate-800 rounded-xl p-6">
                <div className="text-sm text-slate-500 mb-2">Generate exam for:</div>
                <div className="bg-slate-700 rounded-lg px-4 py-3 mb-4">
                  <span className="text-white">Spring Security OAuth2</span>
                  <span className="text-slate-400 animate-pulse">|</span>
                </div>
                <button className="w-full bg-primary-600 hover:bg-primary-500 rounded-lg py-3 font-medium transition">
                  Generate 10 Questions
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20 px-4 bg-slate-800/50">
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">
              Simple, Honest Pricing
            </h2>
            <p className="text-xl text-slate-400">
              Start free. Upgrade when you're ready.
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
            {/* Free */}
            <div className="bg-slate-700/30 rounded-2xl p-8 border border-slate-600/50">
              <h3 className="text-xl font-bold mb-2">Free</h3>
              <p className="text-slate-400 mb-6">Perfect to get started</p>
              <div className="text-4xl font-bold mb-6">$0<span className="text-lg text-slate-400">/mo</span></div>
              <ul className="space-y-4 mb-8">
                {[
                  "20 reviews per day",
                  "Public exams (Spring, etc.)",
                  "Spaced repetition",
                  "Progress tracking",
                ].map((item, i) => (
                  <li key={i} className="flex items-center gap-3">
                    <Check className="w-5 h-5 text-green-400" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
              <a href="/signup" className="block w-full bg-slate-600 hover:bg-slate-500 text-center py-3 rounded-xl font-medium transition">
                Start Free
              </a>
            </div>

            {/* Premium */}
            <div className="bg-gradient-to-br from-primary-500/20 to-accent-500/20 rounded-2xl p-8 border border-primary-500/30 relative">
              <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-primary-500 text-sm px-3 py-1 rounded-full font-medium">
                Most Popular
              </div>
              <h3 className="text-xl font-bold mb-2">Premium</h3>
              <p className="text-slate-400 mb-6">For serious learners</p>
              <div className="text-4xl font-bold mb-6">$9<span className="text-lg text-slate-400">/mo</span></div>
              <ul className="space-y-4 mb-8">
                {[
                  "Unlimited reviews",
                  "AI question generation",
                  "Create custom exams",
                  "Upload documents (soon)",
                  "Export & analytics",
                  "Priority support",
                ].map((item, i) => (
                  <li key={i} className="flex items-center gap-3">
                    <Check className="w-5 h-5 text-primary-400" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
              <a href="/signup?plan=premium" className="block w-full bg-primary-600 hover:bg-primary-500 text-center py-3 rounded-xl font-medium transition">
                Start 7-Day Free Trial
              </a>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section id="testimonials" className="py-20 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">
              Loved by Developers
            </h2>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                name: "Alex Chen",
                role: "Senior Developer",
                avatar: "👨‍💻",
                text: "Passed my Spring certification with 84% thanks to ExamLoop. The spaced repetition actually works.",
              },
              {
                name: "Marie Dubois",
                role: "DevOps Engineer",
                avatar: "👩‍💻",
                text: "Finally a tool that doesn't let me forget Kubernetes concepts. The AI generation is a game changer.",
              },
              {
                name: "James Wilson",
                role: "Tech Lead",
                avatar: "🧑‍💻",
                text: "We use ExamLoop for our team's technical onboarding. Everyone loves the daily sessions.",
              },
            ].map((t, i) => (
              <div key={i} className="bg-slate-700/30 rounded-xl p-6 border border-slate-600/50">
                <div className="flex items-center gap-1 mb-4">
                  {[1,2,3,4,5].map(i => (
                    <Star key={i} className="w-4 h-4 fill-yellow-500 text-yellow-500" />
                  ))}
                </div>
                <p className="text-slate-300 mb-6">"{t.text}"</p>
                <div className="flex items-center gap-3">
                  <div className="text-3xl">{t.avatar}</div>
                  <div>
                    <div className="font-medium">{t.name}</div>
                    <div className="text-sm text-slate-400">{t.role}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Final CTA */}
      <section className="py-20 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl md:text-5xl font-bold mb-6">
            Ready to Remember<br />
            <span className="gradient-text">Everything You Learn?</span>
          </h2>
          <p className="text-xl text-slate-400 mb-8">
            Join 1,200+ developers who never forget.
          </p>
          <a 
            href="/signup"
            className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 px-8 py-4 rounded-xl font-semibold text-lg transition-all hover:scale-105"
          >
            Start Free — No Credit Card
            <ArrowRight className="w-5 h-5" />
          </a>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-slate-700/50 py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="flex flex-col md:flex-row justify-between items-center gap-6">
            <div className="flex items-center gap-2">
              <Brain className="w-6 h-6 text-primary-500" />
              <span className="font-bold">ExamLoop</span>
            </div>
            <div className="flex items-center gap-6 text-slate-400">
              <a href="/privacy" className="hover:text-white transition">Privacy</a>
              <a href="/terms" className="hover:text-white transition">Terms</a>
              <a href="mailto:hello@examloop.com" className="hover:text-white transition">Contact</a>
            </div>
            <div className="text-slate-500">
              © 2026 ExamLoop. All rights reserved.
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
