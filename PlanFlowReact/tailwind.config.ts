import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: 'var(--color-bg)',
        headline: 'var(--color-headline)',
        paragraph: 'var(--color-paragraph)',
        button: 'var(--color-button)',
        'button-text': 'var(--color-button-text)',
        stroke: 'var(--color-stroke)',
        main: 'var(--color-main)',
        highlight: 'var(--color-highlight)',
        secondary: 'var(--color-secondary)',
        tertiary: 'var(--color-tertiary)',
        surface: 'var(--color-surface)',
        'surface-muted': 'var(--color-surface-muted)',
        accent: 'var(--color-accent)',
        'accent-soft': 'var(--color-accent-soft)',
        'accent-warm': 'var(--color-accent-warm)',
      },
      borderRadius: {
        lg: '14px',
        md: '10px',
        sm: '6px',
      },
      boxShadow: {
        card: '0 1px 2px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.06)',
      },
      fontFamily: {
        sans: [
          'Inter',
          'system-ui',
          '-apple-system',
          'Segoe UI',
          'Roboto',
          'sans-serif',
        ],
      },
      transitionDuration: {
        fast: '120ms',
        base: '200ms',
      },
      keyframes: {
        'landing-drift': {
          '0%, 100%': { transform: 'translate(0%, 0%) scale(1)' },
          '50%': { transform: 'translate(12%, -10%) scale(1.06)' },
        },
        'landing-drift-slow': {
          '0%, 100%': { transform: 'translate(0%, 0%) scale(1)' },
          '50%': { transform: 'translate(-14%, 8%) scale(1.04)' },
        },
        'landing-pulse-soft': {
          '0%, 100%': { opacity: '0.45' },
          '50%': { opacity: '0.75' },
        },
      },
      animation: {
        'landing-drift': 'landing-drift 22s ease-in-out infinite',
        'landing-drift-slow': 'landing-drift-slow 30s ease-in-out infinite',
        'landing-pulse-soft': 'landing-pulse-soft 8s ease-in-out infinite',
      },
    },
  },
  plugins: [],
};

export default config;
