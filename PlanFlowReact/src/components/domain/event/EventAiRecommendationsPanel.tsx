import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import type { Components } from 'react-markdown';
import Typography from '@mui/material/Typography';
import { Button, Card, ErrorMessage, Spinner } from '@/components/ui';
import type { AppApiError, AsyncStatus, EventPostMortemAiReportResponseDto } from '@/types';
export type EventAiRecommendationsPanelProps = {
    fetchStatus: AsyncStatus;
    fetchError: AppApiError | null;
    report: EventPostMortemAiReportResponseDto | null;
};
const markdownComponents: Components = {
    h1: ({ children }) => (<h1 className="mt-4 first:mt-0 mb-2 text-xl font-bold tracking-tight text-headline">{children}</h1>),
    h2: ({ children }) => (<h2 className="mt-4 first:mt-0 mb-2 text-lg font-semibold text-headline border-b border-secondary/50 pb-1">
      {children}
    </h2>),
    h3: ({ children }) => (<h3 className="mt-3 first:mt-0 mb-1.5 text-base font-semibold text-headline">{children}</h3>),
    h4: ({ children }) => (<h4 className="mt-2 first:mt-0 mb-1 text-sm font-semibold uppercase tracking-wide text-headline">{children}</h4>),
    p: ({ children }) => <p className="mb-2 last:mb-0 leading-relaxed text-paragraph">{children}</p>,
    ul: ({ children }) => <ul className="mb-2 list-disc space-y-1 pl-5 text-paragraph">{children}</ul>,
    ol: ({ children }) => <ol className="mb-2 list-decimal space-y-1 pl-5 text-paragraph">{children}</ol>,
    li: ({ children }) => <li className="leading-relaxed">{children}</li>,
    strong: ({ children }) => <strong className="font-semibold text-headline">{children}</strong>,
    em: ({ children }) => <em className="italic text-paragraph">{children}</em>,
    hr: () => <hr className="my-4 border-0 border-t border-secondary/40"/>,
    blockquote: ({ children }) => (<blockquote className="my-2 border-l-4 border-primary/35 bg-surface-muted/50 py-1 pl-3 text-paragraph">
      {children}
    </blockquote>),
    a: ({ href, children }) => (<a href={href} className="font-medium text-primary underline underline-offset-2 hover:opacity-90" target="_blank" rel="noopener noreferrer">
      {children}
    </a>),
    pre: ({ children }) => (<pre className="my-2 max-w-full overflow-x-auto rounded-md border border-secondary/40 bg-surface-muted p-3 text-xs leading-normal text-headline">
      {children}
    </pre>),
    code: ({ className, children, ...props }) => {
        const isBlock = Boolean(className);
        if (isBlock) {
            return (<code className={`font-mono text-[0.8125rem] ${className ?? ''}`} {...props}>
          {children}
        </code>);
        }
        return (<code className="rounded bg-surface-muted px-1 py-0.5 font-mono text-[0.85em] text-headline" {...props}>
        {children}
      </code>);
    },
};
export function EventAiRecommendationsPanel({ fetchStatus, fetchError, report, }: EventAiRecommendationsPanelProps) {
    const [expanded, setExpanded] = useState(false);
    const isPending = fetchStatus === 'pending' ||
        (fetchStatus === 'succeeded' && report?.status === 'PENDING');
    const isFailed = report?.status === 'FAILED';
    const isDone = report?.status === 'COMPLETED';
    const bodyMaxHeight = expanded ? 'max-h-[28rem]' : 'max-h-[12rem]';
    const reportBody = report?.reportText?.trim() ? report.reportText : '';
    return (<Card padded={false} className="flex w-full min-w-0 flex-col overflow-hidden">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-secondary/60 px-5 py-4">
        <div>
          <h2 className="text-lg font-semibold text-headline">Рекомендации от ИИ</h2>
          <p className="text-sm text-paragraph">Анализ завершенного мероприятия для следующего планирования</p>
        </div>
        {isDone ? (<Button size="sm" variant="secondary" type="button" onClick={() => setExpanded((v) => !v)}>
            {expanded ? 'Свернуть' : 'Развернуть'}
          </Button>) : null}
      </div>
      <div className="px-5 py-4">
        {fetchError ? (<ErrorMessage message={fetchError.message}/>) : null}
        {isPending ? (<div className="flex min-h-[140px] flex-col items-center justify-center gap-3 overflow-hidden">
            <Spinner size="lg"/>
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', maxWidth: '36rem' }}>
              Скоро здесь будут рекомендации от ИИ…
            </Typography>
          </div>) : null}
        {isFailed ? (<div className={`min-h-[12rem] overflow-y-auto rounded-md border border-secondary/50 bg-surface-muted/60 p-4 ${bodyMaxHeight}`}>
            <Typography variant="caption" color="error" sx={{ fontWeight: 600, display: 'block', mb: 1 }}>
              Не удалось сформировать отчет
            </Typography>
            <Typography variant="body2" className="whitespace-pre-wrap break-words text-paragraph">
              {report?.errorMessage ?? 'Неизвестная ошибка'}
            </Typography>
          </div>) : null}
        {isDone ? (<div className={`min-h-[12rem] overflow-y-auto rounded-md border border-secondary/40 bg-surface p-4 text-[0.9375rem] ${bodyMaxHeight}`}>
            {reportBody ? (<div className="markdown-ai-report min-w-0 break-words">
                <ReactMarkdown components={markdownComponents}>{reportBody}</ReactMarkdown>
              </div>) : (<p className="text-paragraph">Отчет получен, но текст пустой.</p>)}
          </div>) : null}
      </div>
    </Card>);
}
