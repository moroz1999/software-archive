export {fetchAuthor} from './api/author-api';
export {AUTHOR_QUERY_KEY, authorDetailsQuery} from './api/author-queries';
export {AUTHOR_ROUTE, AUTHORS_PATH, authorPath} from './config/paths';
export {
    type AuthorDetails,
    type AuthorGroup,
    type AuthorId,
    type AuthorLabel,
    type AuthorLocation,
    parseAuthorId,
    requireAuthorId,
    toAuthorId,
} from './model/author';
export {useAuthorDetails} from './model/author-details-context';
export {AuthorDetailsProvider, type AuthorDetailsProviderProps} from './model/AuthorDetailsProvider';
