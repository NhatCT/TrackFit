---
target: trackfitweb/src/components/Home.js
date: 2026-09-03
method: dual-agent
total_score: 21
max_score: 40
p0_count: 1
p1_count: 2
p2_count: 2
---

# Critique: Home Dashboard

Method: dual-agent (A: design-review · B: detector-scan)

## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 2 | No skeleton/spinner during fetch |
| 2 | Match System / Real World | 3 | Vietnamese BMI thresholds, day labels correct |
| 3 | User Control and Freedom | 2 | No undo; hard redirect on reco click |
| 4 | Consistency and Standards | 3 | Tokens coherent; exercise grid uses different shadow |
| 5 | Error Prevention | 1 | BMI sub-text always shows healthy range regardless of classification |
| 6 | Recognition Rather Than Recall | 3 | Sections labeled; play icon ambiguous |
| 7 | Flexibility and Efficiency | 1 | No keyboard nav, no customizable chips |
| 8 | Aesthetic and Minimalist | 3 | Token constraints clean; marketing grid is noise |
| 9 | Error Recovery | 1 | Fetch errors silent; empty states have no CTA |
| 10 | Help and Documentation | 2 | Coach is contextual help; no onboarding |
| **Total** | | **21/40** | **Acceptable** |

## Design Specificity Verdict

Dashboard is authored for Vietnamese fitness users — Asian BMI thresholds, full Vietnamese date formatting, streak copy, Coach chips are genuinely specific and cannot be mistaken for a generic template. "Bai tap noi bat" exercise grid is the one marketing-template element that erodes specificity.

Detector: 2 real findings, 8 false positives (vendor files in src/).
- Real: `gradient-text` CompleteProfile.js:93; `overused-font` Inter in tokens.css (context-dependent, intentional pairing)

## Priority Issues

**P0: Cold-start empty state — three simultaneous failure signals**
No data user sees: rings 0%, BMI "—", streak "0 ngày · Tân binh". No skeleton, no onboarding nudge.
Fix: Onboarding card when `bmiData === null && streak === 0`. Ghost ring strokes.
Command: `/impeccable onboard Home`

**P1: Marketing exercise grid in Operate dashboard**
Generic image grid appears directly below personal metrics. Dilutes personalization.
Fix: Gate behind "Khám phá bài tập →" text link. Dashboard closes with Recommendations.
Command: `/impeccable distill Home`

**P1: No loading state on critical data fetches**
3–4s fetch on VN 4G: rings + metric cards look broken (empty = failed state for user).
Fix: `isLoading` state → skeleton MetricCard + muted ActivityRings with shimmer.
Command: `/impeccable harden Home`

**P2: BMI sub-text wrong per user classification**
Always renders "· khỏe mạnh 18.5–24.9" unconditionally. Obese user reads contradictory label.
Fix: Conditional copy per classification (ưu tiên đốt mỡ / về dưới 23 / tăng cơ).
Command: `/impeccable clarify Home`

**P2: TodayWorkout exposes 4-step workflow in dashboard card**
Plan selector + exercise list + log buttons + active workout all visible at rest.
Fix: Summary tile + single CTA "Bắt đầu tập"; full flow on /workout or modal.
Command: `/impeccable distill TodayWorkout`
