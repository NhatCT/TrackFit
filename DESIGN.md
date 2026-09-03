---
name: Gutim Health
description: Vietnamese health & fitness tracker with AI-personalized recommendations
colors:
  brand: "#F2612C"
  brand-2: "#FF8A4C"
  brand-soft: "rgba(242,97,44,.12)"
  pink: "#FF2D55"
  green: "#34C759"
  cyan: "#00C7BE"
  blue: "#0A84FF"
  purple: "#AF52DE"
  amber: "#FF9500"
  danger: "#FF3B30"
  bg: "#F2F2F7"
  surface: "#FFFFFF"
  surface-2: "#F7F7FB"
  surface-3: "#EDEDF2"
  ink: "#1C1C1E"
  ink-2: "#3A3A3C"
  muted: "#8E8E93"
  faint: "#AEAEB2"
  hair: "#E5E5EA"
  header-bg: "linear-gradient(180deg,#141B29,#0E1420)"
typography:
  display:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontWeight: 900
    fontSize: "2.6rem"
    lineHeight: 1
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontWeight: 800
    fontSize: "1.25rem"
    lineHeight: 1.2
  body:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontWeight: 400
    fontSize: "1rem"
    lineHeight: 1.5
  label:
    fontFamily: "Inter, system-ui, sans-serif"
    fontWeight: 700
    fontSize: "12px"
    letterSpacing: "0.02em"
rounded:
  sm: "9px"
  md: "14px"
  lg: "20px"
  pill: "999px"
spacing:
  sm: "12px"
  md: "20px"
  lg: "24px"
components:
  button-primary:
    backgroundColor: "{colors.brand}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    padding: "12px 20px"
  button-primary-hover:
    backgroundColor: "{colors.brand-2}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    padding: "12px 20px"
  button-ghost:
    backgroundColor: "{colors.surface-2}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    padding: "12px 20px"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.lg}"
    padding: "{spacing.md}"
  pill-tag:
    backgroundColor: "{colors.surface-2}"
    textColor: "{colors.muted}"
    rounded: "{rounded.pill}"
    padding: "3px 10px"
  icon-tile:
    rounded: "{rounded.sm}"
    size: "30px"
---

# Design System: Gutim Health

## Overview

**Creative North Star: "The Vital Signs Monitor"**

Gutim feels like the health display you'd want on your wrist if Apple Watch were designed by someone who actually understood Vietnamese bodies. Every number earns its pixel. The interface recedes — clean grouped surfaces, barely-there shadows — so that the data itself becomes the visual event. When your BMI is in a healthy range, that number should feel like a reward; when it's not, the visual treatment says "let's work on this" without judgment.

The system commits to two typefaces with clearly different roles: **Nunito** for metrics and display (round, confidently large, zero-compromise on legibility at small sizes) and **Inter** for everything that supports, explains, or guides. Six categorical colors — pink, green, cyan, blue, purple, amber — carry semantic meaning from the iOS health vocabulary users already know. The brand orange (`#F2612C`) appears on primary actions and nowhere else. Its rarity is its power.

The visual mode is **Operate**: users complete tracking tasks, read progress, and take action. Design expression lives in precise details — the exact weight of a Nunito headline, the 20px radius that softens every card, the ambient shadow that makes surfaces feel physically lifted — not in decorative layers. The logged-in dashboard must answer "how am I doing today?" in a single glance.

**Key Characteristics:**
- iOS-grouped background (`#F2F2F7`) with white card surfaces — depth through contrast, not shadow volume
- Nunito 900 for metrics ≥1.25rem; Inter 400/500 for everything else
- Brand orange on ≤10% of any screen; categorical colors as data encoding only
- Minimum 20px radius on cards, 14px on buttons, 999px on pills
- Dark mode: pure black background (`#000000`), same color tokens shifted for contrast
- Always-dark branded header (`linear-gradient(#141B29, #0E1420)`)

## Colors

The palette has one true accent, six categorical data colors, and a complete neutral stack — all sourced from the iOS 17 system palette with Gutim's own brand orange substituted for the system blue.

