-- E2E fixture data set for geo: 20 countries with 20 cities each. Stable ids: tests may reference them.
-- Country n has the id ...0002-{n}; city k of country n has the id ...0002-{n * 100 + k}.
INSERT INTO country (id, name, code)
VALUES ('00000000-0000-0000-0002-000000000001', 'United Kingdom', 'GB'),
       ('00000000-0000-0000-0002-000000000002', 'Russia', 'RU'),
       ('00000000-0000-0000-0002-000000000003', 'Ukraine', 'UA'),
       ('00000000-0000-0000-0002-000000000004', 'Poland', 'PL'),
       ('00000000-0000-0000-0002-000000000005', 'Czech Republic', 'CZ'),
       ('00000000-0000-0000-0002-000000000006', 'Finland', 'FI'),
       ('00000000-0000-0000-0002-000000000007', 'Sweden', 'SE'),
       ('00000000-0000-0000-0002-000000000008', 'Norway', 'NO'),
       ('00000000-0000-0000-0002-000000000009', 'Denmark', 'DK'),
       ('00000000-0000-0000-0002-000000000010', 'Germany', 'DE'),
       ('00000000-0000-0000-0002-000000000011', 'Netherlands', 'NL'),
       ('00000000-0000-0000-0002-000000000012', 'Hungary', 'HU'),
       ('00000000-0000-0000-0002-000000000013', 'Slovakia', 'SK'),
       ('00000000-0000-0000-0002-000000000014', 'Spain', 'ES'),
       ('00000000-0000-0000-0002-000000000015', 'Italy', 'IT'),
       ('00000000-0000-0000-0002-000000000016', 'Belarus', 'BY'),
       ('00000000-0000-0000-0002-000000000017', 'Lithuania', 'LT'),
       ('00000000-0000-0000-0002-000000000018', 'Latvia', 'LV'),
       ('00000000-0000-0000-0002-000000000019', 'Estonia', 'EE'),
       ('00000000-0000-0000-0002-000000000020', 'Bulgaria', 'BG')
ON CONFLICT (id) DO UPDATE
    SET name = EXCLUDED.name,
        code = EXCLUDED.code;

INSERT INTO city (id, name, country_id)
SELECT format('00000000-0000-0000-0002-%s', lpad((c.n * 100 + city.k)::text, 12, '0'))::uuid,
       city.name,
       format('00000000-0000-0000-0002-%s', lpad(c.n::text, 12, '0'))::uuid
