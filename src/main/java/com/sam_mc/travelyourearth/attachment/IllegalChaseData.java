package com.sam_mc.travelyourearth.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * "Police" data of an illegal villager (server only, saved in the world).
 *
 * @param knownBy      players that have seen its secret (only they can snitch on it)
 * @param scaredUntil  game time until it runs away from villagers and iron golems (someone saw it trading)
 * @param wantedUntil  game time until it's WANTED: it glows and runs away from everyone. Catch it with a boat!
 * @param snitch       the player that rang the bell on it (gets the reward when it's caught)
 * @param avoided      a player that snitched on a "colleague": this illegal villager avoids them for a while
 * @param avoidUntil   game time until it stops avoiding that player
 * @param hated        the player that snitched on IT: it avoids them forever, at all costs
 * @param leaving      it escaped: it's leaving the village forever (it disappears when no player is near)
 * @param shunnedUntil game time until the other villagers don't talk to it (it was discovered or caught)
 */
public record IllegalChaseData(List<UUID> knownBy, long scaredUntil, long wantedUntil, Optional<UUID> snitch,
                               Optional<UUID> avoided, long avoidUntil, Optional<UUID> hated, boolean leaving,
                               long shunnedUntil) {

    /** Max players remembered in knownBy (the oldest one is forgotten). */
    private static final int MAX_KNOWN = 16;

    public static final IllegalChaseData NONE = new IllegalChaseData(List.of(), 0L, 0L, Optional.empty(),
            Optional.empty(), 0L, Optional.empty(), false, 0L);

    public static final MapCodec<IllegalChaseData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.listOf().optionalFieldOf("known_by", List.of()).forGetter(IllegalChaseData::knownBy),
            Codec.LONG.optionalFieldOf("scared_until", 0L).forGetter(IllegalChaseData::scaredUntil),
            Codec.LONG.optionalFieldOf("wanted_until", 0L).forGetter(IllegalChaseData::wantedUntil),
            UUIDUtil.CODEC.optionalFieldOf("snitch").forGetter(IllegalChaseData::snitch),
            UUIDUtil.CODEC.optionalFieldOf("avoided").forGetter(IllegalChaseData::avoided),
            Codec.LONG.optionalFieldOf("avoid_until", 0L).forGetter(IllegalChaseData::avoidUntil),
            UUIDUtil.CODEC.optionalFieldOf("hated").forGetter(IllegalChaseData::hated),
            Codec.BOOL.optionalFieldOf("leaving", false).forGetter(IllegalChaseData::leaving),
            Codec.LONG.optionalFieldOf("shunned_until", 0L).forGetter(IllegalChaseData::shunnedUntil)
    ).apply(instance, IllegalChaseData::new));

    public boolean knows(UUID player) {
        return this.knownBy.contains(player);
    }

    public boolean isScared(long now) {
        return now < this.scaredUntil;
    }

    public boolean isWanted() {
        return this.wantedUntil > 0L;
    }

    /** True if it doesn't want to be near this player right now (hated forever, or avoided for a while). */
    public boolean avoids(UUID player, long now) {
        return this.hated.map(player::equals).orElse(false)
                || (now < this.avoidUntil && this.avoided.map(player::equals).orElse(false));
    }

    public IllegalChaseData withKnown(UUID player) {
        if (knows(player)) return this;
        List<UUID> copy = new ArrayList<>(this.knownBy);
        copy.add(player);
        while (copy.size() > MAX_KNOWN) copy.remove(0);
        return new IllegalChaseData(List.copyOf(copy), this.scaredUntil, this.wantedUntil, this.snitch,
                this.avoided, this.avoidUntil, this.hated, this.leaving, this.shunnedUntil);
    }

    public IllegalChaseData withScaredUntil(long until) {
        return new IllegalChaseData(this.knownBy, Math.max(this.scaredUntil, until), this.wantedUntil, this.snitch,
                this.avoided, this.avoidUntil, this.hated, this.leaving, this.shunnedUntil);
    }

    /** Wanted! And from now on it hates the snitch forever. */
    public IllegalChaseData withWanted(long until, UUID snitch) {
        return new IllegalChaseData(this.knownBy, this.scaredUntil, until, Optional.of(snitch),
                this.avoided, this.avoidUntil, Optional.of(snitch), this.leaving, this.shunnedUntil);
    }

    /** Not wanted anymore (caught or escaped). It keeps hating the snitch. */
    public IllegalChaseData withoutWanted() {
        return new IllegalChaseData(this.knownBy, this.scaredUntil, 0L, Optional.empty(),
                this.avoided, this.avoidUntil, this.hated, this.leaving, this.shunnedUntil);
    }

    /** Avoids this player for a while (a "colleague" was snitched on). */
    public IllegalChaseData withAvoided(UUID player, long until) {
        return new IllegalChaseData(this.knownBy, this.scaredUntil, this.wantedUntil, this.snitch,
                Optional.of(player), Math.max(this.avoidUntil, until), this.hated, this.leaving, this.shunnedUntil);
    }

    /** It escaped: from now on it leaves the village. */
    public IllegalChaseData withLeaving() {
        return new IllegalChaseData(this.knownBy, this.scaredUntil, 0L, Optional.empty(),
                this.avoided, this.avoidUntil, this.hated, true, this.shunnedUntil);
    }

    /** The other villagers won't talk to it until then (never shortens an existing one). */
    public IllegalChaseData withShunnedUntil(long until) {
        return new IllegalChaseData(this.knownBy, this.scaredUntil, this.wantedUntil, this.snitch,
                this.avoided, this.avoidUntil, this.hated, this.leaving, Math.max(this.shunnedUntil, until));
    }

    public boolean isShunned(long now) {
        return now < this.shunnedUntil;
    }
}