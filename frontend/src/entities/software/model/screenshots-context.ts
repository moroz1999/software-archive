import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {Screenshot} from './screenshot';

/** The screenshots query of software or of a release, for the blocks inside a ScreenshotsProvider. */
export const [ScreenshotsContext, useScreenshots] = createRequiredContext<UseQueryResult<readonly Screenshot[]>>('Screenshots');
