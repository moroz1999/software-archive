-- E2E fixture data set for geo: 20 countries with 20 cities each, named in English and in Russian. Stable ids: tests may
-- reference them.
-- Country n has the id ...0002-{n}; city k of country n has the id ...0002-{n * 100 + k}.
INSERT INTO country (id, code)
VALUES ('00000000-0000-0000-0002-000000000001', 'GB'),
       ('00000000-0000-0000-0002-000000000002', 'RU'),
       ('00000000-0000-0000-0002-000000000003', 'UA'),
       ('00000000-0000-0000-0002-000000000004', 'PL'),
       ('00000000-0000-0000-0002-000000000005', 'CZ'),
       ('00000000-0000-0000-0002-000000000006', 'FI'),
       ('00000000-0000-0000-0002-000000000007', 'SE'),
       ('00000000-0000-0000-0002-000000000008', 'NO'),
       ('00000000-0000-0000-0002-000000000009', 'DK'),
       ('00000000-0000-0000-0002-000000000010', 'DE'),
       ('00000000-0000-0000-0002-000000000011', 'NL'),
       ('00000000-0000-0000-0002-000000000012', 'HU'),
       ('00000000-0000-0000-0002-000000000013', 'SK'),
       ('00000000-0000-0000-0002-000000000014', 'ES'),
       ('00000000-0000-0000-0002-000000000015', 'IT'),
       ('00000000-0000-0000-0002-000000000016', 'BY'),
       ('00000000-0000-0000-0002-000000000017', 'LT'),
       ('00000000-0000-0000-0002-000000000018', 'LV'),
       ('00000000-0000-0000-0002-000000000019', 'EE'),
       ('00000000-0000-0000-0002-000000000020', 'BG')
ON CONFLICT (id) DO UPDATE
    SET code = EXCLUDED.code;

INSERT INTO country_name (country_id, language, name)
SELECT c.id::uuid, l.language, CASE l.language WHEN 'en' THEN c.en ELSE c.ru END
FROM (VALUES ('00000000-0000-0000-0002-000000000001', 'United Kingdom', 'Великобритания'),
       ('00000000-0000-0000-0002-000000000002', 'Russia', 'Россия'),
       ('00000000-0000-0000-0002-000000000003', 'Ukraine', 'Украина'),
       ('00000000-0000-0000-0002-000000000004', 'Poland', 'Польша'),
       ('00000000-0000-0000-0002-000000000005', 'Czech Republic', 'Чехия'),
       ('00000000-0000-0000-0002-000000000006', 'Finland', 'Финляндия'),
       ('00000000-0000-0000-0002-000000000007', 'Sweden', 'Швеция'),
       ('00000000-0000-0000-0002-000000000008', 'Norway', 'Норвегия'),
       ('00000000-0000-0000-0002-000000000009', 'Denmark', 'Дания'),
       ('00000000-0000-0000-0002-000000000010', 'Germany', 'Германия'),
       ('00000000-0000-0000-0002-000000000011', 'Netherlands', 'Нидерланды'),
       ('00000000-0000-0000-0002-000000000012', 'Hungary', 'Венгрия'),
       ('00000000-0000-0000-0002-000000000013', 'Slovakia', 'Словакия'),
       ('00000000-0000-0000-0002-000000000014', 'Spain', 'Испания'),
       ('00000000-0000-0000-0002-000000000015', 'Italy', 'Италия'),
       ('00000000-0000-0000-0002-000000000016', 'Belarus', 'Беларусь'),
       ('00000000-0000-0000-0002-000000000017', 'Lithuania', 'Литва'),
       ('00000000-0000-0000-0002-000000000018', 'Latvia', 'Латвия'),
       ('00000000-0000-0000-0002-000000000019', 'Estonia', 'Эстония'),
       ('00000000-0000-0000-0002-000000000020', 'Bulgaria', 'Болгария')) AS c(id, en, ru)
CROSS JOIN (VALUES ('en'), ('ru')) AS l(language)
ON CONFLICT (country_id, language) DO UPDATE
    SET name = EXCLUDED.name;

CREATE TEMPORARY TABLE fixture_city ON COMMIT DROP AS
SELECT format('00000000-0000-0000-0002-%s', lpad((c.n * 100 + city.k)::text, 12, '0'))::uuid AS id,
       format('00000000-0000-0000-0002-%s', lpad(c.n::text, 12, '0'))::uuid AS country_id,
       city.en,
       c.ru[city.k] AS ru
