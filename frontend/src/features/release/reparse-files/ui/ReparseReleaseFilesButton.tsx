import {useTranslation} from 'react-i18next';
import type {ReleaseId, SoftwareId} from '@/entities/software';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useReparseReleaseFiles} from '../model/reparse-release-files-context';
import {ReparseReleaseFilesProvider} from '../model/ReparseReleaseFilesProvider';

export interface ReparseReleaseFilesButtonProps {
    readonly softwareId: SoftwareId;
    readonly releaseId: ReleaseId;
}

/** Parses the file of a release again, for whoever may change the release. */
export function ReparseReleaseFilesButton({softwareId, releaseId}: ReparseReleaseFilesButtonProps) {
    return (
        <ReparseReleaseFilesProvider softwareId={softwareId} releaseId={releaseId}>
            <ReparseButton/>
        </ReparseReleaseFilesProvider>
    );
}

function ReparseButton() {
    const {t} = useTranslation();
    const {reparse, reparsing, error} = useReparseReleaseFiles();
    return (
        <Stack gap="xs">
            <Inline>
                <Button color="secondary" onClick={reparse} loading={reparsing}>{t('release.reparseFiles')}</Button>
            </Inline>
            {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
        </Stack>
    );
}
