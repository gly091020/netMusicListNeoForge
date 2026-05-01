package com.gly091020.netMusicListNeoforge.entity;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.packet.MusicPlayerEntityPlayMusicPacket;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MusicPlayerEntity extends LivingEntity implements Leashable {
    private ItemStack musicCD = ItemStack.EMPTY;
    private boolean isPlaying = false;
    private static final EntityDataAccessor<Boolean> DATA_PLAYING =
            SynchedEntityData.defineId(MusicPlayerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> DATA_RAW_URL =
            SynchedEntityData.defineId(MusicPlayerEntity.class, EntityDataSerializers.STRING);
    private int tick = 0;

    private @Nullable LeashData leashData;

    public MusicPlayerEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYING, isPlaying);
        builder.define(DATA_RAW_URL, "");
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
        if(level() instanceof ServerLevel level){
            if(damageSource.getEntity() instanceof Player player){
                player.addItem(toPlayerItem(this));
            }else{
                dropAllDeathLoot(level, damageSource);
            }
        }
        if(isLeashed())
            dropLeash();
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void dropAllDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource damageSource) {
        if(!damageSource.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS) &&
                !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
            this.spawnAtLocation(level, toPlayerItem(this));
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        try{
            readLeashData(input);
            isPlaying = input.getBooleanOr("is_playing", false);
            musicCD = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        }catch (Exception exception){
            NetMusicList.LOGGER.error("实体加载失败", exception);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        try{
            writeLeashData(output, getLeashData());
            output.putBoolean("is_playing", isPlaying);
            if(!musicCD.isEmpty()) {
                output.store("item", ItemStack.CODEC, musicCD);
            }
        }catch (Exception exception){
            NetMusicList.LOGGER.error("实体保存失败", exception);
        }
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        if(source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND) || source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS)){
            die(source);
            return true;
        }
        if(source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.MAGIC))return false;
        return super.hurtClient(source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if(source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND) || source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS)){
            die(source);
            return true;
        }
        if(source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.MAGIC))return false;
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    public void setMusicCD(ItemStack stack){
        if(isRightItem(stack) || stack.isEmpty()) {
            this.musicCD = stack;
            var info = ItemMusicCD.getSongInfo(stack);
            if(info != null)
                entityData.set(DATA_RAW_URL, info.songUrl);
            else
                entityData.set(DATA_RAW_URL, "");
        }
        tick = 0;
    }

    public boolean isRightItem(ItemStack stack){
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
        if(!level().isClientSide())
            entityData.set(DATA_PLAYING, isPlaying);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NonNull Vec3 location) {
        if(level().isClientSide())return InteractionResult.SUCCESS;
        var useItem = player.getItemInHand(hand);
        var info = ItemMusicCD.getSongInfo(useItem);
        if(info != null && info.vip && (NetMusicList.CONFIG.noVIP || NetMusicListUtil.hasLoginNeed())){
            if(level().isClientSide())
                player.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip")
                        .withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS;
        }
        if(player.isShiftKeyDown() && useItem.isEmpty()){
            die(level().damageSources().playerAttack(player));
        } else if(useItem.isEmpty() && !musicCD.isEmpty()){
            player.setItemInHand(hand, musicCD);
            setMusicCD(ItemStack.EMPTY);
            setPlaying(false);
            return InteractionResult.SUCCESS;
        } else if (isRightItem(useItem) && musicCD.isEmpty() && info != null) {
            setMusicCD(useItem);
            setPlaying(true);
            player.setItemInHand(hand, ItemStack.EMPTY);
            tryPlayMusic();
            return InteractionResult.SUCCESS;
        }else if(isRightItem(useItem) && !musicCD.isEmpty() && info != null){
            player.setItemInHand(hand, musicCD);
            setMusicCD(useItem);
            setPlaying(true);
            tryPlayMusic();
            return InteractionResult.SUCCESS;
        } else if (useItem.isEmpty()) {
            die(level().damageSources().playerAttack(player));
        }
        return super.interact(player, hand, location);
    }

    public static ItemStack toPlayerItem(MusicPlayerEntity entity){
        var stack = NetMusicList.MUSIC_PLAYER_ITEM.get().getDefaultInstance().copy();
        var c = NetMusicPlayerItem.getContainer(stack);
        if(!entity.getMusicCD().isEmpty()){
            c.setItem(0, entity.getMusicCD());
        }
        return stack;
    }

    public static MusicPlayerEntity fromItem(ItemStack stack, @Nullable Level level){
        if(!stack.is(NetMusicList.MUSIC_PLAYER_ITEM))return null;
        var cd = NetMusicPlayerItem.getContainer(stack).getItem(0);
        var entity = new MusicPlayerEntity(NetMusicList.MUSIC_PLAYER_ENTITY.get(), level);
        entity.setMusicCD(cd);
        return entity;
    }

    public void tryPlayMusic(){
        if(this.isRightItem(this.getMusicCD())){
            var info = ItemMusicCD.getSongInfo(this.getMusicCD());
            if(info == null)return;
            this.setPlaying(true);
            if(level().isClientSide())
                NetworkHandler.sendToServer(new MusicPlayerEntityPlayMusicPacket(this.getId(),
                        info.songUrl, info.songTime, info.songName));
            else
                ((ServerLevel)level()).getPlayers(player -> player.distanceTo(this) < 100)
                        .forEach(player ->
                        NetworkHandler.sendToClientPlayer(new MusicPlayerEntityPlayMusicPacket(this.getId(),
                        info.songUrl, info.songTime, info.songName), player));
        }
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
//        if(isPlaying())return;
        if(this.isRightItem(this.getMusicCD())){
            var info = ItemMusicCD.getSongInfo(this.getMusicCD());
            if(info != null && tick > info.songTime * 20 + 5){
                tick = 0;
                NetMusicListItem.nextMusic(musicCD);
                setMusicCD(musicCD);
                tryPlayMusic();
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

    @Override
    public @Nullable LeashData getLeashData() {
        return leashData;
    }

    @Override
    public void setLeashData(@Nullable LeashData leashData) {
        this.leashData = leashData;
    }
}
