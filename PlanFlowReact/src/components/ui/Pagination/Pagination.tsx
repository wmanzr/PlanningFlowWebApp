import MuiPagination from '@mui/material/Pagination';
export interface PaginationProps {
    page: number;
    totalPages: number;
    onChange: (page: number) => void;
    disabled?: boolean;
}
export const Pagination = ({ page, totalPages, onChange, disabled }: PaginationProps) => {
    if (totalPages <= 1)
        return null;
    return (<nav className="flex items-center justify-center" aria-label="Постраничная навигация">
      <MuiPagination count={totalPages} page={page} onChange={(_, newPage) => onChange(newPage)} disabled={disabled} color="primary" shape="rounded" showFirstButton showLastButton/>
    </nav>);
};
