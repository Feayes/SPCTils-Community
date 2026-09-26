package com.reveth.spctils.features.debug;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PacketRecorder {
    private static final int DURATION_SECONDS = 60;
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // Max field-dump recursion depth. Kept low to avoid StackOverflow on circular refs.
    private static final int MAX_DEPTH = 2;

    // Packages we consider "safe" to recurse into (Minecraft data classes, not renderers/worlds/etc.)
    private static final Set<String> SAFE_PACKAGES = Set.of(
        "net.minecraft.network.packet",
        "net.minecraft.network.message",
        "net.minecraft.entity.data",
        "net.minecraft.util.math",
        "net.minecraft.util"
    );

    // Packages we must NEVER recurse into (causes loops or is too large)
    private static final Set<String> BLOCKED_PACKAGES = Set.of(
        "io.netty",
        "java.nio",
        "sun.",
        "net.minecraft.client",
        "net.minecraft.world",
        "net.minecraft.server",
        "net.minecraft.entity.Entity",
        "net.minecraft.block"
    );

    private static volatile boolean recording = false;
    private static final List<String> recordedPackets = new ArrayList<>();
    private static long startTime = 0;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "spctils-packet-recorder");
        t.setDaemon(true);
        return t;
    });
    private static ScheduledFuture<?> stopTask = null;

    public static boolean isRecording() { return recording; }

    public static boolean start() {
        if (recording) return false;
        synchronized (recordedPackets) { recordedPackets.clear(); }
        startTime = System.currentTimeMillis();
        recording = true;
        stopTask = scheduler.schedule(PacketRecorder::stopAndSave, DURATION_SECONDS, TimeUnit.SECONDS);
        com.reveth.spctils.Spctils.LOGGER.info("[PacketRecorder] Started ({} seconds).", DURATION_SECONDS);
        return true;
    }

    public static void stopAndSave() {
        if (!recording) return;
        recording = false;
        if (stopTask != null && !stopTask.isDone()) { stopTask.cancel(false); stopTask = null; }
        saveToFile();
    }

    /** Called from Mixin for every packet. */
    public static void record(Packet<?> packet, boolean isIncoming) {
        if (!recording) return;
        String direction = isIncoming ? "S2C" : "C2S";
        String timestamp = LocalDateTime.now().format(TIME_FMT);
        long elapsed = System.currentTimeMillis() - startTime;

        String rawClass = packet.getClass().getName();
        String readableName = PacketMappings.translate(rawClass);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] [+%dms] [%s] %s%n", timestamp, elapsed, direction, readableName));

        // Use IdentityHashMap as a visited set to detect circular references
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        try {
            List<String> fields = dumpFields(packet, visited, 0);
            for (String field : fields) {
                sb.append("  ").append(field).append("\n");
            }
        } catch (StackOverflowError e) {
            sb.append("  <StackOverflow during field dump - object too deep>\n");
        } catch (Exception e) {
            sb.append("  <error during field dump: ").append(e.getMessage()).append(">\n");
        }

        synchronized (recordedPackets) {
            recordedPackets.add(sb.toString());
        }
    }

    /**
     * Reflect over all declared fields of an object, including superclass fields.
     * Uses a visited set to prevent circular reference loops.
     */
    private static List<String> dumpFields(Object obj, IdentityHashMap<Object, Boolean> visited, int depth) {
        List<String> result = new ArrayList<>();
        if (obj == null || visited.containsKey(obj)) return result;
        visited.put(obj, Boolean.TRUE);

        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.isSynthetic()) continue;

                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    String valueStr = formatValue(value, visited, depth);
                    result.add(String.format("%s (%s) = %s", field.getName(), field.getType().getSimpleName(), valueStr));
                } catch (StackOverflowError e) {
                    result.add(String.format("%s = <StackOverflow>", field.getName()));
                } catch (Exception e) {
                    result.add(String.format("%s = <error: %s>", field.getName(), e.getMessage()));
                }
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    /**
     * Safely format any value to a string. Uses depth and visited tracking.
     */
    private static String formatValue(Object value, IdentityHashMap<Object, Boolean> visited, int depth) {
        if (value == null) return "null";

        // Hard depth limit
        if (depth >= MAX_DEPTH) {
            if (value instanceof Text) return "\"" + ((Text) value).getString() + "\"";
            return "<" + value.getClass().getSimpleName() + ">";
        }

        // Already visited this instance → circular ref
        if (visited.containsKey(value)) return "<circular ref: " + value.getClass().getSimpleName() + ">";

        // Minecraft Text → extract plain string
        if (value instanceof Text) {
            return "\"" + ((Text) value).getString() + "\"";
        }

        // Primitive wrapper, String, UUID, enums, identifiers → toString is safe
        Class<?> cls = value.getClass();
        if (isPrimitiveSafe(cls)) {
            return value.toString();
        }

        // OptionalInt, OptionalLong, OptionalDouble
        if (value instanceof OptionalInt oi) return oi.isPresent() ? String.valueOf(oi.getAsInt()) : "empty";
        if (value instanceof OptionalLong ol) return ol.isPresent() ? String.valueOf(ol.getAsLong()) : "empty";
        if (value instanceof OptionalDouble od) return od.isPresent() ? String.valueOf(od.getAsDouble()) : "empty";
        if (value instanceof Optional<?> opt) return opt.map(o -> formatValue(o, visited, depth + 1)).orElse("empty");

        // Collections
        if (value instanceof Collection<?> col) {
            if (col.isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            int i = 0;
            for (Object item : col) {
                if (i++ > 0) sb.append(", ");
                if (i > 8) { sb.append("...+").append(col.size() - 8).append(" more"); break; }
                sb.append(formatValue(item, visited, depth + 1));
            }
            sb.append("]");
            return sb.toString();
        }

        // Maps
        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) return "{}";
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i++ > 0) sb.append(", ");
                if (i > 5) { sb.append("..."); break; }
                sb.append(formatValue(entry.getKey(), visited, depth + 1))
                  .append("=")
                  .append(formatValue(entry.getValue(), visited, depth + 1));
            }
            sb.append("}");
            return sb.toString();
        }

        // int[] / byte[] / etc. primitive arrays
        if (cls == int[].class) return Arrays.toString((int[]) value);
        if (cls == byte[].class) return Arrays.toString((byte[]) value);
        if (cls == long[].class) return Arrays.toString((long[]) value);
        if (cls == float[].class) return Arrays.toString((float[]) value);
        if (cls == double[].class) return Arrays.toString((double[]) value);
        if (cls == boolean[].class) return Arrays.toString((boolean[]) value);

        // Object arrays
        if (cls.isArray()) {
            try {
                Object[] arr = (Object[]) value;
                if (arr.length == 0) return "[]";
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < Math.min(arr.length, 8); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(formatValue(arr[i], visited, depth + 1));
                }
                if (arr.length > 8) sb.append(", ...+").append(arr.length - 8).append(" more");
                sb.append("]");
                return sb.toString();
            } catch (ClassCastException ignored) {}
        }

        // Check if class is in a blocked/dangerous package — just use toString
        String clsName = cls.getName();
        for (String blocked : BLOCKED_PACKAGES) {
            if (clsName.startsWith(blocked)) {
                return "<" + PacketMappings.translate(clsName) + ">";
            }
        }

        // For known-safe Minecraft data classes, recurse 1 more level
        boolean isSafe = false;
        for (String safe : SAFE_PACKAGES) {
            if (clsName.startsWith(safe)) { isSafe = true; break; }
        }

        if (isSafe || depth == 0) {
            visited.put(value, Boolean.TRUE);
            try {
                List<String> nested = dumpFields(value, visited, depth + 1);
                if (nested.isEmpty()) return PacketMappings.translate(clsName) + "{}";
                StringBuilder sb = new StringBuilder(PacketMappings.translate(clsName)).append("{");
                for (int i = 0; i < Math.min(nested.size(), 6); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(nested.get(i));
                }
                if (nested.size() > 6) sb.append(", ...");
                sb.append("}");
                return sb.toString();
            } catch (StackOverflowError e) {
                return "<" + cls.getSimpleName() + ": StackOverflow>";
            }
        }

        // Fallback: just show type
        return "<" + PacketMappings.translate(clsName) + ">";
    }

    private static boolean isPrimitiveSafe(Class<?> cls) {
        return cls.isPrimitive()
            || cls == Boolean.class || cls == Byte.class || cls == Character.class
            || cls == Short.class || cls == Integer.class || cls == Long.class
            || cls == Float.class || cls == Double.class
            || cls == String.class
            || cls == java.util.UUID.class
            || cls.isEnum()
            || cls == net.minecraft.util.Identifier.class
            || cls == net.minecraft.util.math.Vec3d.class
            || cls == net.minecraft.util.math.BlockPos.class
            || cls == net.minecraft.util.math.ChunkPos.class;
    }

    private static void saveToFile() {
        List<String> snapshot;
        synchronized (recordedPackets) { snapshot = new ArrayList<>(recordedPackets); }

        File outputDir = FabricLoader.getInstance().getGameDir().resolve("spctils_packets").toFile();
        if (!outputDir.exists()) outputDir.mkdirs();

        String fileName = "packets_" + LocalDateTime.now().format(FILE_FMT) + ".txt";
        File outputFile = new File(outputDir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("=== SPCTils Detailed Packet Recording ===\n");
            writer.write("Duration: " + DURATION_SECONDS + " seconds\n");
            writer.write("Total packets: " + snapshot.size() + "\n");
            writer.write("==========================================\n\n");
            for (String entry : snapshot) {
                writer.write(entry);
                writer.write("\n");
            }
            com.reveth.spctils.Spctils.LOGGER.info("[PacketRecorder] Saved {} packets to {}", snapshot.size(), outputFile.getAbsolutePath());
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                String path = "spctils_packets/" + fileName;
                mc.execute(() -> mc.player.sendMessage(
                    Text.literal("§a[PacketRecorder] §f" + snapshot.size() + " packets §7→ " + path), false
                ));
            }
        } catch (IOException e) {
            com.reveth.spctils.Spctils.LOGGER.error("[PacketRecorder] Failed to save", e);
        }
    }
}
