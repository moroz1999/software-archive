import {useNavigate} from 'react-router';
import {CATEGORIES_PATH} from '@/entities/categories';
import {ManagementTabs} from '@/features/user/management-navigation';
import {CategoryCreator} from '@/widgets/category-editor';

export function CategoryNewPage() {
    const navigate = useNavigate();
    return (
        <CategoryCreator navigation={<ManagementTabs/>} onCreated={() => {
            void navigate(CATEGORIES_PATH);
        }}/>
    );
}
