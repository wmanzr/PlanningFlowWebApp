import { Component, type ErrorInfo, type ReactNode } from 'react';
import MuiButton from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
export interface ErrorBoundaryProps {
    children: ReactNode;
    fallback?: (reset: () => void, error: Error) => ReactNode;
    onError?: (error: Error, info: ErrorInfo) => void;
}
interface ErrorBoundaryState {
    error: Error | null;
}
const DefaultFallback = ({ reset, error, }: {
    reset: () => void;
    error: Error;
}) => (<Box sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        p: 4,
        textAlign: 'center',
        height: '100%',
    }}>
    <Typography variant="h5" sx={{ fontWeight: 600 }} color="text.primary">
      Что-то пошло не так
    </Typography>
    <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 400 }}>
      {error.message}
    </Typography>
    <MuiButton variant="contained" onClick={reset}>
      Попробовать снова
    </MuiButton>
  </Box>);
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
    state: ErrorBoundaryState = { error: null };
    static getDerivedStateFromError(error: Error): ErrorBoundaryState {
        return { error };
    }
    componentDidCatch(error: Error, info: ErrorInfo): void {
        this.props.onError?.(error, info);
    }
    reset = (): void => {
        this.setState({ error: null });
    };
    render(): ReactNode {
        if (this.state.error) {
            if (this.props.fallback)
                return this.props.fallback(this.reset, this.state.error);
            return <DefaultFallback reset={this.reset} error={this.state.error}/>;
        }
        return this.props.children;
    }
}