### Primary
- **Gutim Orange** (`#F2612C` / dark: `#FF6A38`): The brand accent. Used on primary action buttons, active navigation states, and the `--brand-soft` translucent fill for icon tiles. Never use for data encoding — that role belongs to the categorical set.
- **Gutim Orange Hover** (`#FF8A4C`): Brightened 8% for hover and focus states on brand elements.

### Categorical (data colors)
These six colors encode health data categories throughout charts, activity rings, and metric tiles. They follow iOS system color naming and are never used for decorative or structural purposes.
- **Health Pink** (`#FF2D55` / dark: `#FF375F`): Heart rate, active calories, love/care motifs. Activity Ring layer 1.
- **Life Green** (`#34C759` / dark: `#30D158`): Steps, positive trends, healthy status, "ok" semantic.
- **Vitals Cyan** (`#00C7BE` / dark: `#66D4CF`): Hydration, mindfulness, calm metrics. Activity Ring layer 3.
- **Data Blue** (`#0A84FF` / dark: same): Workout minutes, primary chart lines, link color.
- **Focus Purple** (`#AF52DE` / dark: `#BF5AF2`): BMI tile, goals, focus metrics.
- **Energy Amber** (`#FF9500` / dark: `#FF9F0A`): Warnings, moderate-risk indicators, energy metrics.

### Semantic
- **Danger Red** (`#FF3B30` / dark: `#FF453A`): Destructive actions, critical health warnings. Never use casually.

### Neutral
- **Page Ground** (`#F2F2F7`): The grouped page background — all cards float on this.
- **Card Surface** (`#FFFFFF` / dark: `#1C1C1E`): Primary card background.
- **Recessed Surface** (`#F7F7FB` / dark: `#2C2C2E`): Input fields, secondary tiles, inset containers.
- **Separator** (`#E5E5EA` / dark: `#38383A`): Card borders, dividers.
- **Body Text** (`#1C1C1E` / dark: `#FFFFFF`): Primary readable content.
- **Secondary Text** (`#3A3A3C` / dark: `#EBEBF0`): Sub-labels, supporting descriptions.
- **Muted Text** (`#8E8E93` / dark: `#98989F`): Labels on metric tiles, timestamps, helper text.
- **Faint Text** (`#AEAEB2` / dark: `#6C6C70`): Placeholder text, disabled state labels.
- **Branded Header** (`linear-gradient(180deg,#141B29,#0E1420)`): Always dark, both themes. The one surface that never adapts.

### Named Rules
**The One Orange Rule.** The brand accent (`--brand`) appears on ≤10% of any screen. Its rarity is the signal; diluting it across decorative elements destroys it.

**The Data Color Contract.** Categorical colors (pink/green/cyan/blue/purple/amber) are reserved for data encoding only. Never use them for structural UI (borders, backgrounds unrelated to a data metric). A green button is not "positive"; it's confusing.

## Typography

**Display Font:** Nunito (600–900 weight range, Google Fonts)
**Body Font:** Inter (400–700, Google Fonts)

**Character:** Nunito's geometric roundness makes large numerals feel friendly rather than cold — the exact register needed when someone reads their BMI at 6am. Inter's neutral legibility lets it disappear behind content at body scale, keeping the user's eye on the metrics, not the interface.

### Hierarchy
- **Display** (Nunito 900, 2.6rem, line-height 1, letter-spacing −0.02em, tabular-nums): Large health metrics — the central number on a MetricCard, the primary BMI value. Never more than one Display value per card.
- **Headline** (Nunito 800, 1.25rem, line-height 1.2): Card titles like "Chuỗi ngày", section headings, coach name.
- **Title** (Nunito 700 or Inter 600, 1rem–1.1rem): Section sub-headers, modal titles.
- **Body** (Inter 400, 1rem, line-height 1.5): Recommendation descriptions, coach messages, paragraph copy. Max line length 68ch.
- **Label** (Inter 700, 12px, letter-spacing 0.02em, color `--muted`): Metric names above Display numbers, timestamps, chip text. All-caps avoided — sentence case respects Vietnamese diacritics.

### Named Rules
**The Nunito Numbers Rule.** Every standalone numeric metric (BMI, streak count, weight, minutes) uses Nunito with `font-variant-numeric: tabular-nums`. A metric in Inter is a design bug.

