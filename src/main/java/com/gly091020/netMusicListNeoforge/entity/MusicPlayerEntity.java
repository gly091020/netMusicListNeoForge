package com.gly091020.netMusicListNeoforge.entity;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.server.music.EntityRinger;
import com.gly091020.netMusicListNeoforge.server.music.MusicRingManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * 放置/掉落形态的随身播放器实体。
 * 播放计算全部交给服务端 {@link EntityRinger}，实体只保存物品与播放状态用于渲染。
 */
public class MusicPlayerEntity extends LivingEntity {
    private ItemStack musicCD = ItemStack.EMPTY;
    private boolean isPlaying = false;
    private boolean loadedFromSave = false;
    private static final EntityDataAccessor<Boolean> DATA_PLAYING =
            SynchedEntityData.defineId(MusicPlayerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> DATA_RAW_URL =
            SynchedEntityData.defineId(MusicPlayerEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> DATA_RINGER_ID =
            SynchedEntityData.defineId(MusicPlayerEntity.class, EntityDataSerializers.STRING);

    public MusicPlayerEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYING, isPlaying);
        builder.define(DATA_RAW_URL, "");
        builder.define(DATA_RINGER_ID, "");
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot equipmentSlot, @NotNull ItemStack itemStack) {
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (level() instanceof ServerLevel level) {
            removeRingerForTransfer();
            if (damageSource.getEntity() instanceof Player player) {
                player.addItem(toPlayerItem(this));
            } else {
                dropAllDeathLoot(level, damageSource);
            }
        }
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void dropAllDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource damageSource) {
        if (!damageSource.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS) &&
                !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.spawnAtLocation(toPlayerItem(this));
        }
    }

    @Override
    public void kill() {
        removeRingerForTransfer();
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND) || source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS)) {
            die(source);
            return true;
        }
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.MAGIC)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        loadedFromSave = true;
        try {
            isPlaying = compound.contains("is_playing") && compound.getBoolean("is_playing");
            if (compound.contains("Item")) {
                musicCD = ItemStack.parse(level().registryAccess(), Objects.requireNonNull(compound.get("Item"))).orElseThrow();
            } else {
                musicCD = ItemStack.EMPTY;
            }
            if (compound.contains("ringer_id")) {
                entityData.set(DATA_RINGER_ID, compound.getString("ringer_id"));
            }
        } catch (Exception exception) {
            NetMusicList.LOGGER.error("实体加载失败", exception);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        try {
            compound.putBoolean("is_playing", isPlaying);
            if (!musicCD.isEmpty()) {
                compound.put("Item", musicCD.save(level().registryAccess()));
            }
            var ringerId = getRingerId();
            if (ringerId != null) {
                compound.putString("ringer_id", ringerId.toString());
            }
        } catch (Exception exception) {
            NetMusicList.LOGGER.error("实体保存失败", exception);
        }
    }

    public void setMusicCD(ItemStack stack) {
        if (isRightItem(stack) || stack.isEmpty()) {
            this.musicCD = stack;
            var info = ItemMusicCD.getSongInfo(stack);
            entityData.set(DATA_RAW_URL, info != null ? info.songUrl : "");
        }
    }

    public boolean isRightItem(ItemStack stack) {
        return ItemMusicCD.getSongInfo(stack) != null;
    }

    public ItemStack getMusicCD() {
        return musicCD;
    }

    public boolean isPlaying() {
        return entityData.get(DATA_PLAYING);
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        if (!level().isClientSide) {
            entityData.set(DATA_PLAYING, isPlaying);
        }
    }

    public void setRingerId(@Nullable UUID ringerId) {
        entityData.set(DATA_RINGER_ID, ringerId == null ? "" : ringerId.toString());
    }

    @Nullable
    public UUID getRingerId() {
        var s = entityData.get(DATA_RINGER_ID);
        return s == null || s.isEmpty() ? null : UUID.fromString(s);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        var useItem = player.getItemInHand(hand);
        var info = ItemMusicCD.getSongInfo(useItem);
        if (info != null && NetMusicListUtil.isVipBlocked(info)) {
            player.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && useItem.isEmpty()) {
            die(level().damageSources().playerAttack(player));
        } else if (useItem.isEmpty() && !musicCD.isEmpty()) {
            player.setItemInHand(hand, musicCD);
            setMusicCD(ItemStack.EMPTY);
            setPlaying(false);
            stopRinger();
            return InteractionResult.SUCCESS;
        } else if (isRightItem(useItem) && musicCD.isEmpty() && info != null) {
            setMusicCD(useItem);
            setPlaying(true);
            player.setItemInHand(hand, ItemStack.EMPTY);
            tryPlayMusic();
            return InteractionResult.SUCCESS;
        } else if (isRightItem(useItem) && !musicCD.isEmpty() && info != null) {
            player.setItemInHand(hand, musicCD);
            setMusicCD(useItem);
            setPlaying(true);
            tryPlayMusic();
            return InteractionResult.SUCCESS;
        } else if (useItem.isEmpty()) {
            die(level().damageSources().playerAttack(player));
        }
        return super.interact(player, hand);
    }

    private void stopRinger() {
        var id = getRingerId();
        if (id == null || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var ringer = MusicRingManager.get(serverLevel).getRinger(id);
        if (ringer instanceof EntityRinger entityRinger) {
            entityRinger.stop();
        }
    }

    public static ItemStack toPlayerItem(MusicPlayerEntity entity) {
        var stack = NetMusicList.MUSIC_PLAYER_ITEM.get().getDefaultInstance().copy();
        var c = NetMusicPlayerItem.getContainer(stack);
        if (!entity.getMusicCD().isEmpty()) {
            c.setItem(0, entity.getMusicCD());
        }
        var ringerId = entity.getRingerId();
        if (ringerId != null) {
            stack.set(NetMusicList.RINGER_COMPONENT.get(), ringerId);
        }
        return stack;
    }

    public static MusicPlayerEntity fromItem(ItemStack stack, @Nullable Level level) {
        if (!stack.is(NetMusicList.MUSIC_PLAYER_ITEM)) {
            return null;
        }
        var cd = NetMusicPlayerItem.getContainer(stack).getItem(0);
        var entity = new MusicPlayerEntity(NetMusicList.MUSIC_PLAYER_ENTITY.get(), level);
        entity.setMusicCD(cd);
        return entity;
    }

    /** 服务端：让对应 Ringer 开始播放（实体形态与物品形态共用同一 Ringer UUID）。 */
    public void tryPlayMusic() {
        if (level().isClientSide || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var id = getRingerId();
        if (id == null) {
            id = UUID.randomUUID();
            setRingerId(id);
        }
        var manager = MusicRingManager.get(serverLevel);
        var ringer = manager.getRinger(id);
        if (ringer instanceof EntityRinger entityRinger) {
            entityRinger.play();
            return;
        }
        // 物品形式的 Ringer 若还残留，先移除（触发停止广播并持久化），避免同 UUID 冲突
        if (ringer != null) {
            manager.removeRinger(id);
        }
        var entityRinger = new EntityRinger(manager, serverLevel, id, this.getId());
        manager.addRinger(entityRinger);
        entityRinger.play();
    }

    /** 实体移除前把播放状态移交给 SavedData（供回收为物品后恢复播放），并广播停止。 */
    private void removeRingerForTransfer() {
        var id = getRingerId();
        if (id != null && level() instanceof ServerLevel serverLevel) {
            MusicRingManager.get(serverLevel).removeRinger(id);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            var id = getRingerId();
            if (id != null) {
                var manager = MusicRingManager.get(serverLevel);
                var ringer = manager.getRinger(id);
                if (!(ringer instanceof EntityRinger)) {
                    if (ringer != null) {
                        // 物品形式的 Ringer 残留：先移除（广播停止并持久化播放状态）
                        manager.removeRinger(id);
                    }
                    var entityRinger = new EntityRinger(manager, serverLevel, id, this.getId());
                    manager.addRinger(entityRinger);
                    if (entityRinger.isRingerPlaying()) {
                        entityRinger.resume();
                    } else if (!loadedFromSave) {
                        // 新生成（丢出/放置）的实体：自动开始播放
                        entityRinger.play();
                    }
                }
            }
        }
    }

    @Override
    protected @NotNull Component getTypeName() {
        return Component.translatable("item.net_music_list.music_player");
    }

    @Override
    public boolean canDrownInFluidType(@NotNull FluidType type) {
        return false;
    }
}
