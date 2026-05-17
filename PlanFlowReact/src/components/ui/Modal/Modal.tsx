import { type ReactNode } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
export interface ModalProps {
    open: boolean;
    onClose: () => void;
    title?: ReactNode;
    description?: ReactNode;
    footer?: ReactNode;
    children?: ReactNode;
    size?: 'sm' | 'md' | 'ml' | 'lg';
    closeOnBackdrop?: boolean;
}
const sizeMap: Record<NonNullable<ModalProps['size']>, 'xs' | 'sm' | 'md' | false> = {
    sm: 'xs',
    md: 'sm',
    ml: false,
    lg: 'md',
};
const PAPER_MAX_WIDTH_PX: Partial<Record<NonNullable<ModalProps['size']>, number>> = {
    ml: 720,
};
export const Modal = ({ open, onClose, title, description, footer, children, size = 'md', closeOnBackdrop = true, }: ModalProps) => {
    const paperMaxWidth = PAPER_MAX_WIDTH_PX[size];
    return (<Dialog open={open} onClose={closeOnBackdrop ? onClose : undefined} maxWidth={sizeMap[size]} fullWidth scroll="paper" sx={paperMaxWidth !== undefined
            ? {
                '& .MuiDialog-paper': {
                    maxWidth: paperMaxWidth,
                    width: 'calc(100% - 32px)',
                },
            }
            : undefined} slotProps={{
            backdrop: {
                sx: { backdropFilter: 'blur(2px)' },
            },
        }}>
      {(title ?? description) ? (<DialogTitle sx={{ pb: description ? 0.5 : 2 }}>
          <div className="flex items-start justify-between gap-2">
            <div>
              {title ? (<Typography variant="h6" component="span" sx={{ fontWeight: 600 }}>
                  {title}
                </Typography>) : null}
              {description ? (<Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  {description}
                </Typography>) : null}
            </div>
            <IconButton size="small" onClick={onClose} sx={{ mt: -0.5, mr: -1 }}>
              <CloseIcon fontSize="small"/>
            </IconButton>
          </div>
        </DialogTitle>) : null}
      <DialogContent dividers={!!(title ?? description)} sx={{
            maxHeight: 'min(85vh, 880px)',
            overflowY: 'auto',
        }}>
        {children}
      </DialogContent>
      {footer ? (<DialogActions sx={{ px: 3, py: 2 }}>
          {footer}
        </DialogActions>) : null}
    </Dialog>);
};