**The Case Preservation Rule.** Never uppercase Vietnamese text. Diacritics on uppercase letters are visually broken in most fonts, and the practice signals foreign software to Vietnamese readers.

## Layout

The layout model is **iOS-grouped**: the page background (`--bg`) is a pale grey, white cards float on top, and sections are separated by the grouped ground showing between groups rather than by drawn lines.

- **Container:** Bootstrap `Container` (max-width ~1140px, auto margins). No full-bleed sections in the app UI (logged-in state). Marketing section (logged-out Home) may break to full-bleed.
- **Grid:** Bootstrap 12-column with `Row/Col`. Dashboard cards use `lg={5/4/3}` splits on desktop; columns collapse to `xs={12}` on mobile with `sm={6}` for metric tile pairs.
- **Card Internal Padding:** 20px (`--spacing-md`) uniformly. Never use padding < 16px inside a card.
- **Gap Between Cards:** `g-3` (12px) or `g-4` (16px) Bootstrap gutter classes.
- **Breakpoints:** Bootstrap defaults (xs 0, sm 576, md 768, lg 992, xl 1200). Mobile-first. Dashboard designed to work at ≥360px.
- **Spacing Rhythm:** 4px base unit, scale: 4 / 8 / 12 / 16 / 20 / 24 / 32. Card margins and internal layout respect this rhythm.

## Elevation & Depth

Gutim uses **flat surfaces with ambient lift shadows** — the iOS grouped model. Depth is established primarily through background color contrast (white card on `#F2F2F7` ground) with a supporting feather shadow. Shadows are ambient and architectural, not dramatic or decorative.

### Shadow Vocabulary
- **Card shadow** (`0 1px 2px rgba(0,0,0,.04), 0 8px 24px rgba(0,0,0,.06)`): All `.g-card` and Bootstrap `.card` elements at rest. The double layer — sharp near, diffuse far — mimics iOS card lift.
- **Shadow SM** (`0 1px 3px rgba(0,0,0,.05)`): Smaller interactive elements (chips, input focus, icon tiles).
- **Dark mode shadows** (`0 1px 2px rgba(0,0,0,.4), 0 10px 30px rgba(0,0,0,.5)`): Higher opacity shadows on pure black background — maintains perceived lift even with less luminance contrast.

### Named Rules
**The Flat-by-Default Rule.** No element is elevated without a reason. Hover state may add `translateY(-3px)` to a card's transform to signal interactivity; the resting shadow stays the same. Only `.g-card--tap` applies the hover elevation treatment — use it only on cards the user can click.

## Shapes

The form language is **consistently rounded** — Gutim never uses sharp corners in app UI. Roundness maps to trust and health; the more primary the element, the larger its radius.

- **Cards and primary containers:** 20px radius (`--radius`). This is the dominant shape in the app.
- **Buttons and secondary cards:** 14px radius (`--radius-sm`). Action-adjacent elements are slightly less rounded than containers.
- **Icon tiles:** 9px radius. Small enough not to look like a circle, large enough to feel soft.
- **Pills, tags, chips:** 999px (`--radius-pill`). Fully rounded to signal non-destructive, informational content.
- **Inputs (Bootstrap):** 0.375rem (Bootstrap default ~6px). Inputs are the least rounded element — they sit recessed in forms, smaller radius keeps them legible.
- **No element uses `border-radius: 0`** in app UI (marketing hero section excepts). A right angle would feel foreign in this system.

## Components

### Buttons

**Primary:** Confident orange pill-ish rectangle. The only place brand orange appears as a background.
- **Shape:** 14px radius (`--radius-sm`)
- **Primary:** `background: var(--brand)`, `color: #fff`, padding `12px 20px`, Nunito 800 15px, `box-shadow: 0 6px 18px var(--brand-soft)`
- **Hover / Focus:** `translateY(-2px)`, `filter: brightness(1.03)` — subtle lift, no color change
- **Ghost:** `background: var(--surface-2)`, `color: var(--ink)`, 1px `var(--hair)` border, no shadow. Used for secondary actions adjacent to a primary.

### Cards / Containers

