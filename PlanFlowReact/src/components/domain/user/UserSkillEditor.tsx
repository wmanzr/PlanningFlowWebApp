import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import Typography from '@mui/material/Typography';
import { useMemo } from 'react';
import { Button, Card, EmptyState, Select, type SelectOption } from '@/components/ui';
import { SkillTier, type SkillId, type SkillResponseDto, type UserSkillsUpdateRequest, } from '@/types';
const TIER_OPTIONS: SelectOption<SkillTier>[] = [
    { value: SkillTier.NOVICE, label: 'Новичок' },
    { value: SkillTier.PRACTITIONER, label: 'Практик' },
    { value: SkillTier.EXPERT, label: 'Эксперт' },
];
export interface SkillRowDraft {
    skillId?: SkillId;
    tier?: SkillTier;
}
export type SkillRowFieldErrors = {
    skill?: string;
    tier?: string;
};
export interface UserSkillEditorProps {
    rows: SkillRowDraft[];
    onRowsChange: (rows: SkillRowDraft[]) => void;
    available: SkillResponseDto[];
    rowErrors?: Record<number, SkillRowFieldErrors>;
    showFooterSave?: boolean;
    submitting?: boolean;
    onSave?: (body: UserSkillsUpdateRequest) => void;
}
function buildSkillOptions(available: SkillResponseDto[]): SelectOption<number>[] {
    return [...available]
        .sort((a, b) => {
        const ca = a.category?.trim() ? a.category.trim() : 'Без категории';
        const cb = b.category?.trim() ? b.category.trim() : 'Без категории';
        const byCat = ca.localeCompare(cb, 'ru');
        if (byCat !== 0)
            return byCat;
        return a.name.localeCompare(b.name, 'ru');
    })
        .map((skill) => {
        const cat = skill.category?.trim() ? skill.category.trim() : 'Без категории';
        return {
            value: skill.id as unknown as number,
            label: `${skill.name} (${cat})`,
        };
    });
}
export const UserSkillEditor = ({ rows, onRowsChange, available, rowErrors, showFooterSave = false, submitting, onSave, }: UserSkillEditorProps) => {
    const skillOptions = useMemo(() => buildSkillOptions(available), [available]);
    const addRow = () => {
        if (available.length === 0)
            return;
        onRowsChange([...rows, {}]);
    };
    const updateRow = (index: number, patch: Partial<SkillRowDraft>) => {
        onRowsChange(rows.map((row, i) => (i === index ? { ...row, ...patch } : row)));
    };
    const clearSkillId = (index: number) => {
        onRowsChange(rows.map((row, i) => {
            if (i !== index)
                return row;
            const { skillId: _omit, ...rest } = row;
            return rest;
        }));
    };
    const clearTier = (index: number) => {
        onRowsChange(rows.map((row, i) => {
            if (i !== index)
                return row;
            const { tier: _omit, ...rest } = row;
            return rest;
        }));
    };
    const removeRow = (index: number) => {
        onRowsChange(rows.filter((_, i) => i !== index));
    };
    const handleSave = () => {
        if (!onSave)
            return;
        const complete = rows.filter((row): row is {
            skillId: SkillId;
            tier: SkillTier;
        } => row.skillId !== undefined && row.tier !== undefined);
        if (complete.length !== rows.length)
            return;
        onSave({
            skillTiers: complete.map((row) => ({ skillId: row.skillId, tier: row.tier })),
        });
    };
    const showRows = rows.length > 0;
    return (<Card>
      <div className="flex items-center justify-between gap-3">
        <Typography variant="h6" component="h3">
          Навыки
        </Typography>
        <Button size="sm" variant="secondary" type="button" onClick={addRow} disabled={available.length === 0}>
          Добавить
        </Button>
      </div>
      <div className="mt-4 flex flex-col gap-3">
        {!showRows ? (<EmptyState title="Навыки не указаны" description="Добавьте свои навыки, чтобы участвовать в подборе на задачи."/>) : (rows.map((row, index) => {
            const err = rowErrors?.[index];
            return (<div key={`skill-draft-${index}`} className="group/skill flex flex-col gap-1">
                <div className="grid items-center gap-2 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto]">
                  <Select<number> label="Навык" options={skillOptions} placeholder="Выберите навык" error={err?.skill} suppressErrorHelperText value={row.skillId === undefined ? '' : (row.skillId as unknown as number)} onChange={(e) => {
                    const raw = e.target.value;
                    if (raw === '') {
                        clearSkillId(index);
                        return;
                    }
                    updateRow(index, { skillId: Number(raw) as unknown as SkillId });
                }}/>
                  <Select<SkillTier> label="Уровень" options={TIER_OPTIONS} placeholder="Выберите уровень" error={err?.tier} suppressErrorHelperText value={row.tier ?? ''} onChange={(e) => {
                    const raw = e.target.value as string;
                    if (raw === '') {
                        clearTier(index);
                        return;
                    }
                    updateRow(index, { tier: raw as SkillTier });
                }}/>
                  <div className="flex items-center justify-center md:justify-end">
                    <Button size="icon" variant="ghost" type="button" title="Удалить строку" aria-label="Удалить строку навыка" className="pointer-events-none shrink-0 opacity-0 transition-all duration-200 ease-out group-hover/skill:pointer-events-auto group-hover/skill:opacity-100 border border-slate-400/80 text-slate-600 hover:border-red-400 hover:bg-red-500/10 hover:text-red-700" onClick={() => removeRow(index)}>
                      <DeleteOutlineOutlinedIcon sx={{ fontSize: 22 }}/>
                    </Button>
                  </div>
                </div>
                {err?.skill || err?.tier ? (<div className="grid gap-1 text-xs text-red-600 md:grid-cols-2 md:gap-2">
                    {err.skill ? <span>{err.skill}</span> : null}
                    {err.tier ? <span>{err.tier}</span> : null}
                  </div>) : null}
              </div>);
        }))}
      </div>
      {showFooterSave && onSave ? (<div className="mt-5 flex justify-end">
          <Button type="button" onClick={handleSave} loading={submitting}>
            Сохранить навыки
          </Button>
        </div>) : null}
    </Card>);
};
