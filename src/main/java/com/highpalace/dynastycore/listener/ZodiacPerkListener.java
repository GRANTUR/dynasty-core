package com.highpalace.dynastycore.listener;

import com.highpalace.dynastycore.zodiac.Zodiac;
import com.highpalace.dynastycore.zodiac.ZodiacProfile;
import com.highpalace.dynastycore.zodiac.ProfileManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import org.bukkit.NamespacedKey;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ZodiacPerkListener implements Listener {
    private final JavaPlugin plugin;
    private final ProfileManager profileManager;
    private final Random random = new Random();

    // Config values
    private final double ratXpBonus;
    private final double tigerBonusDamage;
    private final double rabbitDodgeChance;
    private final double snakePotionMultiplier;
    private final int horseSpeedAmplifier;
    private final double monkeyFoodSaveChance;
    private final int dogRegenRadius;
    private final double pigOreDoubleChance;

    // Track players with active horse speed buff
    private final Set<UUID> horseSpeedActive = new HashSet<>();

    // Ore blocks that Pig perk applies to
    private static final Set<Material> ORE_BLOCKS = Set.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.ANCIENT_DEBRIS
    );

    // Blocks that give Horse speed buff
    private static final Set<Material> ROAD_BLOCKS = Set.of(
            Material.DIRT_PATH,
            Material.COBBLESTONE, Material.COBBLESTONE_SLAB, Material.COBBLESTONE_STAIRS,
            Material.STONE_BRICKS, Material.STONE_BRICK_SLAB, Material.STONE_BRICK_STAIRS,
            Material.MOSSY_COBBLESTONE,
            Material.POLISHED_ANDESITE, Material.POLISHED_DIORITE, Material.POLISHED_GRANITE,
            Material.SMOOTH_STONE, Material.SMOOTH_STONE_SLAB
    );

    public ZodiacPerkListener(JavaPlugin plugin, ProfileManager profileManager) {
        this.plugin = plugin;
        this.profileManager = profileManager;

        this.ratXpBonus = plugin.getConfig().getDouble("zodiac.perks.rat-xp-bonus", 0.25);
        this.tigerBonusDamage = plugin.getConfig().getDouble("zodiac.perks.tiger-bonus-damage", 2.0);
        this.rabbitDodgeChance = plugin.getConfig().getDouble("zodiac.perks.rabbit-dodge-chance", 0.10);
        this.snakePotionMultiplier = plugin.getConfig().getDouble("zodiac.perks.snake-potion-multiplier", 1.5);
        this.horseSpeedAmplifier = plugin.getConfig().getInt("zodiac.perks.horse-speed-amplifier", 1);
        this.monkeyFoodSaveChance = plugin.getConfig().getDouble("zodiac.perks.monkey-food-save-chance", 0.30);
        this.dogRegenRadius = plugin.getConfig().getInt("zodiac.perks.dog-regen-radius", 8);
        this.pigOreDoubleChance = plugin.getConfig().getDouble("zodiac.perks.pig-ore-double-chance", 0.15);
    }

    private Zodiac getPlayerZodiac(Player player) {
        ZodiacProfile profile = profileManager.getProfile(player.getUniqueId());
        return profile != null ? profile.getZodiac() : null;
    }

    // --- RAT: XP orbs worth 25% more ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onXpPickup(org.bukkit.event.player.PlayerExpChangeEvent event) {
        if (getPlayerZodiac(event.getPlayer()) != Zodiac.RAT) return;
        int original = event.getAmount();
        int bonus = (int) Math.ceil(original * ratXpBonus);
        event.setAmount(original + bonus);
    }

    // --- OX: Haste I below Y=50 ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onOxMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockY() == event.getTo().getBlockY()) return;
        Player player = event.getPlayer();
        if (getPlayerZodiac(player) != Zodiac.OX) return;

        if (player.getLocation().getBlockY() < 50) {
            if (!player.hasPotionEffect(PotionEffectType.HASTE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 0, true, false, true));
            }
        }
    }

    // --- TIGER: Bonus damage from above ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onTigerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (getPlayerZodiac(player) != Zodiac.TIGER) return;

        if (player.getLocation().getY() > event.getEntity().getLocation().getY() + 1.0) {
            event.setDamage(event.getDamage() + tigerBonusDamage);
        }
    }

    // --- RABBIT: 10% dodge chance ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRabbitDodge(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (getPlayerZodiac(player) != Zodiac.RABBIT) return;

        if (random.nextDouble() < rabbitDodgeChance) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PHANTOM_FLAP, 0.5f, 2.0f);
        }
    }

    // --- DRAGON: Immune to fire and lava ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDragonFireDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (getPlayerZodiac(player) != Zodiac.DRAGON) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            event.setCancelled(true);
            player.setFireTicks(0);
        }
    }

    // --- SNAKE: Potions last 50% longer ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSnakeDrinkPotion(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (getPlayerZodiac(player) != Zodiac.SNAKE) return;

        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION) return;

        // Apply extended duration after consumption
        new BukkitRunnable() {
            @Override
            public void run() {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    int extendedDuration = (int) (effect.getDuration() * snakePotionMultiplier);
                    player.addPotionEffect(new PotionEffect(
                            effect.getType(), extendedDuration, effect.getAmplifier(),
                            effect.isAmbient(), effect.hasParticles(), effect.hasIcon()
                    ));
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    // --- HORSE: Speed II on roads ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onHorseMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (getPlayerZodiac(player) != Zodiac.HORSE) return;

        Block below = player.getLocation().subtract(0, 1, 0).getBlock();
        UUID uid = player.getUniqueId();

        if (ROAD_BLOCKS.contains(below.getType())) {
            if (!horseSpeedActive.contains(uid)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, horseSpeedAmplifier, true, false, true));
                horseSpeedActive.add(uid);
            }
        } else {
            horseSpeedActive.remove(uid);
        }
    }

    // --- GOAT: Immune to knockback ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onGoatJoin(PlayerJoinEvent event) {
        applyGoatKnockback(event.getPlayer());
    }

    public void applyGoatKnockback(Player player) {
        if (getPlayerZodiac(player) != Zodiac.GOAT) return;
        AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (attr == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "goat_knockback");
        // Remove existing modifier if present
        attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(key))
                .forEach(attr::removeModifier);

        attr.addModifier(new AttributeModifier(key, 1.0, AttributeModifier.Operation.ADD_NUMBER));
    }

    public void removeGoatKnockback(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (attr == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "goat_knockback");
        attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(key))
                .forEach(attr::removeModifier);
    }

    // --- MONKEY: Chance to not consume food ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMonkeyEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (getPlayerZodiac(player) != Zodiac.MONKEY) return;

        if (event.getItem().getType().isEdible() && random.nextDouble() < monkeyFoodSaveChance) {
            ItemStack saved = event.getItem().clone();
            saved.setAmount(1);
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getInventory().addItem(saved);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // --- ROOSTER: Phantoms never target you ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRoosterPhantomTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Phantom)) return;
        if (!(event.getTarget() instanceof Player player)) return;
        if (getPlayerZodiac(player) != Zodiac.ROOSTER) return;

        event.setCancelled(true);
    }

    // --- DOG: Nearby allies get Regeneration I ---
    public void tickDogAura() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (getPlayerZodiac(player) != Zodiac.DOG) continue;

            for (Entity nearby : player.getNearbyEntities(dogRegenRadius, dogRegenRadius, dogRegenRadius)) {
                if (nearby instanceof Player ally && !ally.equals(player)) {
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false, true));
                }
            }
        }
    }

    // --- PIG: Ore blocks chance to double drops ---
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPigMine(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (getPlayerZodiac(player) != Zodiac.PIG) return;
        if (!ORE_BLOCKS.contains(event.getBlock().getType())) return;
        if (player.getInventory().getItemInMainHand().containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH)) return;

        if (random.nextDouble() < pigOreDoubleChance) {
            // Double the drops by dropping the same items again
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (ItemStack drop : event.getBlock().getDrops(player.getInventory().getItemInMainHand(), player)) {
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // --- Cleanup on quit ---
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        horseSpeedActive.remove(event.getPlayer().getUniqueId());
        removeGoatKnockback(event.getPlayer());
    }
}
