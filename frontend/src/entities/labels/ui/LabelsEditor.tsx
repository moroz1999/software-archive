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

/** The labels with a display name: the first one when none is. */
function markedFirst(labels: readonly LabelValue[]): readonly LabelValue[] {
    return labels.some((label) => label.display) ? labels : labels.map((label, i) => ({...label, display: i === 0}));
}

export interface LabelsEditorProps {
    readonly value: readonly LabelValue[];
    readonly onChange: (value: readonly LabelValue[]) => void;
    /** What is wrong with each label, by its row. */
    readonly rowErrors?: readonly (string | undefined)[] | undefined;
    /** What is wrong with the labels as a whole. */
    readonly error?: string | undefined;
    readonly disabled?: boolean;
    /** Whether the owner has a display name among the labels: a person has, a group has not. */
    readonly withDisplayName?: boolean;
}

/**
 * The labels of an owner: renamed in place, added, taken away. Of a person exactly one, chosen by its radio, is the display name — the
 * first label added becomes it, and taking it away passes it to the first label left; a group has none. A label taken away is not
 * deleted, it only stops being the owner's (docs/domain/authors.md).
 */
export function LabelsEditor({value, onChange, rowErrors = [], error, disabled = false, withDisplayName = true}: LabelsEditorProps) {
    const {t} = useTranslation();
    const id = useId();

    function update(index: number, change: Partial<LabelValue>) {
        onChange(value.map((label, i) => (i === index ? {...label, ...change} : label)));
    }

    function mark(index: number) {
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
                                        const left = value.filter((_, i) => i !== index);
                                        onChange(withDisplayName ? markedFirst(left) : left);
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
            <Inline gap="sm">
                <Button
                    color="outlined"
                    size="sm"
                    onClick={() => {
                        onChange([...value, {id: null, name: '', display: withDisplayName && value.length === 0}]);
                    }}
                    disabled={disabled}
                >
                    {t('label.add')}
                </Button>
            </Inline>
        </FormFieldset>
    );
}
