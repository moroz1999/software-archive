import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {FormFieldset} from '@/shared/ui/form';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {ItemRow} from '@/shared/ui/item-row';
import {Radio} from '@/shared/ui/radio';
import {Text} from '@/shared/ui/typography';
import type {LabelValue} from '../model/label-value';

export interface LabelsEditorProps {
    readonly value: readonly LabelValue[];
    readonly onChange: (value: readonly LabelValue[]) => void;
    /** What is wrong with each label, by its row. */
    readonly rowErrors?: readonly (string | undefined)[] | undefined;
    /** What is wrong with the labels as a whole. */
    readonly error?: string | undefined;
    readonly disabled?: boolean;
    /** Whether to offer the display name choice for a person. */
    readonly withDisplayName?: boolean;
}

/**
 * Edits the owner's labels and lets a person choose an optional display name (docs/domain/authors.md).
 */
export function LabelsEditor({value, onChange, rowErrors = [], error, disabled = false, withDisplayName = true}: LabelsEditorProps) {
    const {t} = useTranslation();
    const id = useId();

    function update(index: number, change: Partial<LabelValue>) {
        onChange(value.map((label, i) => (i === index ? {...label, ...change} : label)));
    }

    function mark(index: number | null) {
        onChange(value.map((label, i) => ({...label, display: i === index})));
    }

    return (
        <FormFieldset legend={t('label.labels')} error={error}>
            {value.map((label, index) => {
                const n = index + 1;
                const rowError = rowErrors[index];
                const errorId = `${id}-${String(index)}-error`;
                return (
                    <ItemRow
                        key={index}
                        trailing={(
                            <>
                                {withDisplayName && (
                                    <Radio
                                        label={t('label.display')}
                                        name={`${id}-display`}
                                        checked={label.display}
                                        onChoose={() => {
                                            mark(index);
                                        }}
                                        disabled={disabled}
                                    />
                                )}
                                <Button
                                    color="transparent"
                                    size="sm"
                                    square
                                    ariaLabel={t('label.remove', {n})}
                                    onClick={() => {
                                        onChange(value.filter((_, i) => i !== index));
                                    }}
                                    disabled={disabled}
                                >
                                    ×
                                </Button>
                            </>
                        )}
                        error={rowError !== undefined && <Text as="span" id={errorId} variant="caption" tone="danger">{rowError}</Text>}
                    >
                        <Input
                            id={`${id}-${String(index)}`}
                            ariaLabel={t('label.name', {n})}
                            placeholder={t('label.placeholder')}
                            value={label.name}
                            onChange={(name) => {
                                update(index, {name});
                            }}
                            invalid={rowError !== undefined}
                            describedBy={rowError === undefined ? undefined : errorId}
                            disabled={disabled}
                        />
                    </ItemRow>
                );
            })}
            <Inline gap="sm" align="center" wrap>
                <Button
                    color="outlined"
                    size="sm"
                    onClick={() => {
                        onChange([...value, {id: null, name: '', display: false}]);
                    }}
                    disabled={disabled}
                >
                    {t('label.add')}
                </Button>
                {withDisplayName && value.length > 0 && (
                    <Radio
                        label={t('label.noDisplay')}
                        name={`${id}-display`}
                        checked={!value.some((label) => label.display)}
                        onChoose={() => {
                            mark(null);
                        }}
                        disabled={disabled}
                    />
                )}
            </Inline>
        </FormFieldset>
    );
}
