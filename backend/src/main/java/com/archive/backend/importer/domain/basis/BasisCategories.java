package com.archive.backend.importer.domain.basis;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * What each category of "Базис" is in the archive (docs/domain/imports.md#caglrcccscalar): a category of the archive's
 * tree, a computer of the Vector family, hardware the program uses or its storage. A name not listed is a mark that
 * is no genre, or a category the site added since, and is passed over.
 */
final class BasisCategories {

    /** The categories the source offers, kept locally so browsing import options never reads the source. */
    static final List<String> SOURCE_CATEGORIES = List.of(
            "3D",
            "adventure",
            "arcade",
            "ay",
            "barkar",
            "basic",
            "basic-dev",
            "beeper",
            "beta",
            "board",
            "boot",
            "boulderdash",
            "breakout",
            "business",
            "card",
            "catalogue",
            "color-music",
            "converter",
            "copy",
            "database",
            "demo",
            "development",
            "disk",
            "document",
            "e-zine",
            "editor",
            "education",
            "electronic",
            "emulator",
            "event",
            "fighting",
            "flight",
            "font",
            "gambling",
            "game",
            "gift",
            "graphics",
            "ham",
            "hardware",
            "hdd",
            "info",
            "intro",
            "isometric",
            "joystick_p",
            "joystick_s",
            "krista-2",
            "kvaz",
            "loader",
            "magazine",
            "maze",
            "missing",
            "monitor",
            "msx",
            "multiplayer",
            "music",
            "occult",
            "os",
            "packer",
            "pascal",
            "pc",
            "phone",
            "pk-6128c",
            "platform",
            "pron",
            "puzzle",
            "quiz",
            "racing",
            "reklama",
            "scientific",
            "shooter",
            "sound",
            "sourcecode",
            "sports",
            "stakan",
            "start-1200",
            "strategy",
            "tape",
            "test",
            "text",
            "utility",
            "vi53-music",
            "virus",
            "z80",
            "zx");

    /** Software for the PC, paper and hardware are not software of the archive. */
    static final Set<String> LEFT_OUT = Set.of("pc", "magazine", "document", "catalogue", "hardware");

    static final String MISSING = "missing";

    /** The computers of the family that take the place of the Vector-06C. */
    static final Map<String, HardwareCode> COMPUTERS = Map.of(
            "krista-2", code("krista2"),
            "pk-6128c", code("pk6128c"),
            "start-1200", code("start1200"));

    /** The hardware a program uses. */
    static final Map<String, HardwareCode> HARDWARE = Map.of(
            "ay", code("ay8910"),
            "beeper", code("beeper"),
            "vi53-music", code("vi53"),
            "joystick_p", code("joystick-p"),
            "joystick_s", code("joystick-s"),
            "hdd", code("hdd"),
            "kvaz", code("kvaz"),
            "barkar", code("barkar"),
            "z80", code("z80"));

    /** The storage a release comes on. */
    static final Map<String, HardwareCode> STORAGE = Map.of("tape", code("tape"), "disk", code("floppy"));

    private static final String GAMES = "081bf3a1-782b-5a8b-8d12-3b155e0a58cb";
    private static final String DEMO = "936b5821-6c89-54c5-93f0-a21ffb1580b3"; // Demoscene > Demo
    private static final String SYSTEM_SOFTWARE = "f0dd01e3-ec45-5b42-ad06-ed564ae0eb7b";
    private static final String SOUND = "32543277-e739-554c-810b-755e96f35da7"; // System Software > Sound
    private static final String BOOT = "087f5590-b140-5dda-9bd7-05d1d85b7723"; // System Software > Boot
    private static final String ELECTRONIC_MAGAZINE = "15c479d7-f10e-5291-8f99-83f320b2a955"; // Press

    /** The categories of the archive's tree; the archive keeps only the deepest of a branch, such as Arcade under Games. */
    static final Map<String, CategoryId> CATEGORIES = Map.ofEntries(
            Map.entry("game", category(GAMES)),
            Map.entry("arcade", category("8536e450-1fc7-5985-9cd2-97b499713a67")), // Games > Action > Arcade
            Map.entry("adventure", category("b5e37aaa-60ea-52fe-a587-c399dab174d5")), // Games > Adventure
            Map.entry("board", category("da35173c-e559-5ba5-8d04-1665f4d39eef")), // Games > Card and Board Games
            Map.entry("card", category("a02ae163-946b-5c7e-9624-6a327a02e7b8")), // > Traditional Card Games
            Map.entry("boulderdash", category("e4fd7717-46ea-5cb3-8267-6bcafca8d37e")), // > Boulder Dash-like
            Map.entry("breakout", category("28e21669-e786-567b-827a-61156b5146cb")), // > Block Breaker
            Map.entry("fighting", category("816b6a8f-315e-5cc9-ae30-d9ee58f787cd")), // > Fighting Games
            Map.entry("flight", category("3773cddc-096a-5072-bd97-c71eb2209f4d")), // > Flight Simulators
            Map.entry("gambling", category("a216cb2f-6486-5b94-8f6b-9aee9a9a477c")), // Games > Gambling
            Map.entry("maze", category("d1c1f13f-04d6-5ad1-82a2-88bccdb824e9")), // Games > Action > Maze
            Map.entry("platform", category("29d0e7c6-fe95-5bd4-9339-d00f3a490c85")), // > Platformers
            Map.entry("puzzle", category("b3570270-ca07-57bf-97d5-f2fbb7a7130a")), // Games > Puzzle
            Map.entry("quiz", category("2350fb58-f656-5832-99f3-e22844c12db5")), // Games > Puzzle > Quiz
            Map.entry("racing", category("e69d46e6-8817-5da4-81e9-c717b9712b1e")), // Games > Action > Racing
            Map.entry("shooter", category("36d8df17-2b5b-518b-ac38-c3df4913be05")), // Games > Action > Shooters
            Map.entry("sports", category("3bf14055-79d0-5963-bbee-d14ad5324628")), // > Sports Simulators
            Map.entry("stakan", category("304d12c4-720a-581a-9557-8bebb22ce111")), // > Falling Block Games
            Map.entry("strategy", category("623ec340-9a17-5999-9331-9a52e9fa6646")), // Games > Strategy
            Map.entry("demo", category(DEMO)),
            Map.entry("color-music", category(DEMO)),
            Map.entry("intro", category("47301d2f-eb74-5c1c-90c3-090b4f33e498")), // Demoscene > Intro
            Map.entry("gift", category("27ad3e77-33c2-543f-9696-63dace221ee4")), // Demoscene > Gift
            Map.entry("reklama", category("4aa926ea-3246-54b1-b9c5-000b610ed078")), // Demoscene > Promo Demo
            Map.entry("e-zine", category(ELECTRONIC_MAGAZINE)),
            Map.entry("info", category("607945dd-9031-543c-b874-b046929f9414")), // Press > Info
            Map.entry("boot", category(BOOT)),
            Map.entry("loader", category(BOOT)),
            Map.entry("copy", category("9683f749-75f1-535f-8c48-e42031fe6374")), // System Software > Copy/Backup
            Map.entry("development", category("86e454bd-24f9-5a44-b750-ddf036b97fb5")), // > Programming
            Map.entry("basic-dev", category("b1a201b7-d846-58e9-9b1b-f3435192a8ff")), // > Programming > BASIC
            Map.entry("pascal", category("ccd66987-944c-5932-a6d8-87d1293f7dfa")), // > Programming languages
            Map.entry("monitor", category("99f97638-6c50-57aa-be30-268308de2e17")), // > Programming > Debuggers
            Map.entry("test", category("821bc680-e7e0-5066-87c2-86bfdada6564")), // > Programming > Tests
            Map.entry("sourcecode", category("771f11fc-4959-5bdf-8e30-0b6c705526cf")), // > Source Code
            Map.entry("emulator", category("82e1d447-21a2-5162-9eac-28cd2aaa835b")), // > Emulator
            Map.entry("font", category("c7a95ebc-bb31-5214-a6da-cd9aad5c5e09")), // > Fonts/UDGs
            Map.entry("graphics", category("16b736cf-b7e9-51cc-9156-07fe321a922d")), // > Graphics
            Map.entry("converter", category("23fe40df-eea7-50e1-9deb-90002f381868")), // > Graphic converters
            Map.entry("os", category("a540e99e-e091-55c9-88db-9dc5420de1bd")), // > Operating systems
            Map.entry("packer", category("03307c44-d370-54bf-8f95-14e4db9f8f23")), // > Data Compression
            Map.entry("sound", category(SOUND)),
            Map.entry("music", category(SOUND)),
            Map.entry("utility", category("b78ed6d4-3fb8-5bad-9c19-401d0ccbd0a5")), // > System Utilities
            Map.entry("editor", category(SYSTEM_SOFTWARE)),
            Map.entry("ham", category("64ad6713-e267-5618-92d3-d80590b75b4e")), // > Telecom/Network
            Map.entry("text", category("e106c954-4886-50c1-b261-4185033b4a1c")), // > Texts > Word Processor
            Map.entry("business", category("10fc1ae9-9172-5153-ba85-381ec0cb4679")), // Applications > Business
            Map.entry("database", category("474c717c-abc9-5a07-aae3-35cbe3a46ff6")), // > Database/Filing
            Map.entry("electronic", category("2a15af4c-1523-55c4-b6d3-7944de98b85a")), // > Electronics
            Map.entry("scientific", category("1a9152f2-3076-50eb-96da-0348344d5892")), // > Maths/Science
            Map.entry("occult", category("c728827c-ce89-50e8-8db0-2b1c3093fb35")), // > Prediction
            Map.entry("phone", category("a336f6b5-94d8-5153-ba74-e1d6d4aa3fd3")), // Applications > Domestic
            Map.entry("education", category("36819685-fbbb-5a71-860b-f08f7e5a957f")), // Educational
            Map.entry("virus", category("e7a41ace-07a5-5cae-9234-34b7dd90ea93"))); // Misc

    private BasisCategories() {}

    private static HardwareCode code(String value) {
        return new HardwareCode(value);
    }

    private static CategoryId category(String id) {
        return new CategoryId(UUID.fromString(id));
    }
}
