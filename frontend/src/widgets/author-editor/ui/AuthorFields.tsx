import {useTranslation} from 'react-i18next';
import {useAuthorForm} from '@/entities/authors';
import {LABEL_NAME_PROBLEM_KEYS, LabelsEditor} from '@/entities/labels';
import {COUNTRY_REQUIRED_KEY, LocationsEditor} from '@/entities/locations';

/** The labels of the author in the form around. */
export function AuthorLabels() {
    const {t} = useTranslation();
    const {values, errors, refusals, changeLabels, busy} = useAuthorForm();
    return (
        <LabelsEditor
            value={values.labels}
            onChange={changeLabels}
            rowErrors={errors.labels?.map((problem) => (problem === null ? undefined : t(LABEL_NAME_PROBLEM_KEYS[problem])))}
            error={refusals.labels}
            disabled={busy}
        />
    );
}

/** The locations of the author in the form around. */
export function AuthorLocations() {
    const {t} = useTranslation();
    const {values, errors, refusals, changeLocations, busy} = useAuthorForm();
    return (
        <LocationsEditor
            value={values.locations}
            onChange={changeLocations}
            rowErrors={errors.locations?.map((problem) => (problem === null ? undefined : t(COUNTRY_REQUIRED_KEY)))}
            error={refusals.locations}
            disabled={busy}
        />
    );
}
