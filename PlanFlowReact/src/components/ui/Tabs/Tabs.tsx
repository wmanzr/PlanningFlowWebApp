import { type ReactNode } from 'react';
import MuiTabs from '@mui/material/Tabs';
import MuiTab from '@mui/material/Tab';
import { useTheme } from '@mui/material/styles';
export interface TabItem<T extends string> {
    value: T;
    label: ReactNode;
    disabled?: boolean;
}
export interface TabsProps<T extends string> {
    value: T;
    items: TabItem<T>[];
    onChange: (value: T) => void;
    className?: string;
}
export const Tabs = <T extends string>({ value, items, onChange, className, }: TabsProps<T>) => {
    const theme = useTheme();
    return (<MuiTabs value={value} onChange={(_, newValue: T) => onChange(newValue)} className={className} variant="scrollable" scrollButtons="auto" sx={{
            minHeight: 36,
            '& .MuiTab-root': {
                minHeight: 36,
                py: 0.75,
                px: 2,
            },
            '& .MuiTabs-indicator': {
                backgroundColor: theme.app.highlight,
            },
        }}>
      {items.map((item) => (<MuiTab key={item.value} value={item.value} label={item.label} disabled={item.disabled}/>))}
    </MuiTabs>);
};
