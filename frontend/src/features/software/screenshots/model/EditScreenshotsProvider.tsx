import {type ReactNode, useState} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {
    deleteScreenshot,
    movedScreenshot,
    type ReleaseId,
    reorderScreenshots,
    type ScreenshotFileProblem,
    screenshotFileProblem,
    type ScreenshotId,
    screenshotsQuery,
    SOFTWARE_QUERY_KEY,
    type SoftwareId,
    uploadScreenshots,
} from '@/entities/software';
import {type BusinessError, businessErrorMessage, businessErrorOf} from '@/shared/api';
import {screenshotsMessages} from '../config/messages';
import {EditScreenshotsContext, type EditScreenshotsState} from './edit-screenshots-context';

export interface EditScreenshotsProviderProps {
    readonly softwareId: SoftwareId;
    /** The release whose screenshots these are; none for the software's own. */
    readonly releaseId?: ReleaseId | null;
    /** The block has come into view: only then are the screenshots asked for (docs/data-loading.md). */
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** Adding, deleting and reordering the screenshots of software or of its release. */
export function EditScreenshotsProvider({softwareId, releaseId = null, enabled, children}: EditScreenshotsProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const screenshots = screenshotsQuery(softwareId, releaseId);
    const query = useQuery({...screenshots, enabled});
    // Why the files chosen last cannot be screenshots, found before anything is sent.
    const [problem, setProblem] = useState<ScreenshotFileProblem | null>(null);

    // The screenshots are read anew after every change; the cards of the software lists may show other pictures now.
    const refresh = async () => {
        await queryClient.invalidateQueries({queryKey: screenshots.queryKey, exact: true});
        await queryClient.invalidateQueries({queryKey: SOFTWARE_QUERY_KEY, refetchType: 'none'});
    };
    const upload = useMutation({
        mutationFn: (files: readonly File[]) => uploadScreenshots(softwareId, releaseId, files),
        onSuccess: refresh,
    });
    const reorder = useMutation({
        mutationFn: (order: readonly ScreenshotId[]) => reorderScreenshots(softwareId, releaseId, order),
        onSuccess: refresh,
        onError: async (error) => {
            // Changed meanwhile: shown as they are now, to be moved again.
            if (businessErrorOf(error)?.status === 409) {
                await refresh();
            }
        },
    });
    const remove = useMutation({
        mutationFn: (id: ScreenshotId) => deleteScreenshot(softwareId, releaseId, id),
        onSuccess: refresh,
    });

    // One change at a time: a new one forgets how the last went.
    const start = () => {
        setProblem(null);
        upload.reset();
        reorder.reset();
        remove.reset();
    };
    const message = (error: BusinessError | undefined) => error === undefined ? undefined : businessErrorMessage(error, screenshotsMessages(t));
    const changeError = businessErrorOf(upload.error) ?? businessErrorOf(reorder.error);

    const state: EditScreenshotsState = {
        query,
        upload: (files) => {
            start();
            const found = files.map(screenshotFileProblem).find((one) => one !== null) ?? null;
            if (found === null) {
                upload.mutate(files);
            } else {
                setProblem(found);
            }
        },
        move: (id, by) => {
            const ids = query.data?.map((screenshot) => screenshot.id) ?? [];
            const order = movedScreenshot(ids, id, by);
            if (order !== ids) {
                start();
                reorder.mutate(order);
            }
        },
        remove: (id) => {
            start();
            remove.mutate(id);
        },
        busy: upload.isPending || reorder.isPending || remove.isPending,
        error: problem === null ? message(changeError) : t(`screenshots.errors.${problem}`),
        deleteError: message(businessErrorOf(remove.error)),
        deleted: remove.isSuccess ? remove.variables : undefined,
    };
    return <EditScreenshotsContext value={state}>{children}</EditScreenshotsContext>;
}
