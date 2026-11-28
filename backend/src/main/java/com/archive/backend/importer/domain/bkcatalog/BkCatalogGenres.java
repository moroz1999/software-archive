package com.archive.backend.importer.domain.bkcatalog;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.importer.domain.source.SourceWarning;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The category of the archive's tree each genre of a section of bk-catalog stands for
 * (docs/domain/imports.md#bk-catalog). A game without a genre is a game.
 */
final class BkCatalogGenres {

    private static final Pattern COMMA = Pattern.compile("\\s*,\\s*");

    private static final String GAMES = "081bf3a1-782b-5a8b-8d12-3b155e0a58cb";
    private static final String DEMO = "936b5821-6c89-54c5-93f0-a21ffb1580b3"; // Demoscene > Demo
    private static final String INTRO = "47301d2f-eb74-5c1c-90c3-090b4f33e498"; // Demoscene > Intro
    private static final String SOUND = "32543277-e739-554c-810b-755e96f35da7"; // System Software > Sound
    private static final String ASSEMBLER = "d05ad688-b25b-5a85-8d2f-06a2a5dc9d4e"; // Programming > Assembler/Mcode
    private static final String MUSIC_COLLECTIONS = "850090ce-5bd1-5874-86b1-f673e97a47c7"; // Art pack > Music

    private static final Map<String, CategoryId> OF_GAMES = Map.ofEntries(
            entry("Аркада", "8536e450-1fc7-5985-9cd2-97b499713a67"), // Games > Action > Arcade
            entry("Логическая", "b3570270-ca07-57bf-97d5-f2fbb7a7130a"), // Games > Puzzle
            entry("Лабиринт", "d1c1f13f-04d6-5ad1-82a2-88bccdb824e9"), // Games > Action > Maze
            entry("Леталка-Стрелялка", "2c8bb7d0-4b81-520e-ae28-4b135e36745e"), // > Shooters > Shoot 'em up
            entry("Стрелялка", "36d8df17-2b5b-518b-ac38-c3df4913be05"), // Games > Action > Shooters
            entry("Платформа", "29d0e7c6-fe95-5bd4-9339-d00f3a490c85"), // Games > Action > Platformers
            entry("Драки", "816b6a8f-315e-5cc9-ae30-d9ee58f787cd"), // Games > Action > Fighting Games
            entry("Ралли", "e69d46e6-8817-5da4-81e9-c717b9712b1e"), // Games > Action > Racing
            entry("Азартная", "a216cb2f-6486-5b94-8f6b-9aee9a9a477c"), // Games > Gambling
            entry("Стратегия", "623ec340-9a17-5999-9331-9a52e9fa6646"), // Games > Strategy
            entry("Квест", "b5e37aaa-60ea-52fe-a587-c399dab174d5"), // Games > Adventure
            entry("Point-and-Click", "031316eb-1c4d-5876-8794-1389e3428a91"), // > Point-and-Click Adventures
            entry("Спортивная", "3bf14055-79d0-5963-bbee-d14ad5324628"), // Games > Simulators > Sports
            entry("Обучающая", "3fac0cbb-4498-5cae-8b04-b36e08525725"), // Educational > Educational Game
            entry("Реклама", "4aa926ea-3246-54b1-b9c5-000b610ed078"), // Demoscene > Promo Demo
            entry("Демо", DEMO),
            entry("Разное", "e7a41ace-07a5-5cae-9234-34b7dd90ea93"), // Misc
            entry("Эротическая", "c3d2848f-3803-5b6b-b52d-102ebd3fa715")); // Games > Adult

    private static final Map<String, CategoryId> OF_SOFTWARE = Map.ofEntries(
            entry("Пресса", "15c479d7-f10e-5291-8f99-83f320b2a955"), // Press > Electronic Magazine
            entry("Графическая", "16b736cf-b7e9-51cc-9156-07fe321a922d"), // System Software > Graphics
            entry("Графический редактор", "04f76abf-3bb3-5f68-b61f-4ab0e8e760ed"), // > Graphic editors
            entry("Редактор Спрайтов", "518de908-29b9-5a30-b5f0-a40afa35bcd6"), // > Sprite editors
            entry("Утилита", "b78ed6d4-3fb8-5bad-9c19-401d0ccbd0a5"), // System Software > System Utilities
            entry("Копировщик", "9683f749-75f1-535f-8c48-e42031fe6374"), // System Software > Copy/Backup
            entry("Отладчик", "99f97638-6c50-57aa-be30-268308de2e17"), // Programming > Debuggers
            entry("Язык программирования", "ccd66987-944c-5932-a6d8-87d1293f7dfa"), // > Programming languages
            entry("Ассемблер", ASSEMBLER),
            entry("Дизассемблер", ASSEMBLER),
            entry("Музыкальный редактор", SOUND),
            entry("Синтез речи", "943cbb47-1938-5bfe-beeb-a7fcf9d20031"), // Sound > Sound/Speech utilities
            entry("Музыка", MUSIC_COLLECTIONS),
            entry("Операционная система", "a540e99e-e091-55c9-88db-9dc5420de1bd"), // > Operating systems
            entry("Текстовый редактор", "e106c954-4886-50c1-b261-4185033b4a1c"), // Texts > Word Processor
            entry("Читалка", "d14113cf-eee7-5bea-8949-dbac4f1da892"), // Texts > Text viewers
            entry("База Данных", "474c717c-abc9-5a07-aae3-35cbe3a46ff6")); // Applications > Database/Filing

    private static final Map<String, CategoryId> OF_DEMOSCENE = Map.ofEntries(
            entry("demo", DEMO),
            entry("wild", "64ad90af-cf42-59c0-ad1f-52500c3ec926"), // Demoscene > Demo > Wild
            entry("intro", INTRO),
            entry("32b", "7c70b992-b0d6-5f85-9556-6d9e90235c18"),
            entry("64b", "09b75ce2-e0a0-5d89-9ae5-15c3b8a970b9"),
            entry("128b", "faa51702-e7ea-5b89-beb8-e54153ec41d0"),
            entry("256b", "b7dcf3ef-7e99-5efb-a68b-6c32505dd147"),
            entry("512b", "17b7ec6a-193e-568d-8451-1908870387de"),
            entry("4k", "e9d495af-b0d1-5b0f-a22f-6955b423ab8d"),
            entry("8k", "3a5f9338-ff27-53bc-88e5-342c9143a5b5"),
            entry("16k", "f998de7e-e754-5a30-8cb4-7e00a3127528"),
            entry("64k", INTRO),
            entry("procedural graphics", "055a80fc-d3cc-5925-ad5b-d5b69628a633"), // Demoscene > Procedural graphics
            entry("invitation", "e3295599-c19a-5119-a511-664c89b841f0"), // Demoscene > Invitation
            entry("musicdisk", MUSIC_COLLECTIONS),
            entry("demopack", "72d7e77a-f330-5a5a-923a-d0a62f8fe620"), // Art pack > Demopack
            entry("game", GAMES),
            entry("demotool", SOUND));

    private BkCatalogGenres() {}

    /** The categories the genres stand for; a genre the import does not know is passed over with a warning. */
    static Set<CategoryId> categoriesOf(BkCatalogSection section, String genres, List<SourceWarning> warnings) {
        Set<CategoryId> categories = new LinkedHashSet<>();
        if (genres.isEmpty()) {
            if (section == BkCatalogSection.GAMES) {
                categories.add(category(GAMES));
            }
            return categories;
        }
        Map<String, CategoryId> meanings = switch (section) {
            case GAMES -> OF_GAMES;
            case SOFTWARE -> OF_SOFTWARE;
            case DEMOSCENE -> OF_DEMOSCENE;
        };
        for (String genre : COMMA.splitAsStream(genres).toList()) {
            if (genre.isEmpty()) {
                continue;
            }
            CategoryId category = meanings.get(genre);
            if (category == null) {
                warnings.add(new SourceWarning(SourceWarning.Kind.GENRE, genre));
            } else {
                categories.add(category);
            }
        }
        return categories;
    }

    private static Map.Entry<String, CategoryId> entry(String genre, String id) {
        return Map.entry(genre, category(id));
    }

    private static CategoryId category(String id) {
        return new CategoryId(UUID.fromString(id));
    }
}
