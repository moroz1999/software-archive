package com.archive.backend.category.infra;

import java.util.UUID;

/** Categories of the tree the schema brings, the infra tests work on. */
final class SeededTree {

    static final UUID GAMES = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    static final UUID DEMOSCENE = UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607");
    /** Demoscene → Intro, with 15 subcategories. */
    static final UUID INTRO = UUID.fromString("47301d2f-eb74-5c1c-90c3-090b4f33e498");
    /** Demoscene → Intro → 4K intro, the ninth of Intro. */
    static final UUID INTRO_4K = UUID.fromString("e9d495af-b0d1-5b0f-a22f-6955b423ab8d");

    private SeededTree() {}
}