FROM (VALUES
    (1, ARRAY['London', 'Manchester', 'Birmingham', 'Liverpool', 'Leeds', 'Glasgow', 'Edinburgh', 'Bristol', 'Sheffield', 'Cardiff', 'Belfast', 'Newcastle', 'Nottingham', 'Leicester', 'Southampton', 'Brighton', 'Oxford', 'Cambridge', 'York', 'Aberdeen']),
    (2, ARRAY['Moscow', 'Saint Petersburg', 'Novosibirsk', 'Yekaterinburg', 'Kazan', 'Nizhny Novgorod', 'Chelyabinsk', 'Samara', 'Omsk', 'Rostov-on-Don', 'Ufa', 'Krasnoyarsk', 'Voronezh', 'Perm', 'Volgograd', 'Krasnodar', 'Saratov', 'Tyumen', 'Tolyatti', 'Izhevsk']),
    (3, ARRAY['Kyiv', 'Kharkiv', 'Odesa', 'Dnipro', 'Donetsk', 'Zaporizhzhia', 'Lviv', 'Kryvyi Rih', 'Mykolaiv', 'Mariupol', 'Luhansk', 'Vinnytsia', 'Kherson', 'Poltava', 'Chernihiv', 'Cherkasy', 'Sumy', 'Zhytomyr', 'Khmelnytskyi', 'Rivne']),
    (4, ARRAY['Warsaw', 'Kraków', 'Łódź', 'Wrocław', 'Poznań', 'Gdańsk', 'Szczecin', 'Bydgoszcz', 'Lublin', 'Białystok', 'Katowice', 'Gdynia', 'Częstochowa', 'Radom', 'Toruń', 'Sosnowiec', 'Rzeszów', 'Kielce', 'Gliwice', 'Olsztyn']),
    (5, ARRAY['Prague', 'Brno', 'Ostrava', 'Plzeň', 'Liberec', 'Olomouc', 'České Budějovice', 'Hradec Králové', 'Ústí nad Labem', 'Pardubice', 'Zlín', 'Havířov', 'Kladno', 'Most', 'Opava', 'Frýdek-Místek', 'Karviná', 'Jihlava', 'Teplice', 'Děčín']),
    (6, ARRAY['Helsinki', 'Espoo', 'Tampere', 'Vantaa', 'Oulu', 'Turku', 'Jyväskylä', 'Lahti', 'Kuopio', 'Pori', 'Kouvola', 'Joensuu', 'Lappeenranta', 'Hämeenlinna', 'Vaasa', 'Seinäjoki', 'Rovaniemi', 'Mikkeli', 'Kotka', 'Salo']),
    (7, ARRAY['Stockholm', 'Gothenburg', 'Malmö', 'Uppsala', 'Västerås', 'Örebro', 'Linköping', 'Helsingborg', 'Jönköping', 'Norrköping', 'Lund', 'Umeå', 'Gävle', 'Borås', 'Södertälje', 'Eskilstuna', 'Halmstad', 'Växjö', 'Karlstad', 'Sundsvall']),
    (8, ARRAY['Oslo', 'Bergen', 'Trondheim', 'Stavanger', 'Drammen', 'Fredrikstad', 'Kristiansand', 'Sandnes', 'Tromsø', 'Sarpsborg', 'Skien', 'Ålesund', 'Sandefjord', 'Haugesund', 'Tønsberg', 'Moss', 'Porsgrunn', 'Bodø', 'Arendal', 'Hamar']),
    (9, ARRAY['Copenhagen', 'Aarhus', 'Odense', 'Aalborg', 'Esbjerg', 'Randers', 'Kolding', 'Horsens', 'Vejle', 'Roskilde', 'Herning', 'Hørsholm', 'Silkeborg', 'Næstved', 'Fredericia', 'Viborg', 'Køge', 'Holstebro', 'Taastrup', 'Slagelse']),
    (10, ARRAY['Berlin', 'Hamburg', 'Munich', 'Cologne', 'Frankfurt', 'Stuttgart', 'Düsseldorf', 'Leipzig', 'Dortmund', 'Essen', 'Bremen', 'Dresden', 'Hanover', 'Nuremberg', 'Duisburg', 'Bochum', 'Wuppertal', 'Bielefeld', 'Bonn', 'Münster']),
    (11, ARRAY['Amsterdam', 'Rotterdam', 'The Hague', 'Utrecht', 'Eindhoven', 'Groningen', 'Tilburg', 'Almere', 'Breda', 'Nijmegen', 'Apeldoorn', 'Haarlem', 'Arnhem', 'Enschede', 'Amersfoort', 'Zaanstad', 'Haarlemmermeer', '''s-Hertogenbosch', 'Zwolle', 'Leiden']),
    (12, ARRAY['Budapest', 'Debrecen', 'Szeged', 'Miskolc', 'Pécs', 'Győr', 'Nyíregyháza', 'Kecskemét', 'Székesfehérvár', 'Szombathely', 'Szolnok', 'Tatabánya', 'Kaposvár', 'Érd', 'Veszprém', 'Békéscsaba', 'Zalaegerszeg', 'Sopron', 'Eger', 'Nagykanizsa']),
    (13, ARRAY['Bratislava', 'Košice', 'Prešov', 'Žilina', 'Nitra', 'Banská Bystrica', 'Trnava', 'Trenčín', 'Martin', 'Poprad', 'Prievidza', 'Zvolen', 'Považská Bystrica', 'Michalovce', 'Nové Zámky', 'Spišská Nová Ves', 'Komárno', 'Levice', 'Humenné', 'Bardejov']),
    (14, ARRAY['Madrid', 'Barcelona', 'Valencia', 'Seville', 'Zaragoza', 'Málaga', 'Murcia', 'Palma', 'Las Palmas', 'Bilbao', 'Alicante', 'Córdoba', 'Valladolid', 'Vigo', 'Gijón', 'Granada', 'A Coruña', 'Vitoria-Gasteiz', 'Elche', 'Oviedo']),
    (15, ARRAY['Rome', 'Milan', 'Naples', 'Turin', 'Palermo', 'Genoa', 'Bologna', 'Florence', 'Bari', 'Catania', 'Venice', 'Verona', 'Messina', 'Padua', 'Trieste', 'Brescia', 'Parma', 'Taranto', 'Prato', 'Modena']),
    (16, ARRAY['Minsk', 'Gomel', 'Mogilev', 'Vitebsk', 'Grodno', 'Brest', 'Babruysk', 'Baranovichi', 'Borisov', 'Pinsk', 'Orsha', 'Mozyr', 'Soligorsk', 'Novopolotsk', 'Lida', 'Molodechno', 'Polotsk', 'Zhlobin', 'Svetlogorsk', 'Rechitsa']),
    (17, ARRAY['Vilnius', 'Kaunas', 'Klaipėda', 'Šiauliai', 'Panevėžys', 'Alytus', 'Marijampolė', 'Mažeikiai', 'Jonava', 'Utena', 'Kėdainiai', 'Telšiai', 'Tauragė', 'Ukmergė', 'Visaginas', 'Plungė', 'Kretinga', 'Palanga', 'Radviliškis', 'Druskininkai']),
    (18, ARRAY['Riga', 'Daugavpils', 'Liepāja', 'Jelgava', 'Jūrmala', 'Ventspils', 'Rēzekne', 'Valmiera', 'Jēkabpils', 'Ogre', 'Tukums', 'Salaspils', 'Cēsis', 'Kuldīga', 'Olaine', 'Saldus', 'Talsi', 'Dobele', 'Sigulda', 'Bauska']),
    (19, ARRAY['Tallinn', 'Tartu', 'Narva', 'Pärnu', 'Kohtla-Järve', 'Viljandi', 'Maardu', 'Rakvere', 'Kuressaare', 'Sillamäe', 'Valga', 'Võru', 'Jõhvi', 'Haapsalu', 'Keila', 'Paide', 'Saue', 'Elva', 'Tapa', 'Põlva']),
    (20, ARRAY['Sofia', 'Plovdiv', 'Varna', 'Burgas', 'Ruse', 'Stara Zagora', 'Pleven', 'Sliven', 'Dobrich', 'Shumen', 'Pernik', 'Haskovo', 'Yambol', 'Pazardzhik', 'Blagoevgrad', 'Veliko Tarnovo', 'Vratsa', 'Gabrovo', 'Asenovgrad', 'Vidin'])
) AS c(n, cities)
CROSS JOIN LATERAL unnest(c.cities) WITH ORDINALITY AS city(name, k)
ON CONFLICT (id) DO UPDATE
    SET name       = EXCLUDED.name,
        country_id = EXCLUDED.country_id;
