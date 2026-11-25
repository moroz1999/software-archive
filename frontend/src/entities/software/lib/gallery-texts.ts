import type {Gallery} from '../model/screenshot';

/** The texts every gallery has in words of its own. */
export type GalleryText = 'heading' | 'numbered' | 'nOfCount' | 'none' | 'add' | 'moveLeft' | 'moveRight' | 'delete' | 'deleteTitle' | 'deleteQuestion';

/** The key of a text of a gallery: the screenshots keep theirs, the other galleries have theirs under galleries. */
export function galleryKey(gallery: Gallery, text: GalleryText) {
    return gallery === 'screenshots' ? `screenshots.${text}` as const : `galleries.${gallery}.${text}` as const;
}
