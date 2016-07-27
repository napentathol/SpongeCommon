package org.spongepowered.common.event.tracking.phase;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GenerationPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        CHUNK_LOADING,
        WORLD_SPAWNER_SPAWNING() {
            @SuppressWarnings("unchecked")
            @Override
            public void unwind(CauseTracker causeTracker, PhaseContext context) {
                final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
                if (spawnedEntities.isEmpty()) {
                    return;
                }
                final Cause cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World", causeTracker.getWorld()).build();

                final SpawnEntityEvent.Spawner
                        event =
                        SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, causeTracker.getWorld());
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity entity : event.getEntities()) {
                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                    }
                }

                context.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, context));

            }
        },
        POPULATOR_RUNNING(BlockPhase.State.BLOCK_DECAY, BlockPhase.State.BLOCK_DROP_ITEMS, BlockPhase.State.RESTORING_BLOCKS, State.WORLD_SPAWNER_SPAWNING, GeneralPhase.Post.UNWINDING) {
            @Override
            public boolean canSwitchTo(IPhaseState state) { // Because populators are possibly re-entrant due to mods
                return super.canSwitchTo(state) || state == POPULATOR_RUNNING;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void unwind(CauseTracker causeTracker, PhaseContext context) {
                final PopulatorType
                        runningGenerator = context.firstNamed(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
                final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
                if (spawnedEntities.isEmpty()) {
                    return;
                }
                final Cause.Builder cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World",  causeTracker.getWorld());
                if (runningGenerator != null) { // There are corner cases where a populator may not have a proper type.
                    cause.named(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, runningGenerator);
                }
                final SpawnEntityEvent.Spawner
                        event =
                        SpongeEventFactory.createSpawnEntityEventSpawner(cause.build(), spawnedEntities, causeTracker.getWorld());
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity entity : event.getEntities()) {
                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                    }
                }
            }
        },
        TERRAIN_GENERATION(BlockPhase.State.BLOCK_DECAY, BlockPhase.State.BLOCK_DROP_ITEMS, BlockPhase.State.RESTORING_BLOCKS, State.POPULATOR_RUNNING, State.WORLD_SPAWNER_SPAWNING,
                GeneralPhase.Post.UNWINDING) {
            @SuppressWarnings("unchecked")
            @Override
            public void unwind(CauseTracker causeTracker, PhaseContext context) {
                final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
                if (spawnedEntities.isEmpty()) {
                    return;
                }
                final Cause cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World",  causeTracker.getWorld())
                        .build();
                final SpawnEntityEvent.Spawner
                        event =
                        SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, causeTracker.getWorld());
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity entity : event.getEntities()) {
                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                    }
                }
            }
        };

        private final Set<IPhaseState> compatibleStates;

        State() {
            this(ImmutableSet.of());
        }

        State(ImmutableSet<IPhaseState> states) {
            this.compatibleStates = states;
        }

        State(IPhaseState... states) {
            this(ImmutableSet.copyOf(states));
        }


        @Override
        public GenerationPhase getPhase() {
            return TrackingPhases.GENERATION;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return this.compatibleStates.contains(state);
        }

        @Override
        public boolean isExpectedForReEntrance() {
            return true;
        }

        public boolean captureEntitySpawn(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
            return context.getCapturedEntities().add(entity);
        }

        public void unwind(CauseTracker causeTracker, PhaseContext context) {

        }
    }


    GenerationPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public GenerationPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        ((State) state).unwind(causeTracker, phaseContext);
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return false;
    }

    @Override
    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return true;
    }


    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return phaseData.state != GenerationPhase.State.WORLD_SPAWNER_SPAWNING;
    }

    @Override
    public void appendNotifierPreBlockTick(CauseTracker causeTracker, BlockPos pos, IPhaseState currentState, PhaseContext context, PhaseContext newContext) {

    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return ((GenerationPhase.State) phaseState).captureEntitySpawn(phaseState, context, entity, chunkX, chunkZ);
    }

    @Override
    protected void processPostEntitySpawns(CauseTracker causeTracker, IPhaseState unwindingState, ArrayList<Entity> entities) {
        super.processPostEntitySpawns(causeTracker, unwindingState, entities);
    }

    @Override
    public boolean isWorldGeneration(IPhaseState state) {
        return true;
    }

    @Override
    public void appendPreBlockProtectedCheck(Cause.Builder builder, IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker) {
    }

}