FROM (VALUES
    (1, ARRAY['London', 'Manchester', 'Birmingham', 'Liverpool', 'Leeds', 'Glasgow', 'Edinburgh', 'Bristol', 'Sheffield', 'Cardiff', 'Belfast', 'Newcastle', 'Nottingham', 'Leicester', 'Southampton', 'Brighton', 'Oxford', 'Cambridge', 'York', 'Aberdeen'],
        ARRAY['Лондон', 'Манчестер', 'Бирмингем', 'Ливерпуль', 'Лидс', 'Глазго', 'Эдинбург', 'Бристоль', 'Шеффилд', 'Кардифф', 'Белфаст', 'Ньюкасл', 'Ноттингем', 'Лестер', 'Саутгемптон', 'Брайтон', 'Оксфорд', 'Кембридж', 'Йорк', 'Абердин']),
    (2, ARRAY['Moscow', 'Saint Petersburg', 'Novosibirsk', 'Yekaterinburg', 'Kazan', 'Nizhny Novgorod', 'Chelyabinsk', 'Samara', 'Omsk', 'Rostov-on-Don', 'Ufa', 'Krasnoyarsk', 'Voronezh', 'Perm', 'Volgograd', 'Krasnodar', 'Saratov', 'Tyumen', 'Tolyatti', 'Izhevsk'],
        ARRAY['Москва', 'Санкт-Петербург', 'Новосибирск', 'Екатеринбург', 'Казань', 'Нижний Новгород', 'Челябинск', 'Самара', 'Омск', 'Ростов-на-Дону', 'Уфа', 'Красноярск', 'Воронеж', 'Пермь', 'Волгоград', 'Краснодар', 'Саратов', 'Тюмень', 'Тольятти', 'Ижевск']),
    (3, ARRAY['Kyiv', 'Kharkiv', 'Odesa', 'Dnipro', 'Donetsk', 'Zaporizhzhia', 'Lviv', 'Kryvyi Rih', 'Mykolaiv', 'Mariupol', 'Luhansk', 'Vinnytsia', 'Kherson', 'Poltava', 'Chernihiv', 'Cherkasy', 'Sumy', 'Zhytomyr', 'Khmelnytskyi', 'Rivne'],
        ARRAY['Киев', 'Харьков', 'Одесса', 'Днепр', 'Донецк', 'Запорожье', 'Львов', 'Кривой Рог', 'Николаев', 'Мариуполь', 'Луганск', 'Винница', 'Херсон', 'Полтава', 'Чернигов', 'Черкассы', 'Сумы', 'Житомир', 'Хмельницкий', 'Ровно']),
    (4, ARRAY['Warsaw', 'Kraków', 'Łódź', 'Wrocław', 'Poznań', 'Gdańsk', 'Szczecin', 'Bydgoszcz', 'Lublin', 'Białystok', 'Katowice', 'Gdynia', 'Częstochowa', 'Radom', 'Toruń', 'Sosnowiec', 'Rzeszów', 'Kielce', 'Gliwice', 'Olsztyn'],
        ARRAY['Варшава', 'Краков', 'Лодзь', 'Вроцлав', 'Познань', 'Гданьск', 'Щецин', 'Быдгощ', 'Люблин', 'Белосток', 'Катовице', 'Гдыня', 'Ченстохова', 'Радом', 'Торунь', 'Сосновец', 'Жешув', 'Кельце', 'Гливице', 'Ольштын']),
    (5, ARRAY['Prague', 'Brno', 'Ostrava', 'Plzeň', 'Liberec', 'Olomouc', 'České Budějovice', 'Hradec Králové', 'Ústí nad Labem', 'Pardubice', 'Zlín', 'Havířov', 'Kladno', 'Most', 'Opava', 'Frýdek-Místek', 'Karviná', 'Jihlava', 'Teplice', 'Děčín'],
        ARRAY['Прага', 'Брно', 'Острава', 'Пльзень', 'Либерец', 'Оломоуц', 'Ческе-Будеёвице', 'Градец-Кралове', 'Усти-над-Лабем', 'Пардубице', 'Злин', 'Гавиржов', 'Кладно', 'Мост', 'Опава', 'Фридек-Мистек', 'Карвина', 'Йиглава', 'Теплице', 'Дечин']),
    (6, ARRAY['Helsinki', 'Espoo', 'Tampere', 'Vantaa', 'Oulu', 'Turku', 'Jyväskylä', 'Lahti', 'Kuopio', 'Pori', 'Kouvola', 'Joensuu', 'Lappeenranta', 'Hämeenlinna', 'Vaasa', 'Seinäjoki', 'Rovaniemi', 'Mikkeli', 'Kotka', 'Salo'],
        ARRAY['Хельсинки', 'Эспоо', 'Тампере', 'Вантаа', 'Оулу', 'Турку', 'Ювяскюля', 'Лахти', 'Куопио', 'Пори', 'Коувола', 'Йоэнсуу', 'Лаппеэнранта', 'Хямеэнлинна', 'Вааса', 'Сейняйоки', 'Рованиеми', 'Миккели', 'Котка', 'Сало']),
    (7, ARRAY['Stockholm', 'Gothenburg', 'Malmö', 'Uppsala', 'Västerås', 'Örebro', 'Linköping', 'Helsingborg', 'Jönköping', 'Norrköping', 'Lund', 'Umeå', 'Gävle', 'Borås', 'Södertälje', 'Eskilstuna', 'Halmstad', 'Växjö', 'Karlstad', 'Sundsvall'],
        ARRAY['Стокгольм', 'Гётеборг', 'Мальмё', 'Уппсала', 'Вестерос', 'Эребру', 'Линчёпинг', 'Хельсингборг', 'Йёнчёпинг', 'Норрчёпинг', 'Лунд', 'Умео', 'Евле', 'Бурос', 'Сёдертелье', 'Эскильстуна', 'Хальмстад', 'Векшё', 'Карлстад', 'Сундсвалль']),
    (8, ARRAY['Oslo', 'Bergen', 'Trondheim', 'Stavanger', 'Drammen', 'Fredrikstad', 'Kristiansand', 'Sandnes', 'Tromsø', 'Sarpsborg', 'Skien', 'Ålesund', 'Sandefjord', 'Haugesund', 'Tønsberg', 'Moss', 'Porsgrunn', 'Bodø', 'Arendal', 'Hamar'],
        ARRAY['Осло', 'Берген', 'Тронхейм', 'Ставангер', 'Драммен', 'Фредрикстад', 'Кристиансанн', 'Саннес', 'Тромсё', 'Сарпсборг', 'Шиен', 'Олесунн', 'Саннефьорд', 'Хаугесунн', 'Тёнсберг', 'Мосс', 'Порсгрунн', 'Будё', 'Арендал', 'Хамар']),
    (9, ARRAY['Copenhagen', 'Aarhus', 'Odense', 'Aalborg', 'Esbjerg', 'Randers', 'Kolding', 'Horsens', 'Vejle', 'Roskilde', 'Herning', 'Hørsholm', 'Silkeborg', 'Næstved', 'Fredericia', 'Viborg', 'Køge', 'Holstebro', 'Taastrup', 'Slagelse'],
        ARRAY['Копенгаген', 'Орхус', 'Оденсе', 'Ольборг', 'Эсбьерг', 'Раннерс', 'Кольдинг', 'Хорсенс', 'Вайле', 'Роскилле', 'Хернинг', 'Хёрсхольм', 'Силькеборг', 'Нествед', 'Фредерисия', 'Виборг', 'Кёге', 'Хольстебро', 'Тоструп', 'Слагельсе']),
    (10, ARRAY['Berlin', 'Hamburg', 'Munich', 'Cologne', 'Frankfurt', 'Stuttgart', 'Düsseldorf', 'Leipzig', 'Dortmund', 'Essen', 'Bremen', 'Dresden', 'Hanover', 'Nuremberg', 'Duisburg', 'Bochum', 'Wuppertal', 'Bielefeld', 'Bonn', 'Münster'],
        ARRAY['Берлин', 'Гамбург', 'Мюнхен', 'Кёльн', 'Франкфурт', 'Штутгарт', 'Дюссельдорф', 'Лейпциг', 'Дортмунд', 'Эссен', 'Бремен', 'Дрезден', 'Ганновер', 'Нюрнберг', 'Дуйсбург', 'Бохум', 'Вупперталь', 'Билефельд', 'Бонн', 'Мюнстер']),
    (11, ARRAY['Amsterdam', 'Rotterdam', 'The Hague', 'Utrecht', 'Eindhoven', 'Groningen', 'Tilburg', 'Almere', 'Breda', 'Nijmegen', 'Apeldoorn', 'Haarlem', 'Arnhem', 'Enschede', 'Amersfoort', 'Zaanstad', 'Haarlemmermeer', '''s-Hertogenbosch', 'Zwolle', 'Leiden'],
        ARRAY['Амстердам', 'Роттердам', 'Гаага', 'Утрехт', 'Эйндховен', 'Гронинген', 'Тилбург', 'Алмере', 'Бреда', 'Неймеген', 'Апелдорн', 'Харлем', 'Арнем', 'Энсхеде', 'Амерсфорт', 'Занстад', 'Харлеммермер', 'Хертогенбос', 'Зволле', 'Лейден']),
    (12, ARRAY['Budapest', 'Debrecen', 'Szeged', 'Miskolc', 'Pécs', 'Győr', 'Nyíregyháza', 'Kecskemét', 'Székesfehérvár', 'Szombathely', 'Szolnok', 'Tatabánya', 'Kaposvár', 'Érd', 'Veszprém', 'Békéscsaba', 'Zalaegerszeg', 'Sopron', 'Eger', 'Nagykanizsa'],
        ARRAY['Будапешт', 'Дебрецен', 'Сегед', 'Мишкольц', 'Печ', 'Дьёр', 'Ньиредьхаза', 'Кечкемет', 'Секешфехервар', 'Сомбатхей', 'Сольнок', 'Татабанья', 'Капошвар', 'Эрд', 'Веспрем', 'Бекешчаба', 'Залаэгерсег', 'Шопрон', 'Эгер', 'Надьканижа']),
    (13, ARRAY['Bratislava', 'Košice', 'Prešov', 'Žilina', 'Nitra', 'Banská Bystrica', 'Trnava', 'Trenčín', 'Martin', 'Poprad', 'Prievidza', 'Zvolen', 'Považská Bystrica', 'Michalovce', 'Nové Zámky', 'Spišská Nová Ves', 'Komárno', 'Levice', 'Humenné', 'Bardejov'],
        ARRAY['Братислава', 'Кошице', 'Прешов', 'Жилина', 'Нитра', 'Банска-Бистрица', 'Трнава', 'Тренчин', 'Мартин', 'Попрад', 'Превидза', 'Зволен', 'Поважска-Бистрица', 'Михаловце', 'Нове-Замки', 'Спишска-Нова-Вес', 'Комарно', 'Левице', 'Гуменне', 'Бардеёв']),
    (14, ARRAY['Madrid', 'Barcelona', 'Valencia', 'Seville', 'Zaragoza', 'Málaga', 'Murcia', 'Palma', 'Las Palmas', 'Bilbao', 'Alicante', 'Córdoba', 'Valladolid', 'Vigo', 'Gijón', 'Granada', 'A Coruña', 'Vitoria-Gasteiz', 'Elche', 'Oviedo'],
        ARRAY['Мадрид', 'Барселона', 'Валенсия', 'Севилья', 'Сарагоса', 'Малага', 'Мурсия', 'Пальма', 'Лас-Пальмас', 'Бильбао', 'Аликанте', 'Кордова', 'Вальядолид', 'Виго', 'Хихон', 'Гранада', 'Ла-Корунья', 'Витория-Гастейс', 'Эльче', 'Овьедо']),
    (15, ARRAY['Rome', 'Milan', 'Naples', 'Turin', 'Palermo', 'Genoa', 'Bologna', 'Florence', 'Bari', 'Catania', 'Venice', 'Verona', 'Messina', 'Padua', 'Trieste', 'Brescia', 'Parma', 'Taranto', 'Prato', 'Modena'],
        ARRAY['Рим', 'Милан', 'Неаполь', 'Турин', 'Палермо', 'Генуя', 'Болонья', 'Флоренция', 'Бари', 'Катания', 'Венеция', 'Верона', 'Мессина', 'Падуя', 'Триест', 'Брешиа', 'Парма', 'Таранто', 'Прато', 'Модена']),
    (16, ARRAY['Minsk', 'Gomel', 'Mogilev', 'Vitebsk', 'Grodno', 'Brest', 'Babruysk', 'Baranovichi', 'Borisov', 'Pinsk', 'Orsha', 'Mozyr', 'Soligorsk', 'Novopolotsk', 'Lida', 'Molodechno', 'Polotsk', 'Zhlobin', 'Svetlogorsk', 'Rechitsa'],
        ARRAY['Минск', 'Гомель', 'Могилёв', 'Витебск', 'Гродно', 'Брест', 'Бобруйск', 'Барановичи', 'Борисов', 'Пинск', 'Орша', 'Мозырь', 'Солигорск', 'Новополоцк', 'Лида', 'Молодечно', 'Полоцк', 'Жлобин', 'Светлогорск', 'Речица']),
    (17, ARRAY['Vilnius', 'Kaunas', 'Klaipėda', 'Šiauliai', 'Panevėžys', 'Alytus', 'Marijampolė', 'Mažeikiai', 'Jonava', 'Utena', 'Kėdainiai', 'Telšiai', 'Tauragė', 'Ukmergė', 'Visaginas', 'Plungė', 'Kretinga', 'Palanga', 'Radviliškis', 'Druskininkai'],
        ARRAY['Вильнюс', 'Каунас', 'Клайпеда', 'Шяуляй', 'Паневежис', 'Алитус', 'Мариямполе', 'Мажейкяй', 'Йонава', 'Утена', 'Кедайняй', 'Тельшяй', 'Таураге', 'Укмерге', 'Висагинас', 'Плунге', 'Кретинга', 'Паланга', 'Радвилишкис', 'Друскининкай']),
    (18, ARRAY['Riga', 'Daugavpils', 'Liepāja', 'Jelgava', 'Jūrmala', 'Ventspils', 'Rēzekne', 'Valmiera', 'Jēkabpils', 'Ogre', 'Tukums', 'Salaspils', 'Cēsis', 'Kuldīga', 'Olaine', 'Saldus', 'Talsi', 'Dobele', 'Sigulda', 'Bauska'],
        ARRAY['Рига', 'Даугавпилс', 'Лиепая', 'Елгава', 'Юрмала', 'Вентспилс', 'Резекне', 'Валмиера', 'Екабпилс', 'Огре', 'Тукумс', 'Саласпилс', 'Цесис', 'Кулдига', 'Олайне', 'Салдус', 'Талси', 'Добеле', 'Сигулда', 'Бауска']),
    (19, ARRAY['Tallinn', 'Tartu', 'Narva', 'Pärnu', 'Kohtla-Järve', 'Viljandi', 'Maardu', 'Rakvere', 'Kuressaare', 'Sillamäe', 'Valga', 'Võru', 'Jõhvi', 'Haapsalu', 'Keila', 'Paide', 'Saue', 'Elva', 'Tapa', 'Põlva'],
        ARRAY['Таллин', 'Тарту', 'Нарва', 'Пярну', 'Кохтла-Ярве', 'Вильянди', 'Маарду', 'Раквере', 'Курессааре', 'Силламяэ', 'Валга', 'Выру', 'Йыхви', 'Хаапсалу', 'Кейла', 'Пайде', 'Сауэ', 'Эльва', 'Тапа', 'Пылва']),
    (20, ARRAY['Sofia', 'Plovdiv', 'Varna', 'Burgas', 'Ruse', 'Stara Zagora', 'Pleven', 'Sliven', 'Dobrich', 'Shumen', 'Pernik', 'Haskovo', 'Yambol', 'Pazardzhik', 'Blagoevgrad', 'Veliko Tarnovo', 'Vratsa', 'Gabrovo', 'Asenovgrad', 'Vidin'],
        ARRAY['София', 'Пловдив', 'Варна', 'Бургас', 'Русе', 'Стара-Загора', 'Плевен', 'Сливен', 'Добрич', 'Шумен', 'Перник', 'Хасково', 'Ямбол', 'Пазарджик', 'Благоевград', 'Велико-Тырново', 'Враца', 'Габрово', 'Асеновград', 'Видин'])
) AS c(n, en, ru)
CROSS JOIN LATERAL unnest(c.en) WITH ORDINALITY AS city(en, k);

INSERT INTO city (id, country_id)
SELECT id, country_id FROM fixture_city
ON CONFLICT (id) DO UPDATE
    SET country_id = EXCLUDED.country_id;

INSERT INTO city_name (city_id, language, name)
SELECT c.id, l.language, CASE l.language WHEN 'en' THEN c.en ELSE c.ru END
FROM fixture_city c
CROSS JOIN (VALUES ('en'), ('ru')) AS l(language)
ON CONFLICT (city_id, language) DO UPDATE
    SET name = EXCLUDED.name;
