package com.highpalace.dynastycore.zodiac;

public enum Zodiac {
    RAT("鼠", "Rat", "First Among Twelve", "XP orbs are worth 25% more"),
    OX("牛", "Ox", "Tireless Plow", "Haste I while below Y=50"),
    TIGER("虎", "Tiger", "Downhill Fury", "Deal bonus damage when attacking from above"),
    RABBIT("兔", "Rabbit", "Jade Evasion", "10% chance to completely dodge an attack"),
    DRAGON("龙", "Dragon", "Imperial Flame", "Immune to fire and lava damage"),
    SNAKE("蛇", "Snake", "Venom Wisdom", "Potions you drink last 50% longer"),
    HORSE("马", "Horse", "Road of Ten Thousand Li", "Speed II when walking on paths or cobblestone"),
    GOAT("羊", "Goat", "Immovable Stance", "Immune to knockback"),
    MONKEY("猴", "Monkey", "Monkey Steals the Peach", "Chance to not consume food when eating"),
    ROOSTER("鸡", "Rooster", "Punctual Rest", "Phantoms never target you"),
    DOG("狗", "Dog", "Guardian's Warmth", "Nearby allied players within 8 blocks get Regeneration I"),
    PIG("猪", "Pig", "Abundance of the Sty", "Ore blocks have a chance to double drops");

    private final String hanzi;
    private final String displayName;
    private final String perkName;
    private final String perkDescription;

    Zodiac(String hanzi, String displayName, String perkName, String perkDescription) {
        this.hanzi = hanzi;
        this.displayName = displayName;
        this.perkName = perkName;
        this.perkDescription = perkDescription;
    }

    public String getHanzi() {
        return hanzi;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPerkName() {
        return perkName;
    }

    public String getPerkDescription() {
        return perkDescription;
    }

    public String getFullLabel() {
        return hanzi + " " + displayName;
    }

    public static Zodiac fromString(String input) {
        if (input == null) return null;
        String normalized = input.trim().toUpperCase();
        for (Zodiac z : values()) {
            if (z.name().equals(normalized) || z.displayName.equalsIgnoreCase(input) || z.hanzi.equals(input)) {
                return z;
            }
        }
        return null;
    }
}
