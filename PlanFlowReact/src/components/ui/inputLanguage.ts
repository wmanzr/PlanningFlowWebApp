export function withRuInputLang(attrs?: Record<string, unknown>): Record<string, unknown> {
    return { ...(attrs ?? {}), lang: 'ru' };
}
