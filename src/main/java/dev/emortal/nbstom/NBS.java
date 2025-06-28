package dev.emortal.nbstom;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NBS {

    private static final Map<UUID, NBSPlayer> playingSongs = new ConcurrentHashMap<>();

    private static class NBSPlayer {
        final NBSSong song;
        final Audience audience;
        final Scheduler scheduler;
        final UUID stopId;

        Task task;
        int tick = 0;

        NBSPlayer(NBSSong song, Audience audience, Scheduler scheduler, UUID stopId) {
            this.song = song;
            this.audience = audience;
            this.scheduler = scheduler;
            this.stopId = stopId;
        }

        void schedule() {
            if (this.task != null && this.task.isAlive()) {
                this.task.cancel();
            }

            this.task = this.scheduler.submitTask(() -> {
                if (tick > song.getLength() + 1) {
                    playingSongs.remove(this.stopId);
                    return TaskSchedule.stop();
                }

                List<Sound> sounds = song.getTicks().get(tick);
                if (sounds != null) {
                    for (Sound sound : sounds) {
                        audience.playSound(sound, Sound.Emitter.self());
                    }
                }

                tick++;

                return TaskSchedule.millis((long) (1000.0 / song.getTps()));
            });
        }

        void pause() {
            if (this.task != null) {
                this.task.cancel();
            }
        }

        void stop() {
            if (this.task != null) {
                this.task.cancel();
            }
        }
    }


    /**
     * Plays this NBS song to an audience
     * Can be cancelled by using {@link #stop(UUID)} with the player's UUID. This stops automatically when the player leaves.
     *
     * @param player The player to play the song to
     */
    public static void play(NBSSong song, Player player) {
        play(song, player, player.scheduler(), player.getUuid());
    }

    /**
     * Plays this NBS song to an audience
     * Can be cancelled by using {@link #stop(UUID)} with the same stopId or by cancelling via the scheduler
     *
     * @param audience The audience to play the song to
     * @param scheduler The scheduler to tick the song on
     * @param stopId The id for use with {@link #stop(UUID)} later
     */
    public static void play(NBSSong song, Audience audience, Scheduler scheduler, UUID stopId) {
        stop(stopId);

        NBSPlayer nbsPlayer = new NBSPlayer(song, audience, scheduler, stopId);
        playingSongs.put(stopId, nbsPlayer);
        nbsPlayer.schedule();
    }

    public static void stop(Player player) {
        stop(player.getUuid());
    }

    public static void stop(UUID stopId) {
        NBSPlayer nbsPlayer = playingSongs.remove(stopId);
        if (nbsPlayer != null) {
            nbsPlayer.stop();
        }
    }

    /**
     * Pauses a song for a specific player.
     * The song can be resumed using {@link #resume(Player)}.
     *
     * @param player The player to pause the song for.
     */
    public static void pause(Player player) {
        pause(player.getUuid());
    }

    /**
     * Pauses a song for a specific stopId.
     * The song can be resumed using {@link #resume(UUID)}.
     *
     * @param stopId The id of the song to pause.
     */
    public static void pause(UUID stopId) {
        NBSPlayer nbsPlayer = playingSongs.get(stopId);
        if (nbsPlayer != null) {
            nbsPlayer.pause();
        }
    }

    /**
     * Resumes a paused song for a specific player.
     *
     * @param player The player to resume the song for.
     */
    public static void resume(Player player) {
        resume(player.getUuid());
    }

    /**
     * Resumes a paused song for a specific stopId.
     *
     * @param stopId The id of the song to resume.
     */
    public static void resume(UUID stopId) {
        NBSPlayer nbsPlayer = playingSongs.get(stopId);
        if (nbsPlayer != null) {
            if (nbsPlayer.task == null || !nbsPlayer.task.isAlive()) {
                nbsPlayer.schedule();
            }
        }
    }
}