The foundational surface. Everything in the dashboard is a card.
- **Corner Style:** 20px radius — the most generous in the system
- **Background:** `var(--surface)` (white / dark `#1C1C1E`)
- **Shadow:** Card shadow (see Elevation)
- **Border:** 1px `var(--hair)` — the border is structural, always present, never decorative
- **Internal Padding:** 20px

### Metric Tiles (MetricCard)

Gutim's signature component: icon tile + label + Nunito Display number + sub-text + optional trend badge.
- **Layout:** flex column, icon-tile top-left (or inlined), label above number
- **Number:** Nunito 900, Display scale, colored to match categorical role (`var(--purple)` for BMI, `var(--pink)` for streak)
- **Sub-text:** Inter 400 body scale, `color: var(--muted)`
- **Trend badge:** `.g-trend--down` (green) or `.g-trend--up` (amber), pill shape

### Activity Rings (ActivityRings)

Three concentric SVG rings — the most distinctive visual in the dashboard.
- **Stroke width:** 11px per ring, 3px gap between rings
- **Colors per ring:** layer 0 = pink (active calories), layer 1 = green (steps), layer 2 = cyan (minutes)
- **Track (background):** same color at 18% opacity
- **Animation:** `stroke-dashoffset` transition 0.8s ease. Respects `prefers-reduced-motion`.
- **Sizes:** Default 132px SVG. Use `size` prop to adapt per viewport.

### Pills / Tags

Non-interactive labels for categories, statuses, muscle groups.
- **Style:** `background: var(--surface-2)`, `color: var(--muted)`, 1px `var(--hair)` border, 999px radius
- **Size:** Inter 11.5px 600 weight, padding 3px 10px

### Navigation (Header)

Always-dark branded bar. Contrast is fixed — tokens adapt inside; the header does not.
- **Background:** `linear-gradient(180deg, #141B29, #0E1420)` (both themes)
- **Brand link:** logo + "Gutim" wordmark in white
- **Nav items:** Inter 500, white at 90% opacity; active state: full white + `var(--brand)` underline or indicator
- **Mobile:** Bootstrap `Navbar` collapse behind hamburger
- **Badge (notifications):** `var(--danger)` red circle, absolute-positioned on bell icon

### Icon Tiles (.g-ic)

30×30px rounded squares, one per categorical color family. Each color has a soft background (`color-mix(in srgb, var(--X) 15%, transparent)`) and a solid foreground icon in the same token.

## Do's and Don'ts

### Do:
- **Do** use `var(--brand)` (`#F2612C`) exclusively for the primary CTA. One orange per screen.
- **Do** use Nunito 900 for every standalone numeric metric — BMI, weight, streak count, minute totals.
- **Do** maintain `border-radius: 20px` on all card containers. Clip overflow content before using a smaller radius.
- **Do** pair every card with a 1px `var(--hair)` border. The border is structural; it defines the card edge in both light and dark without relying on shadow alone.
- **Do** use `color-mix(in srgb, var(--X) 15%, transparent)` for icon tile backgrounds — never hardcode a translucent hex.
- **Do** test all numeric displays with long values (BMI 99.9, streak 365, weight 120.5) before shipping.
- **Do** keep categorical colors (pink/green/cyan/blue/purple/amber) strictly for data encoding — health metric tiles, chart lines, activity rings only.

### Don't:
- **Don't** use gradient text (`background-clip: text`) in app UI. Solid `var(--brand)` or a categorical color achieves the same emphasis without degrading on Safari or in high-contrast mode.
- **Don't** use `border-radius: 0` on any interactive element in the app shell (logged-in). Sharp corners read as broken or intentionally hostile.
- **Don't** uppercase Vietnamese copy. Diacritics on capitals are visually broken and culturally off.
- **Don't** animate `height`, `width`, `padding`, or `margin`. Use `transform: scaleY()` or `grid-template-rows` for height reveals; `transform: translateX/Y` for position changes.
- **Don't** add shadows beyond the two defined levels (card / shadow-sm). A new shadow role belongs in the token sheet first.
- **Don't** use `Inter` for standalone numeric metrics — that role belongs to Nunito only.
- **Don't** repeat the brand orange as a categorical data color. If you need a seventh data color, use `var(--ink-2)` or extend the categorical set explicitly.
