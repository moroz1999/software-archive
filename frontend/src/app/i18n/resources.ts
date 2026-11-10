export const resources = {
    en: {translation: {
        language: {label: 'Language', choose: 'Choose a language'},
        navigation: {mainMenu: 'Main menu', home: 'Home', software: 'Software', authors: 'Authors', groups: 'Groups'},
        actions: {
            addSoftware: 'Add software', editSoftware: 'Edit software', save: 'Save', edit: 'Edit', delete: 'Delete',
            deleteForGood: 'Delete for good', cancel: 'Cancel', close: 'Close', retry: 'Retry', addRelease: 'Add release',
        },
        pages: {recentlyAdded: 'Recently added', software: 'Software', authors: 'Authors', groups: 'Groups'},
        empty: {
            software: 'No software in the archive yet.', authors: 'No authors found.', groups: 'No groups found.',
            releases: 'No releases known yet.', nothingFound: 'Nothing found.', noScreenshot: 'No screenshot yet',
        },
        filters: {
            platform: 'Platform', allPlatforms: 'All platforms', country: 'Country', anyCountry: 'Any country', city: 'City',
            anyCity: 'Any city', firstLetter: 'First letter', all: 'All', filter: 'Filter', filterLabel: 'Filter {{label}}',
        },
        fields: {
            title: 'Title', platform: 'Platform', description: 'Description', releaseDate: 'Release date', version: 'Version',
            type: 'Type', hardware: 'Hardware', choosePlatform: 'Choose a platform', chooseType: 'Choose a type',
            platformLocked: 'Remove the hardware to change the platform.',
        },
        validation: {
            titleRequired: 'Enter a title.', titleTooLong: 'Keep the title within 255 characters.',
            releaseDateInvalid: 'Enter a full date.', platformRequired: 'Choose a platform.',
            versionTooLong: 'Keep the version within 50 characters.', typeRequired: 'Choose a type.',
        },
        errors: {
            system: {
                title: 'Something went wrong', network: 'The server could not be reached. Check your connection and try again.',
                server: 'The server could not complete the request. Please try again.',
                response: 'The server sent a response this page does not understand. Please try again later.',
            },
            notFound: {title: 'Not found', message: 'This page does not exist or has been deleted.', home: 'Go to the home page'},
            page: {message: 'This page could not be shown. Please reload it or try again later.'},
            business: {conflict: 'This item was changed in the meantime. Reload and try again.', refused: 'The request was refused.'},
            hardwareUnknown: 'Choose hardware of the platform of the software.',
            platformLocked: 'Remove the hardware of the software and its releases first.',
        },
        pagination: {label: 'Pages', previous: 'Previous', next: 'Next'},
        loading: {refreshing: 'Refreshing'},
        selection: {remove: 'Remove {{name}}', selected_one: '{{count}} selected', selected_other: '{{count}} selected'},
        author: {
            realName: 'Real name', labels: 'Labels', locations: 'Locations', groups: 'Groups', notes: 'Notes',
            as: 'as {{name}}', labelOf: 'label of {{name}}',
        },
        group: {
            name: 'Name', types: 'Types', labels: 'Labels', locations: 'Locations', members: 'Members', notes: 'Notes',
            memberGroups: 'Member groups', memberOf: 'Member of',
        },
        release: {
            add: 'Add release', addTo: 'Add release to {{title}}', edit: 'Edit release', editOf: 'Edit release of {{title}}',
            delete: 'Delete release', releases: 'Releases', noReleases: 'No releases known yet.', view: 'View', table: 'Table',
            deleteQuestion: 'Delete the release {{name}} from the archive?', cards: 'Cards', allTypes: 'All', year: 'Year',
            versionFact: 'Version {{version}}', editNamed: 'Edit {{title}}',
            hardwareHint: 'A group checked here replaces that group of the software.',
            types: {
                original: 'Original', rerelease: 'Rerelease', adaptation: 'Adaptation', localization: 'Localization', bugfix: 'Bugfix',
                mod: 'Mod', crack: 'Crack', mia: 'Missing in action', corrupted: 'Corrupted', compilation: 'Compilation',
                incomplete: 'Incomplete', demoversion: 'Demo version', unknown: 'Unknown',
            },
        },
        software: {delete: 'Delete software', deleteQuestion: 'Delete “{{title}}” from the archive?', released: 'Released'},
        hardwareGroups: {
            computers: 'Computers', storage: 'Storage', dos: 'DOS', sound: 'Sound', controls: 'Controls', expansion: 'Expansion',
        },
    }},
    ru: {translation: {
        language: {label: 'Язык', choose: 'Выберите язык'},
        navigation: {mainMenu: 'Главное меню', home: 'Главная', software: 'Программы', authors: 'Авторы', groups: 'Группы'},
        actions: {
            addSoftware: 'Добавить программу', editSoftware: 'Редактировать программу', save: 'Сохранить', edit: 'Редактировать',
            delete: 'Удалить', deleteForGood: 'Удалить навсегда', cancel: 'Отмена', close: 'Закрыть', retry: 'Повторить',
            addRelease: 'Добавить релиз',
        },
        pages: {recentlyAdded: 'Недавно добавленное', software: 'Программы', authors: 'Авторы', groups: 'Группы'},
        empty: {
            software: 'В архиве пока нет программ.', authors: 'Авторы не найдены.', groups: 'Группы не найдены.',
            releases: 'Сведения о релизах пока отсутствуют.', nothingFound: 'Ничего не найдено.', noScreenshot: 'Скриншота пока нет',
        },
        filters: {
            platform: 'Платформа', allPlatforms: 'Все платформы', country: 'Страна', anyCountry: 'Любая страна', city: 'Город',
            anyCity: 'Любой город', firstLetter: 'Первая буква', all: 'Все', filter: 'Фильтр', filterLabel: 'Фильтр: {{label}}',
        },
        fields: {
            title: 'Название', platform: 'Платформа', description: 'Описание', releaseDate: 'Дата выпуска', version: 'Версия',
            type: 'Тип', hardware: 'Оборудование', choosePlatform: 'Выберите платформу', chooseType: 'Выберите тип',
            platformLocked: 'Удалите оборудование, чтобы изменить платформу.',
        },
        validation: {
            titleRequired: 'Введите название.', titleTooLong: 'Название должно содержать не более 255 символов.',
            releaseDateInvalid: 'Введите полную дату.', platformRequired: 'Выберите платформу.',
            versionTooLong: 'Версия должна содержать не более 50 символов.', typeRequired: 'Выберите тип.',
        },
        errors: {
            system: {
                title: 'Что-то пошло не так', network: 'Не удалось связаться с сервером. Проверьте соединение и попробуйте ещё раз.',
                server: 'Сервер не смог выполнить запрос. Попробуйте ещё раз.',
                response: 'Сервер вернул непонятный для этой страницы ответ. Попробуйте позже.',
            },
            notFound: {title: 'Не найдено', message: 'Эта страница не существует или была удалена.', home: 'Перейти на главную'},
            page: {message: 'Не удалось показать страницу. Перезагрузите её или попробуйте позже.'},
            business: {conflict: 'Объект был изменён. Перезагрузите страницу и попробуйте ещё раз.', refused: 'Запрос отклонён.'},
            hardwareUnknown: 'Выберите оборудование платформы этой программы.',
            platformLocked: 'Сначала удалите оборудование программы и её релизов.',
        },
        pagination: {label: 'Страницы', previous: 'Назад', next: 'Вперёд'},
        loading: {refreshing: 'Обновление'},
        selection: {
            remove: 'Удалить {{name}}', selected_one: 'Выбран {{count}}', selected_few: 'Выбрано {{count}}',
            selected_many: 'Выбрано {{count}}', selected_other: 'Выбрано {{count}}',
        },
        author: {
            realName: 'Настоящее имя', labels: 'Псевдонимы', locations: 'Места', groups: 'Группы', notes: 'Заметки',
            as: 'как {{name}}', labelOf: 'псевдоним {{name}}',
        },
        group: {
            name: 'Название', types: 'Типы', labels: 'Псевдонимы', locations: 'Места', members: 'Участники', notes: 'Заметки',
            memberGroups: 'Группы-участники', memberOf: 'Входит в группы',
        },
        release: {
            add: 'Добавить релиз', addTo: 'Добавить релиз для {{title}}', edit: 'Редактировать релиз',
            editOf: 'Редактировать релиз {{title}}', delete: 'Удалить релиз', releases: 'Релизы',
            noReleases: 'Сведения о релизах пока отсутствуют.', view: 'Вид', table: 'Таблица', cards: 'Карточки', allTypes: 'Все',
            deleteQuestion: 'Удалить релиз {{name}} из архива?', year: 'Год', versionFact: 'Версия {{version}}',
            editNamed: 'Редактировать {{title}}',
            hardwareHint: 'Выбранная здесь группа заменяет соответствующую группу оборудования программы.',
            types: {
                original: 'Оригинал', rerelease: 'Переиздание', adaptation: 'Адаптация', localization: 'Локализация',
                bugfix: 'Исправление ошибок', mod: 'Модификация', crack: 'Взлом', mia: 'Утрачено', corrupted: 'Повреждено',
                compilation: 'Сборник', incomplete: 'Незавершённое', demoversion: 'Демоверсия', unknown: 'Неизвестно',
            },
        },
        software: {delete: 'Удалить программу', deleteQuestion: 'Удалить «{{title}}» из архива?', released: 'Выпущено'},
        hardwareGroups: {
            computers: 'Компьютеры', storage: 'Носители', dos: 'DOS', sound: 'Звук', controls: 'Управление', expansion: 'Расширения',
        },
    }},
} as const;
