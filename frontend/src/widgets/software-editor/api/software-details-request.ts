import {fetchSoftware} from '../../../entities/software';
import {viewRequestFamily} from '../../../shared/api';

/** The details of one software, the block at the top of its editing. */
export const softwareDetailsRequest = viewRequestFamily(fetchSoftware);
