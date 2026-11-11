import {useNavigate} from 'react-router';
import {CATEGORIES_PATH} from '@/entities/categories';
import {CategoryCreator} from '@/widgets/category-editor';

export function CategoryNewPage() {
    const navigate = useNavigate();
    return (
        <CategoryCreator onCreated={() => {
            void navigate(CATEGORIES_PATH);
        }}/>
    );
}
