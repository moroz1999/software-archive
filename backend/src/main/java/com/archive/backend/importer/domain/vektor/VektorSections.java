package com.archive.backend.importer.domain.vektor;

import com.archive.backend.category.domain.CategoryId;
import java.util.Map;
import java.util.UUID;

/**
 * The sections of vektor-06c.ru, known by the code in their address, and the categories of the archive they go to.
 * The utilities for the PC are not software of the Vector and are left out; a section the archive does not know yet
 * is taken without a category.
 */
public final class VektorSections {

    private static final String PC_UTILITIES = "utility-dlya-pc";
    private static final String MISC = "e7a41ace-07a5-5cae-9234-34b7dd90ea93";
    private static final String SYSTEM_SOFTWARE = "f0dd01e3-ec45-5b42-ad06-ed564ae0eb7b";

    private static final Map<String, String> CATEGORIES = Map.ofEntries(
            Map.entry("3d-programmy", MISC),
            Map.entry("arkadnye-igry", "8536e450-1fc7-5985-9cd2-97b499713a67"), // Games > Action > Arcade
            Map.entry("azartnye-igry", "a216cb2f-6486-5b94-8f6b-9aee9a9a477c"), // Games > Gambling
            Map.entry("bazy-dannykh", "474c717c-abc9-5a07-aae3-35cbe3a46ff6"), // Applications > Database/Filing
            Map.entry("biznes-igry", "623ec340-9a17-5999-9331-9a52e9fa6646"), // Games > Strategy
            Map.entry("demo-programmy", "c35b7f9b-44a8-5570-9406-9e6dcb30b607"), // Demoscene
            Map.entry("elektronnye-izdaniya", "15c479d7-f10e-5291-8f99-83f320b2a955"), // Press > Electronic Magazine
            Map.entry("emulyatory", "82e1d447-21a2-5162-9eac-28cd2aaa835b"), // System Software > Emulator
            Map.entry("igry-na-doskakh", "da35173c-e559-5ba5-8d04-1665f4d39eef"), // Games > Card and Board Games
            Map.entry("igry-s-borboy", "816b6a8f-315e-5cc9-ae30-d9ee58f787cd"), // Games > Action > Fighting Games
            Map.entry("igry-s-labirintami", "d1c1f13f-04d6-5ad1-82a2-88bccdb824e9"), // Games > Action > Maze
            Map.entry("igry-s-pazlami", "b3570270-ca07-57bf-97d5-f2fbb7a7130a"), // Games > Puzzle
            Map.entry("igry-s-polyetami", "36d8df17-2b5b-518b-ac38-c3df4913be05"), // Games > Action > Shooters
            Map.entry("informatsionnye-programmy", "607945dd-9031-543c-b874-b046929f9414"), // Press > Info
            // Games > Card and Board Games > Traditional Card Games
            Map.entry("kartochnye-igry_", "a02ae163-946b-5c7e-9624-6a327a02e7b8"),
            Map.entry("logicheskie-igry", "b8386291-387a-5360-b431-3d75a49b4a88"), // Games > Puzzle > Logic Games
            Map.entry("muzykalnye-programmy", "32543277-e739-554c-810b-755e96f35da7"), // System Software > Sound
            Map.entry("obuchayushchie-programmy-", "36819685-fbbb-5a71-860b-f08f7e5a957f"), // Educational
            Map.entry("operatsionnye-sistemy", "a540e99e-e091-55c9-88db-9dc5420de1bd"), // System Software > OS
            Map.entry("priklyuchencheskie-igry", "b5e37aaa-60ea-52fe-a587-c399dab174d5"), // Games > Adventure
            Map.entry("programmy-na-beysike", MISC),
            Map.entry("programmy-po-elektronike", "2a15af4c-1523-55c4-b6d3-7944de98b85a"), // Applications > Electronics
            Map.entry("redaktory", SYSTEM_SOFTWARE),
            Map.entry("reklamnye-programmy", "4aa926ea-3246-54b1-b9c5-000b610ed078"), // Demoscene > Promo Demo
            Map.entry("sborniki-na-disketakh", "bcbdf8ed-03cd-55df-9c62-6fd99d2f2a04"), // Compilation
            Map.entry("sportivnye-igry", "3bf14055-79d0-5963-bbee-d14ad5324628"), // Games > Simulators > Sports
            Map.entry("sredstva-razrabotki", "86e454bd-24f9-5a44-b750-ddf036b97fb5"), // System Software > Programming
            Map.entry("telekommunikatsii", "64ad6713-e267-5618-92d3-d80590b75b4e"), // System Software > Telecom
            Map.entry("utility", "b78ed6d4-3fb8-5bad-9c19-401d0ccbd0a5"), // System Software > System Utilities
            Map.entry("yazyk-programmirovaniya-basic", "b1a201b7-d846-58e9-9b1b-f3435192a8ff"), // Programming > BASIC
            // System Software > Programming > Programming languages
            Map.entry("yazyk-programmirovaniya-pascal", "ccd66987-944c-5932-a6d8-87d1293f7dfa"),
            Map.entry("zagruzchiki", "087f5590-b140-5dda-9bd7-05d1d85b7723")); // System Software > Boot

    private VektorSections() {}

    public static VektorPlacement placementOf(String section) {
        if (section.equals(PC_UTILITIES)) {
            return new VektorPlacement(false, null);
        }
        String category = CATEGORIES.get(section);
        return new VektorPlacement(true, category == null ? null : new CategoryId(UUID.fromString(category)));
    }
}
